from fastapi import FastAPI, Request
from routers import pipline_router  # image_router를 import
import time

app = FastAPI()

@app.middleware("http")
async def add_process_time_header(request: Request, call_next):
    start_time = time.time()
    response = await call_next(request)
    process_time = time.time() - start_time
    response.headers["X-Process-Time"] = str(process_time)  # 응답 헤더에 추가
    print(f"Request processed in {process_time} seconds")
    return response

app.include_router(pipline_router.router)