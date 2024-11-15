# models/edge_detector.py

import torch
import torch.nn as nn
import numpy as np
import cv2
import os
from typing import Union, List, Tuple, Optional
from PIL import Image
from config.edge_config import EdgeConfig
from model import DexiNed


class EdgeDetector:
    def __init__(self,
                 checkpoint_path: str = EdgeConfig.CHECKPOINT_PATH,
                 device: str = EdgeConfig.DEVICE):
        """
        EdgeDetector 클래스 초기화
        Args:
            checkpoint_path: DexiNed 모델 체크포인트 경로
            device: 실행 디바이스 ('cuda' or 'cpu')
        """
        self.device = device if device else ('cuda' if torch.cuda.is_available() else 'cpu')

        # DexiNed 모델 초기화
        print("DexiNed 모델 로딩 중...")
        self.model = DexiNed().to(self.device)

        # 체크포인트 로드
        if not os.path.isfile(checkpoint_path):
            raise FileNotFoundError(f"Checkpoint file not found: {checkpoint_path}")

        print(f"체크포인트 로딩 중: {checkpoint_path}")
        self.model.load_state_dict(torch.load(checkpoint_path, map_location=self.device))
        self.model.eval()
        print("DexiNed 모델 로딩 완료")

        # 전처리 파라미터
        self.mean_pixel_values = [103.939, 116.779, 123.68, 137.86]
        self.img_size = (352, 352)

    def preprocess_image(self, image: Union[str, np.ndarray, Image.Image]) -> torch.Tensor:
        """
        DexiNed 모델에 맞게 이미지 전처리
        Args:
            image: 입력 이미지 (파일 경로, numpy 배열, 또는 PIL Image)
        Returns:
            전처리된 이미지 텐서
        """
        if isinstance(image, str):
            image = cv2.imread(image)
        elif isinstance(image, Image.Image):
            image = np.array(image)

        # BGR로 변환 (만약 RGB라면)
        if len(image.shape) == 3 and image.shape[2] == 3:
            image = cv2.cvtColor(image, cv2.COLOR_RGB2BGR)

        # 크기 조정
        image = cv2.resize(image, self.img_size)

        # 전처리
        image = image.astype(np.float32)
        image -= self.mean_pixel_values[:3]

        # 차원 변경 (HWC -> CHW)
        image = image.transpose((2, 0, 1))
        image = torch.from_numpy(image).unsqueeze(0)
        return image.to(self.device)

    def detect_edges(self,
                     image: Union[str, np.ndarray, Image.Image],
                     edge_color: Tuple[int, int, int] = None) -> Tuple[np.ndarray, np.ndarray]:
        """
        이미지에서 엣지 검출
        Args:
            image: 입력 이미지
        Returns:
            avg_edge_map: 모든 레이어의 평균 엣지맵
            fused_edge_map: 마지막 레이어의 엣지맵
        """
        if isinstance(image, np.ndarray):
            original_size = (image.shape[1], image.shape[0])
        elif isinstance(image, Image.Image):
            original_size = image.size
        else:
            img = cv2.imread(image)
            original_size = (img.shape[1], img.shape[0])

            # 이미지 전처리
        img_tensor = self.preprocess_image(image)

        with torch.no_grad():
            preds_list = self.model(img_tensor)

            # 엣지맵 생성
            edge_maps = []
            for pred in preds_list[:-1]:
                tmp = torch.sigmoid(pred).cpu().detach().numpy()
                tmp = tmp[0, 0]
                tmp = cv2.resize(tmp, original_size)
                tmp = np.uint8(self.image_normalization(tmp))
                tmp = cv2.bitwise_not(tmp)

                # RGBA로 변환
                tmp_rgba = np.zeros((*tmp.shape, 4), dtype=np.uint8)
                if edge_color is not None:
                    # 컬러 엣지 생성
                    color_mask = np.zeros((*tmp.shape, 3), dtype=np.uint8)
                    color_mask[:] = edge_color
                    # 엣지 부분만 색상 적용
                    tmp_rgba[..., :3] = np.where(tmp[..., None] < 128, color_mask, [255, 255, 255])
                else:
                    tmp_rgba[..., :3] = cv2.cvtColor(tmp, cv2.COLOR_GRAY2BGR)

                tmp_rgba[..., 3] = np.where(tmp >= 128, 0, 255)
                edge_maps.append(tmp_rgba)

            # avg 버전
            avg_edge_map = np.uint8(np.mean(edge_maps, axis=0))

            # fused 버전도 동일하게 적용
            fused = torch.sigmoid(preds_list[-1]).cpu().detach().numpy()
            fused = fused[0, 0]
            fused = cv2.resize(fused, original_size)
            fused_edge_map = np.uint8(self.image_normalization(fused))
            fused_edge_map = cv2.bitwise_not(fused_edge_map)

            fused_rgba = np.zeros((*fused_edge_map.shape, 4), dtype=np.uint8)
            if edge_color is not None:
                color_mask = np.zeros((*fused_edge_map.shape, 3), dtype=np.uint8)
                color_mask[:] = edge_color
                fused_rgba[..., :3] = np.where(fused_edge_map[..., None] < 128, color_mask, [255, 255, 255])
            else:
                fused_rgba[..., :3] = cv2.cvtColor(fused_edge_map, cv2.COLOR_GRAY2BGR)

            fused_rgba[..., 3] = np.where(fused_edge_map >= 128, 0, 255)

            return avg_edge_map, fused_rgba

    def image_normalization(self, img, img_min=0, img_max=255, epsilon=1e-12):
        """이미지 정규화"""
        img = np.float32(img)
        img = (img - np.min(img)) * (img_max - img_min) / \
              ((np.max(img) - np.min(img)) + epsilon) + img_min
        return img

    def detect_edges_batch(self,
                           images: List[Union[str, np.ndarray, Image.Image]],
                           batch_size: int = 8,
                           mode: str = 'fused') -> List[np.ndarray]:
        """
        배치 단위로 엣지 검출
        Args:
            images: 입력 이미지 리스트
            batch_size: 배치 크기
            mode: 'avg' 또는 'fused'
        Returns:
            엣지 맵 리스트
        """
        results = []
        for i in range(0, len(images), batch_size):
            batch = images[i:i + batch_size]
            batch_results = []
            for img in batch:
                avg, fused = self.detect_edges(img)
                batch_results.append(avg if mode == 'avg' else fused)
            results.extend(batch_results)
        return results

    def postprocess_edge_map(self, edge_map: np.ndarray) -> np.ndarray:
        """
        엣지맵 후처리 - DexiNed 스타일
        Args:
            edge_map: 입력 엣지맵
        Returns:
            후처리된 엣지맵
        """
        # 가우시안 블러로 부드럽게
        edge_map = cv2.GaussianBlur(edge_map, (3, 3), 0)

        # 대비 향상
        edge_map = cv2.convertScaleAbs(edge_map, alpha=1.5, beta=0)

        return edge_map

    def enhance_edges(self,
                      edge_map: np.ndarray,
                      kernel_size: int = 3) -> np.ndarray:
        """
        엣지 품질 개선
        Args:
            edge_map: 입력 엣지맵
            kernel_size: 모폴로지 연산 커널 크기
        Returns:
            개선된 엣지맵
        """
        # 가우시안 블러로 부드럽게
        edge_map = cv2.GaussianBlur(edge_map, (3, 3), 0)

        # 대비 향상
        edge_map = cv2.convertScaleAbs(edge_map, alpha=1.5, beta=0)

        return edge_map

    def visualize_edges(self,
                        image: Union[str, np.ndarray, Image.Image],
                        edge_map: Optional[np.ndarray] = None,
                        alpha: float = 0.7) -> np.ndarray:
        """
        엣지 검출 결과 시각화
        Args:
            image: 원본 이미지
            edge_map: 엣지맵 (None인 경우 자동 검출)
            alpha: 합성 비율
        Returns:
            시각화된 이미지
        """
        if isinstance(image, str):
            image = cv2.imread(image)
            image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
        elif isinstance(image, Image.Image):
            image = np.array(image)

        if edge_map is None:
            edge_map = self.detect_edges(image)

        # 엣지맵을 RGB로 변환
        edge_map_rgb = cv2.cvtColor(edge_map, cv2.COLOR_GRAY2RGB)

        # 합성
        visualization = cv2.addWeighted(image, 1 - alpha, edge_map_rgb, alpha, 0)
        return visualization

    def save_results(self,
                     image: Union[str, np.ndarray, Image.Image],
                     save_dir: str,
                     base_name: Optional[str] = None) -> None:
        os.makedirs(save_dir, exist_ok=True)

        if base_name is None:
            if isinstance(image, str):
                base_name = os.path.splitext(os.path.basename(image))[0]
            else:
                base_name = "edge_result"

        avg_edge_map, fused_edge_map = self.detect_edges(image)

        avg_dir = os.path.join(save_dir, 'avg')
        fused_dir = os.path.join(save_dir, 'fused')
        os.makedirs(avg_dir, exist_ok=True)
        os.makedirs(fused_dir, exist_ok=True)

        # OpenCV에서 PIL Image로 변환하여 저장
        avg_pil = Image.fromarray(avg_edge_map)
        fused_pil = Image.fromarray(fused_edge_map)

        # PNG 포맷으로 알파 채널 유지하여 저장
        avg_pil.save(os.path.join(avg_dir, f"{base_name}.png"), "PNG", optimize=True)
        fused_pil.save(os.path.join(fused_dir, f"{base_name}.png"), "PNG", optimize=True)