# config/edge_config.py
import os

import torch

# 프로젝트 루트 디렉토리 경로 설정
ROOT_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


class EdgeConfig:
    # 모델 설정
    CHECKPOINT_PATH = os.path.join(ROOT_DIR, "checkpoints", "BIPED", "10", "10_model.pth")
    IMAGE_SIZE = (352, 352)
    DEVICE = "cuda" if torch.cuda.is_available() else "cpu"

    # 전처리 설정
    MEAN_PIXEL_VALUES = [103.939, 116.779, 123.68, 137.86]

    # 검출 설정
    EDGE_THRESHOLD = 0.5  # 엣지 검출 임계값
    APPLY_NMS = True  # Non-maximum suppression 적용 여부

    # 배치 처리 설정
    BATCH_SIZE = 8  # 배치 크기

    # 시각화 설정
    VISUALIZATION_ALPHA = 0.7  # 시각화시 엣지와 원본 이미지 합성 비율

    # 추가 설정
    KERNEL_SIZE = 3  # 모폴로지 연산을 위한 커널 크기