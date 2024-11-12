from fastapi import FastAPI, Request
from routers import image_processing
import time

# FastAPI 인스턴스 생성
app = FastAPI()

# 응답 시간 측정 미들웨어
@app.middleware("http")
async def add_process_time_header(request: Request, call_next):
    start_time = time.time()
    response = await call_next(request)
    process_time = time.time() - start_time
    response.headers["X-Process-Time"] = str(process_time)  # 응답 헤더에 추가
    print(f"Request processed in {process_time} seconds")
    return response

# 라우터 등록
app.include_router(image_processing.router)
