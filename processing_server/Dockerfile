# Python 베이스 이미지 사용
FROM python:3.7.16-slim

# 작업 디렉토리 설정
WORKDIR /app

# 필수 패키지 설치
RUN apt-get update && apt-get install -y git\
    gcc \
    g++ \
    build-essential \
    libpq-dev \
    libgl1-mesa-glx \
    libglib2.0-0 \ 
    curl \
    && rm -rf /var/lib/apt/lists/*

# CUDA를 사용하지 않도록 설정
ENV CUDA_VISIBLE_DEVICES=""

# 의존성 파일 복사
COPY requirements.txt .

COPY mask_rcnn_coco.h5 /processing_server/mask_rcnn_coco.h5

# 의존성 설치
RUN pip install --no-cache-dir -r requirements.txt

RUN pip install certifi
RUN pip install git+https://github.com/matterport/Mask_RCNN.git



COPY . .


# FastAPI 애플리케이션 실행
CMD ["uvicorn", "mask_rcnn:app", "--host", "0.0.0.0", "--port", "8000"]
