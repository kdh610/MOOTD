from fastapi import FastAPI
from routers import image_processing

# FastAPI 인스턴스 생성
app = FastAPI()

# 라우터 등록
app.include_router(image_processing.router)
