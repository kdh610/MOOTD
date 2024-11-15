# utils/image_utils.py

import cv2
import numpy as np
from typing import Union, Tuple, Optional
from PIL import Image
import os


def load_image(image_path: str) -> np.ndarray:
    """
    이미지 파일을 로드
    Args:
        image_path: 이미지 파일 경로
    Returns:
        RGB 형식의 numpy 배열
    """
    if not os.path.exists(image_path):
        raise FileNotFoundError(f"Image file not found: {image_path}")

    img = cv2.imread(image_path)
    if img is None:
        raise ValueError(f"Failed to load image: {image_path}")
    return cv2.cvtColor(img, cv2.COLOR_BGR2RGB)


def save_image(image: np.ndarray,
               save_path: str,
               rgb2bgr: bool = True) -> None:
    save_dir = os.path.dirname(save_path)
    if save_dir:
        os.makedirs(save_dir, exist_ok=True)

    # RGBA 이미지인 경우
    if len(image.shape) == 3 and image.shape[2] == 4:
        # PIL을 사용하여 알파 채널 유지하며 저장
        Image.fromarray(image).save(save_path, format='PNG')
    else:
        # 기존 RGB/BGR 이미지 처리
        if rgb2bgr:
            image = cv2.cvtColor(image, cv2.COLOR_RGB2BGR)
        cv2.imwrite(save_path, image)


def resize_image(image: np.ndarray,
                 size: Tuple[int, int],
                 keep_aspect_ratio: bool = True) -> np.ndarray:
    """
    이미지 크기 조정
    Args:
        image: 입력 이미지
        size: (width, height)
        keep_aspect_ratio: 종횡비 유지 여부
    Returns:
        크기가 조정된 이미지
    """
    if keep_aspect_ratio:
        h, w = image.shape[:2]
        target_w, target_h = size

        # 종횡비 계산
        aspect = w / h
        target_aspect = target_w / target_h

        if aspect > target_aspect:
            # 너비에 맞춤
            new_w = target_w
            new_h = int(new_w / aspect)
        else:
            # 높이에 맞춤
            new_h = target_h
            new_w = int(new_h * aspect)

        resized = cv2.resize(image, (new_w, new_h))

        # 패딩 추가
        pad_h = target_h - new_h
        pad_w = target_w - new_w

        top = pad_h // 2
        bottom = pad_h - top
        left = pad_w // 2
        right = pad_w - left

        return cv2.copyMakeBorder(resized, top, bottom, left, right,
                                  cv2.BORDER_CONSTANT, value=[0, 0, 0])
    else:
        return cv2.resize(image, size)


def normalize_image(image: np.ndarray,
                    mean: Optional[list] = None,
                    std: Optional[list] = None) -> np.ndarray:
    """
    이미지 정규화
    Args:
        image: 입력 이미지 (0-255)
        mean: RGB 평균값
        std: RGB 표준편차
    Returns:
        정규화된 이미지
    """
    img = image.astype(np.float32) / 255.0

    if mean is not None and std is not None:
        img = (img - np.array(mean)) / np.array(std)

    return img


def overlay_mask(image: np.ndarray,
                 mask: np.ndarray,
                 alpha: float = 0.5,
                 color: Tuple[int, int, int] = (255, 0, 0)) -> np.ndarray:
    """
    이미지에 마스크를 오버레이
    Args:
        image: 원본 이미지
        mask: 바이너리 마스크
        alpha: 투명도 (0-1)
        color: 마스크 색상 (RGB)
    Returns:
        마스크가 오버레이된 이미지
    """
    mask = mask.astype(bool)
    overlay = image.copy()
    overlay[mask] = color

    output = cv2.addWeighted(image, 1 - alpha, overlay, alpha, 0)
    return output


def create_visualization(images: list,
                         size: Optional[Tuple[int, int]] = None,
                         num_cols: int = 2) -> np.ndarray:
    """
    여러 이미지를 그리드 형태로 시각화
    Args:
        images: 이미지 리스트
        size: 각 이미지 크기 (width, height)
        num_cols: 열 개수
    Returns:
        그리드 형태로 배치된 이미지
    """
    num_images = len(images)
    if num_images == 0:
        return None

    if size:
        images = [cv2.resize(img, size) for img in images]

    num_rows = (num_images + num_cols - 1) // num_cols

    cell_height = images[0].shape[0]
    cell_width = images[0].shape[1]

    vis = np.zeros((cell_height * num_rows, cell_width * num_cols, 3), dtype=np.uint8)

    for idx, image in enumerate(images):
        i = idx // num_cols
        j = idx % num_cols

        if len(image.shape) == 2:  # 그레이스케일 이미지인 경우
            image = cv2.cvtColor(image, cv2.COLOR_GRAY2RGB)

        vis[i * cell_height:(i + 1) * cell_height,
        j * cell_width:(j + 1) * cell_width] = image

    return vis


def convert_to_pil(image: Union[np.ndarray, str]) -> Image.Image:
    """
    이미지를 PIL Image로 변환
    Args:
        image: numpy 배열 또는 이미지 경로
    Returns:
        PIL Image 객체
    """
    if isinstance(image, str):
        return Image.open(image)
    elif isinstance(image, np.ndarray):
        if image.dtype != np.uint8:
            image = (image * 255).astype(np.uint8)
        return Image.fromarray(image)
    elif isinstance(image, Image.Image):
        return image
    else:
        raise TypeError(f"Unsupported image type: {type(image)}")