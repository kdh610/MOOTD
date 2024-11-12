# from gpt2_image_captioning.captioning_utils import make_caption_list
from gpt2_image_captioning.cap_pipline import make_caption_list

def generate_caption_keywords(image_path):
    # 이미지에서 단어 리스트 생성
    words = make_caption_list(image_path)
    return words
