import os
import sys
import cv2
import random
import numpy as np
import matplotlib.pyplot as plt
from mrcnn import utils
from mrcnn import model as modellib
from mrcnn.config import Config
from mrcnn import visualize
from fastapi import FastAPI, File, UploadFile
from fastapi.responses import FileResponse

app = FastAPI()

# Mask R-CNN 루트 경로 설정
ROOT_DIR = os.path.abspath("../processing_server/")

# 모델 가중치 파일 경로
COCO_MODEL_PATH = os.path.join(ROOT_DIR, "mask_rcnn_coco.h5")

class InferenceConfig(Config):
    NAME = "coco_inference"
    GPU_COUNT = 1
    IMAGES_PER_GPU = 1
    NUM_CLASSES = 1 + 80

config = InferenceConfig()

# Mask R-CNN 모델 로드
model = modellib.MaskRCNN(mode="inference", model_dir=ROOT_DIR, config=config)
model.load_weights(COCO_MODEL_PATH, by_name=True)


# 이미지 업로드 후 마스킹 처리하는 엔드포인트
@app.post("/process_image/")
async def process_image(file: UploadFile = File(...)):
    # 이미지 로드
    contents = await file.read()
    np_arr = np.frombuffer(contents, np.uint8)
    image = cv2.imdecode(np_arr, cv2.IMREAD_COLOR)

    # 사람 클래스 마스크 추출 및 적용
    results = model.detect([image], verbose=1)
    r = results[0]

    # 사람 클래스의 마스크 추출 및 마스킹 처리
    person_class_id = 1  # COCO에서 사람 클래스 ID는 1
    mask = r['masks'][:, :, r['class_ids'] == person_class_id]

    # 블러 처리를 위한 마스크 생성
    combined_mask = np.zeros(image.shape[:2], dtype=np.uint8)
    for i in range(mask.shape[2]):
        combined_mask = np.logical_or(combined_mask, mask[:, :, i])
    combined_mask = combined_mask.astype(np.uint8) * 255  # 0과 255로 구성된 마스크

    # 블러 강도 동적 조정
    height, width = image.shape[:2]
    blur_kernel_size = max(101, int(min(height, width) / 10) | 1)  # 최소 51 이상, 크기에 따라 동적 조정
    blurred_image = cv2.GaussianBlur(image, (blur_kernel_size, blur_kernel_size), 0)

    # 원본 이미지에 블러 효과 적용
    masked_image = image.copy()
    masked_image[combined_mask == 255] = blurred_image[combined_mask == 255]

    # 경계 부드럽게 하기 (Optional: Morphological operations)
    kernel = np.ones((7, 7), np.uint8)
    expanded_mask = cv2.dilate(combined_mask, kernel, iterations=1)  # 마스크 확장
    combined_mask = cv2.morphologyEx(expanded_mask, cv2.MORPH_CLOSE, kernel)

    # 블러 처리
    masked_image[combined_mask == 255] = cv2.GaussianBlur(image, (blur_kernel_size, blur_kernel_size), 0)[combined_mask == 255]

    # 원본 이미지에 블러 효과 적용
    masked_image = image.copy()
    masked_image[combined_mask == 255] = blurred_image[combined_mask == 255]

    # 결과 이미지 저장
    result_path = "masked_result.jpg"
    cv2.imwrite(result_path, masked_image)

    return FileResponse(result_path, media_type="image/jpeg")
