# models/person_segmenter.py
import os

import torch
import numpy as np
from transformers import SegformerFeatureExtractor, SegformerForSemanticSegmentation
from PIL import Image
import cv2
from typing import Union, Tuple
from config.segformer_config import SegformerConfig
from utils.image_utils import load_image, save_image, overlay_mask, convert_to_pil
from typing import Optional, Union, Tuple


class PersonSegmenter:
    def __init__(self, model_name: str = SegformerConfig.MODEL_NAME):
        """
        Segformer를 이용한 사람 세그멘테이션 모듈
        Args:
            model_name: 사용할 Segformer 모델 이름
        """
        self.device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')

        # Segformer 모델 및 특징 추출기 초기화
        self.feature_extractor = SegformerFeatureExtractor.from_pretrained(model_name)
        self.model = SegformerForSemanticSegmentation.from_pretrained(model_name)
        self.model.to(self.device)
        self.model.eval()

        # ADE20K 데이터셋의 person 클래스 인덱스
        self.person_idx = 12

    def preprocess_image(self, image: Union[str, np.ndarray, Image.Image]) -> dict:
        """
        이미지 전처리
        Args:
            image: 입력 이미지 (파일 경로, numpy 배열, 또는 PIL Image)
        Returns:
            전처리된 이미지 dictionary
        """
        # 이미지 로드 및 PIL Image로 변환
        if isinstance(image, str):
            image = load_image(image)
        image = convert_to_pil(image)

        # feature_extractor를 사용하여 이미지 전처리
        inputs = self.feature_extractor(images=image, return_tensors="pt")
        return {k: v.to(self.device) for k, v in inputs.items()}

    def get_person_mask(self, image: Union[str, np.ndarray, Image.Image]) -> Tuple[np.ndarray, Tuple[int, int]]:
        """
        이미지에서 사람 마스크 추출
        """
        if isinstance(image, str):
            image = load_image(image)
        elif isinstance(image, Image.Image):
            image = np.array(image)

        original_size = (image.shape[1], image.shape[0])
        inputs = self.preprocess_image(image)

        with torch.no_grad():
            outputs = self.model(**inputs)
            logits = outputs.logits

        # 마스크 생성
        mask = torch.argmax(logits, dim=1) == self.person_idx
        mask = mask.cpu().numpy()[0].astype(np.float32)  # float32로 변경

        # 마스크 스무딩을 위해 가우시안 블러 적용
        mask = cv2.GaussianBlur(mask, (7, 7), 0)

        # 원본 크기로 리사이즈 (부드러운 보간법 사용)
        mask = cv2.resize(mask, original_size, interpolation=cv2.INTER_LINEAR)

        # 임계값 적용하여 이진화
        mask = (mask > 0.5).astype(np.uint8)

        return mask, original_size

    def segment_person(self,
                       image: Union[str, np.ndarray, Image.Image],
                       enhance: bool = True) -> Tuple[Optional[np.ndarray], np.ndarray, np.ndarray]:
        if isinstance(image, str):
            image = load_image(image)
        elif isinstance(image, Image.Image):
            image = np.array(image)

        # 마스크 추출
        mask, _ = self.get_person_mask(image)

        # 마스크가 비어있는지 확인 (사람이 없는 경우)
        if np.sum(mask) == 0:
            return None, image, mask

        # 마스크 품질 개선
        if enhance:
            mask = self.enhance_mask(mask)

        mask_3ch = np.repeat(mask[..., np.newaxis], 3, axis=2)

        # 사람 영역 추출
        person_image = image * mask_3ch

        # 배경 영역 추출
        background_image = image * (1 - mask_3ch)

        return person_image, background_image, mask

    def enhance_mask(self, mask: np.ndarray) -> np.ndarray:
        """
        마스크 품질 개선을 위한 후처리
        Args:
            mask: 입력 마스크
        Returns:
            개선된 마스크
        """
        # 입력 마스크가 uint8인지 확인
        if mask.dtype != np.uint8:
            mask = mask.astype(np.uint8)

        # 노이즈 제거
        kernel = np.ones((5, 5), np.uint8)
        mask = cv2.morphologyEx(mask, cv2.MORPH_CLOSE, kernel)
        mask = cv2.morphologyEx(mask, cv2.MORPH_OPEN, kernel)

        # 엣지 스무딩
        mask_float = mask.astype(np.float32)
        mask_float = cv2.GaussianBlur(mask_float, (5, 5), 0)
        mask = (mask_float > 0.5).astype(np.uint8)

        # 외곽선 스무딩
        contours, _ = cv2.findContours(mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
        mask_smooth = np.zeros_like(mask)

        if contours:  # contours가 존재할 경우에만 처리
            for contour in contours:
                # 컨투어 길이 계산
                perimeter = cv2.arcLength(contour, True)
                # epsilon 값 계산 (컨투어 길이의 0.2%)
                epsilon = 0.002 * perimeter
                # 컨투어 근사화
                approx = cv2.approxPolyDP(contour, epsilon, True)
                # 근사화된 컨투어 그리기
                cv2.drawContours(mask_smooth, [approx], -1, (1), -1)
        else:
            # contours가 없으면 원본 마스크 반환
            return mask

        # 최종 스무딩
        mask_smooth = cv2.GaussianBlur(mask_smooth.astype(np.float32), (3, 3), 0)
        mask_smooth = (mask_smooth > 0.5).astype(np.uint8)

        return mask_smooth

    # models/person_segmenter.py

    def visualize_segmentation(self,
                               image: Union[str, np.ndarray, Image.Image],
                               alpha: float = 0.5) -> np.ndarray:
        """
        세그멘테이션 결과 시각화
        Args:
            image: 입력 이미지
            alpha: 오버레이 투명도
        Returns:
            시각화된 이미지
        """
        if isinstance(image, str):
            image = load_image(image)
        elif isinstance(image, Image.Image):
            image = np.array(image)

        # segment_person이 이제 3개의 값을 반환하므로 수정
        _, _, mask = self.segment_person(image)

        # 마스크 스무딩
        mask_float = mask.astype(np.float32)
        mask_float = cv2.GaussianBlur(mask_float, (5, 5), 0)

        # 컬러맵 적용
        colored_mask = np.zeros((*mask.shape, 3), dtype=np.uint8)
        colored_mask[mask_float > 0.5] = [0, 255, 0]  # 초록색으로 표시

        # 블렌딩
        result = cv2.addWeighted(image, 1, colored_mask, alpha, 0)

        return result

    def save_results(self,
                     image: Union[str, np.ndarray, Image.Image],
                     save_dir: str,
                     save_visualization: bool = True) -> None:
        """
        세그멘테이션 결과 저장
        Args:
            image: 입력 이미지
            save_dir: 저장 디렉토리
            save_visualization: 시각화 결과 저장 여부
        """
        os.makedirs(save_dir, exist_ok=True)

        # 세그멘테이션 수행
        person_image, background_image, mask = self.segment_person(image)

        # 파일명 설정
        if isinstance(image, str):
            base_name = os.path.splitext(os.path.basename(image))[0]
        else:
            base_name = "segmentation_result"

        # 결과 저장
        # 알파 채널 적용하여 저장
        save_image(person_image, os.path.join(save_dir, f"{base_name}_person.png"), rgb2bgr=True)
        save_image(background_image, os.path.join(save_dir, f"{base_name}_background.png"), rgb2bgr=True)

        if save_visualization:
            vis_result = self.visualize_segmentation(image)
            save_image(vis_result, os.path.join(save_dir, f"{base_name}_visualization.png"))