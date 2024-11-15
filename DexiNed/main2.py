# main2.py

import os
import traceback
import cv2
from models.person_segmenter import PersonSegmenter
from models.edge_detector import EdgeDetector
from utils.image_utils import save_image
from config.edge_config import EdgeConfig

def main():
   try:
       print("Segformer 모델 초기화 중...")
       segmenter = PersonSegmenter()
       print("DexiNed 모델 초기화 중...")
       edge_detector = EdgeDetector()
       print("모든 모델 초기화 완료")
   except Exception as e:
       print(f"모델 초기화 실패: {str(e)}")
       traceback.print_exc()
       return

   image_path = "data/test2.jpg"
   if not os.path.exists(image_path):
       print(f"이미지 파일을 찾을 수 없습니다: {image_path}")
       return

   output_dir = "segmentation_and_edge_results"
   os.makedirs(output_dir, exist_ok=True)

   try:
       print("세그멘테이션 시작...")
       person_image, background_image, mask = segmenter.segment_person(image_path)
       print("세그멘테이션 완료")

       base_name = os.path.splitext(os.path.basename(image_path))[0]

       print("엣지 검출 시작...")
       if person_image is not None:
           # 사람 이미지 엣지 검출 (빨간색)
           person_avg, person_fused = edge_detector.detect_edges(
               cv2.cvtColor(person_image, cv2.COLOR_RGB2BGR),
               edge_color=(0, 0, 255)  # BGR 형식
           )
           # 결과 저장
           os.makedirs(os.path.join(output_dir, "avg"), exist_ok=True)
           os.makedirs(os.path.join(output_dir, "fused"), exist_ok=True)
           save_image(person_avg, os.path.join(output_dir, "avg", f"{base_name}_person.png"))
           save_image(person_fused, os.path.join(output_dir, "fused", f"{base_name}_person.png"))

       # 배경 이미지 엣지 검출 (기본 검정색)
       background_avg, background_fused = edge_detector.detect_edges(
           cv2.cvtColor(background_image, cv2.COLOR_RGB2BGR)
       )
       save_image(background_avg, os.path.join(output_dir, "avg", f"{base_name}_background.png"))
       save_image(background_fused, os.path.join(output_dir, "fused", f"{base_name}_background.png"))

       print("엣지 검출 완료")

   except Exception as e:
       print(f"처리 중 오류 발생: {str(e)}")
       traceback.print_exc()

if __name__ == "__main__":
   main()