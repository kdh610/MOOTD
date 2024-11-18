from fastapi import FastAPI, UploadFile, File
from fastapi.responses import JSONResponse
import cv2
import numpy as np
from pydantic import BaseModel
from typing import Optional
import base64
import torch
import asyncio
from concurrent.futures import ThreadPoolExecutor
import logging
import os  # 디렉토리 생성을 위해 추가

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class ModelManager:
    _instance = None
    _lock = asyncio.Lock()

    @classmethod
    async def get_instance(cls):
        if not cls._instance:
            async with cls._lock:
                if not cls._instance:
                    cls._instance = cls()
                    await cls._instance.initialize()
        return cls._instance

    def __init__(self):
        self.segmenter = None
        self.edge_detector = None

    async def initialize(self):
        # CUDA 사용 가능 여부 로깅
        logger.info(f"CUDA 사용 가능 여부: {torch.cuda.is_available()}")
        if torch.cuda.is_available():
            logger.info(f"사용 중인 GPU: {torch.cuda.get_device_name(0)}")

        from models.person_segmenter import PersonSegmenter
        from models.edge_detector import EdgeDetector

        self.segmenter = PersonSegmenter()
        self.edge_detector = EdgeDetector()
        logger.info("모델 초기화 완료")


class ImageResponse(BaseModel):
    is_person: bool
    person_edge: Optional[str] = None
    background_edge: str


app = FastAPI()
thread_pool = ThreadPoolExecutor(max_workers=3)


def ensure_dir(dir_path):
    """디렉토리가 없으면 생성"""
    if not os.path.exists(dir_path):
        os.makedirs(dir_path)


def preprocess_image(img, max_size=1024):
    """이미지 전처리: 비율을 유지하면서 크기 조정"""
    height, width = img.shape[:2]

    # 비율 계산
    aspect_ratio = width / height

    if height > max_size or width > max_size:
        if width > height:
            new_width = max_size
            new_height = int(max_size / aspect_ratio)
        else:
            new_height = max_size
            new_width = int(max_size * aspect_ratio)

        img = cv2.resize(img, (new_width, new_height))
        logger.info(f"이미지 리사이즈: {width}x{height} -> {new_width}x{new_height} (비율: {aspect_ratio:.2f})")

    return img


def image_to_base64(image):
    """OpenCV 이미지를 base64 문자열로 변환"""
    success, encoded_image = cv2.imencode('.png', image)
    if success:
        return base64.b64encode(encoded_image.tobytes()).decode('utf-8')
    return None


