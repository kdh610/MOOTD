FROM python:3.10-slim

WORKDIR /app

# pip 업데이트
RUN pip install --upgrade pip

# 필요한 Python 패키지 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 애플리케이션 코드 복사
COPY . .

# 포트 설정
EXPOSE 3000

# 서버 실행
CMD ["uvicorn", "api:app", "--host", "0.0.0.0", "--port", "3000"]

