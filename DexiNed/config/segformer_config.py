# config/segformer_config.py
class SegformerConfig:
    # Segformer 모델 설정
    MODEL_NAME = "nvidia/segformer-b0-finetuned-ade-512-512"
    DEVICE = "cuda"  # or "cpu"

    # 이미지 처리 설정
    IMAGE_SIZE = (512, 512)
    BATCH_SIZE = 4