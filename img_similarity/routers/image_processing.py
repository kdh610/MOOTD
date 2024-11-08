from fastapi import APIRouter, File, UploadFile
from fastapi.responses import JSONResponse
from pathlib import Path
import os
from utils.image_captioning import generate_caption_keywords
from utils.meta_clip import generate_metaclip_keywords

# 라우터 생성
router = APIRouter()

# 이미지 저장 디렉토리 설정
UPLOAD_DIR = "uploaded_images"
Path(UPLOAD_DIR).mkdir(exist_ok=True)  # 디렉토리가 없으면 생성

@router.post("/upload-image/")
async def upload_image(file: UploadFile = File(...)):
    # 이미지 저장 경로 설정
    image_path = os.path.join(UPLOAD_DIR, file.filename)
    
    # 이미지 저장
    with open(image_path, "wb") as buffer:
        buffer.write(await file.read())

    # 이미지 캡셔닝을 통해 단어 리스트 생성
    word_list = generate_caption_keywords(image_path)

    # MetaCLIP 키워드 추출
    keywords_list = generate_metaclip_keywords(image_path, word_list)

    # 결과 응답으로 반환
    return JSONResponse(content={"keywords": keywords_list})
