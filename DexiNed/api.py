# api.py
from fastapi import FastAPI, UploadFile, File
from fastapi.responses import FileResponse
import shutil
import os
from typing import List, Optional
import cv2
import numpy as np
from pydantic import BaseModel
from models.person_segmenter import PersonSegmenter
from models.edge_detector import EdgeDetector
import tempfile

from utils.image_utils import save_image

app = FastAPI()

# 모델 초기화
segmenter = PersonSegmenter()
edge_detector = EdgeDetector()


class EdgeResponse(BaseModel):
    is_person: bool
    person_edge_filename: Optional[str] = None
    background_edge_filename: str


@app.post("/process_image/")
async def process_image(file: UploadFile = File(...)):
    with tempfile.NamedTemporaryFile(delete=False, suffix='.png') as tmp:
        shutil.copyfileobj(file.file, tmp)
        tmp_path = tmp.name

    try:
        person_image, background_image, mask = segmenter.segment_person(tmp_path)

        output_dir = "processed_images"
        os.makedirs(output_dir, exist_ok=True)

        base_name = f"result_{os.path.basename(tmp_path)}"

        if person_image is not None:
            person_filename = f"person_{base_name}"
            background_filename = f"background_{base_name}"

            person_avg, person_fused = edge_detector.detect_edges(
                cv2.cvtColor(person_image, cv2.COLOR_RGB2BGR),
                edge_color=(0, 0, 255)
            )
            save_image(person_fused, os.path.join(output_dir, person_filename))

            background_avg, background_fused = edge_detector.detect_edges(
                cv2.cvtColor(background_image, cv2.COLOR_RGB2BGR)
            )
            save_image(background_fused, os.path.join(output_dir, background_filename))

            return EdgeResponse(
                is_person=True,
                person_edge_filename=person_filename,
                background_edge_filename=background_filename
            )
        else:
            background_filename = f"background_{base_name}"
            background_avg, background_fused = edge_detector.detect_edges(
                cv2.cvtColor(background_image, cv2.COLOR_RGB2BGR)
            )
            save_image(background_fused, os.path.join(output_dir, background_filename))

            return EdgeResponse(
                is_person=False,
                background_edge_filename=background_filename
            )

    finally:
        os.unlink(tmp_path)


@app.get("/download/{filename}")
async def download_file(filename: str):
    file_path = f"processed_images/{filename}"
    if os.path.exists(file_path):
        return FileResponse(
            path=file_path,
            filename=filename,
            media_type='image/png',
            headers={"Content-Disposition": f"attachment; filename={filename}"}
        )
    return {"error": "File not found"}