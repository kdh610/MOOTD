from meta_clip.metaclip_utils import metaclip_res
from gpt2_image_captioning.cap_pipline import make_caption_list
from transformers import pipeline
from PIL import Image

# 이미지 캡셔닝을 위한 pipeline 생성
captioning_pipeline = pipeline("image-to-text", model="nlpconnect/vit-gpt2-image-captioning")

def pic_upload(image_path):
    
    words = []
    keywords_lst = []

    # img_captioning
    # 단어 구분 리스트 생성 ~~
    words = make_caption_list(image_path)

    print(words)

    keywords_lst = metaclip_res(image_path, words)

    return keywords_lst



if __name__ == '__main__':

    image_path = "meta_clip/docs/bada1.jpg"
    # image_path = "meta_clip/docs/bada3.jpg" # 이미지 경로 어케 설정?

    keywords = []

    keywords = pic_upload(image_path)
    
    