async def process_image_task(img, segmenter, edge_detector, filename):
    """이미지 처리 작업을 수행하는 비동기 함수"""
    try:
        # GPU 메모리 캐시 정리
        torch.cuda.empty_cache()

        start_time = asyncio.get_event_loop().time()

        # 임시 저장 디렉토리 생성
        temp_dir = "process_img_temp"
        ensure_dir(temp_dir)

        # 이미지 전처리
        img = preprocess_image(img)
        preprocess_time = asyncio.get_event_loop().time() - start_time
        logger.info(f"전처리 시간: {preprocess_time:.2f}초")

        # 세그멘테이션 수행
        segment_start = asyncio.get_event_loop().time()
        person_image, background_image, mask = segmenter.segment_person(img)
        segment_time = asyncio.get_event_loop().time() - segment_start
        logger.info(f"세그멘테이션 시간: {segment_time:.2f}초")

        results = {}
        edge_start = asyncio.get_event_loop().time()
        if person_image is not None:
            # 사람이 감지된 경우
            person_avg, person_fused = edge_detector.detect_edges(
                cv2.cvtColor(person_image, cv2.COLOR_RGB2BGR),
                edge_color=(0, 0, 255)  # 빨간색 (BGR)
            )
            background_avg, background_fused = edge_detector.detect_edges(
                cv2.cvtColor(background_image, cv2.COLOR_RGB2BGR),
                edge_color=(255, 0, 0)  # 파란색 (BGR)
            )

            # 결과 이미지 저장
            cv2.imwrite(os.path.join(temp_dir, f"{filename}_person_edge.png"), person_fused)
            cv2.imwrite(os.path.join(temp_dir, f"{filename}_background_edge.png"), background_fused)

            # 비동기로 base64 인코딩
            loop = asyncio.get_event_loop()
            person_base64 = await loop.run_in_executor(thread_pool, image_to_base64, person_fused)
            background_base64 = await loop.run_in_executor(thread_pool, image_to_base64, background_fused)

            results = {
                "is_person": True,
                "person_edge": person_base64,
                "background_edge": background_base64
            }
        else:
            # 사람이 감지되지 않은 경우
            background_avg, background_fused = edge_detector.detect_edges(
                cv2.cvtColor(background_image, cv2.COLOR_RGB2BGR),
                edge_color=(255, 0, 0)  # 파란색 (BGR)
            )

            # 결과 이미지 저장
            cv2.imwrite(os.path.join(temp_dir, f"{filename}_background_edge.png"), background_fused)

            # 비동기로 base64 인코딩
            loop = asyncio.get_event_loop()
            background_base64 = await loop.run_in_executor(thread_pool, image_to_base64, background_fused)

            results = {
                "is_person": False,
                "background_edge": background_base64
            }

        edge_time = asyncio.get_event_loop().time() - edge_start
        logger.info(f"엣지 검출 시간: {edge_time:.2f}초")

        # GPU 메모리 캐시 정리
        torch.cuda.empty_cache()

        return results

    except Exception as e:
        logger.error(f"이미지 처리 중 오류 발생: {str(e)}")
        raise e


@app.on_event("startup")
async def startup_event():
    await ModelManager.get_instance()
    logger.info("FastAPI 서버 시작 및 모델 초기화 완료")


@app.on_event("shutdown")
async def shutdown_event():
    thread_pool.shutdown(wait=True)
    logger.info("서버 종료")


@app.post("/process_image/")
async def process_image(file: UploadFile = File(...)):
    try:
        start_time = asyncio.get_event_loop().time()

        # 파일 읽기
        contents = await file.read()
        file_size_kb = len(contents) / 1024
        logger.info(f"입력 파일 크기: {file_size_kb:.2f}KB")

        # 파일명 추출 (확장자 제외)
        filename = os.path.splitext(file.filename)[0]

        # 이미지 디코딩을 스레드 풀에서 실행
        loop = asyncio.get_event_loop()
        nparr = await loop.run_in_executor(thread_pool,
                                           lambda: np.frombuffer(contents, np.uint8))
        img = await loop.run_in_executor(thread_pool,
                                         lambda: cv2.imdecode(nparr, cv2.IMREAD_COLOR))

        if img is None:
            return JSONResponse(
                status_code=400,
                content={"error": "Invalid image file"}
            )

        logger.info(f"원본 이미지 크기: {img.shape}")

        # 모델 가져오기
        model_manager = await ModelManager.get_instance()

        # 이미지 처리 (파일명 전달)
        results = await process_image_task(
            img,
            model_manager.segmenter,
            model_manager.edge_detector,
            filename
        )

        # 처리 시간 계산 및 로깅
        end_time = asyncio.get_event_loop().time()
        processing_time = end_time - start_time
        logger.info(f"이미지 처리 완료. 총 소요 시간: {processing_time:.2f}초")

        return ImageResponse(**results)

    except Exception as e:
        logger.error(f"처리 중 오류 발생: {str(e)}")
        return JSONResponse(
            status_code=500,
            content={"error": f"처리 중 오류가 발생했습니다: {str(e)}"}
        )


@app.get("/health")
async def health_check():
    return {
        "status": "healthy",
        "cuda_available": torch.cuda.is_available(),
        "gpu_name": torch.cuda.get_device_name(0) if torch.cuda.is_available() else None
    }
