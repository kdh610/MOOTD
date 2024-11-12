from transformers import pipeline
from PIL import Image


# new 버전
def make_caption_list(image_path):
    word_list = []
    words_ch = set()
    
    preposition_lst = ['above', 'below', 'over', 'under', 'down', 'into', 'accross', 'along',
                    'through', 'around', 'behind', 'fornt', 'between', 'among', 'for', 'with', 'from', 'and']

    # 이미지 캡셔닝을 위한 pipeline 생성
    captioning_pipeline = pipeline("image-to-text", model="nlpconnect/vit-gpt2-image-captioning")

    # 이미지 로드
    image = Image.open(image_path)
    if image.mode != "RGB":
        image = image.convert(mode="RGB")


    caption = captioning_pipeline(image)[0]['generated_text']
    print("Caption:", caption)
    # print(type(caption))

    word_list = [ word for word in caption.split() if len(word) > 2 and word not in preposition_lst and not (word in words_ch or words_ch.add(word))] # 풍경에서 2글자 이하인게 있나?

    return word_list

    
# image_path = "pics/tg.jpg"
# make_caption_list(image_path)