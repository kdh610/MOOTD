# api.py
from fastapi import FastAPI, UploadFile, File
from fastapi.responses import FileResponse
import shutil
import os
from fastapi.responses import JSONResponse
from typing import List, Optional
import cv2
import numpy as np
from pydantic import BaseModel
from models.person_segmenter import PersonSegmenter
from models.edge_detector import EdgeDetector
import tempfile
import base64

from utils.image_utils import save_image

app = FastAPI()

# 모델 초기화
segmenter = PersonSegmenter()
edge_detector = EdgeDetector()

class ImageResponse(BaseModel):
    is_person: bool
    person_edge: Optional[str] = None  # base64 문자열
    background_edge: str  # base64 문자열

def image_to_base64(image):
    """OpenCV 이미지를 base64 문자열로 변환"""
    success, encoded_image = cv2.imencode('.png', image)
    if success:
        return base64.b64encode(encoded_image.tobytes()).decode('utf-8')
    return None

@app.post("/process_image/")
async def process_image(file: UploadFile = File(...)):
    # 업로드된 파일을 메모리에서 읽기
    contents = await file.read()
    nparr = np.frombuffer(contents, np.uint8)
    img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

    try:
        person_image, background_image, mask = segmenter.segment_person(img)

        if person_image is not None:
            # 사람이 감지된 경우
            person_avg, person_fused = edge_detector.detect_edges(
                cv2.cvtColor(person_image, cv2.COLOR_RGB2BGR),
                edge_color=(0, 0, 255)
            )
            person_base64 = image_to_base64(person_fused)

            background_avg, background_fused = edge_detector.detect_edges(
                cv2.cvtColor(background_image, cv2.COLOR_RGB2BGR)
            )
            background_base64 = image_to_base64(background_fused)

            return ImageResponse(
                is_person=True,
                person_edge=person_base64,
                background_edge=background_base64
            )
        else:
            # 사람이 감지되지 않은 경우
            background_avg, background_fused = edge_detector.detect_edges(
                cv2.cvtColor(background_image, cv2.COLOR_RGB2BGR)
            )
            background_base64 = image_to_base64(background_fused)

            return ImageResponse(
                is_person=False,
                background_edge=background_base64
            )

    except Exception as e:
        return JSONResponse(
            status_code=500,
            content={"error": str(e)}
        )