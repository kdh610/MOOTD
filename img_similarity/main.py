from meta_clip.metaclip_utils import metaclip_res
from gpt2_image_captioning.captioning_utils import show_image_with_caption, make_caption_list
from PIL import Image
import matplotlib.pyplot as plt

def pic_save(image_path):
    
    words = []
    keywords_lst = []

    # img_captioning
    # 단어 구분 리스트 생성 ~~
    words = make_caption_list(image_path)

    keywords_lst = metaclip_res(image_path, words)

    return keywords_lst



if __name__ == '__main__':

    image_path = "meta_clip/docs/bada3.jpg" # 이미지 경로 어케 설정?

    keywords = []

    keywords = pic_save(image_path)
    
