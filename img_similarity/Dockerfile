# Python 3.9 슬림 이미지 사용
FROM python:3.9-slim

# 작업 디렉토리 설정
WORKDIR /app

# 시스템 의존성 설치 (예: numpy, opencv 및 git 등 필요한 라이브러리 설치)
RUN apt-get update && apt-get install -y \
    gcc \
    git \
    libopencv-dev \
    && rm -rf /var/lib/apt/lists/*

# requirements.txt 파일을 복사하고, 필요한 패키지 설치
COPY requirements.txt .

RUN pip install --no-cache-dir -r requirements.txt

# 애플리케이션 소스 파일을 복사
COPY . .

# FastAPI 서버 실행 명령어 설정
CMD ["uvicorn", "mainapp:app", "--host", "0.0.0.0", "--port", "80"]
