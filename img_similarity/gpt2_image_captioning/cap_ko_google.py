
from transformers import VisionEncoderDecoderModel, ViTImageProcessor, AutoTokenizer
import torch
from PIL import Image
import matplotlib.pyplot as plt
from googletrans import Translator

# 모델과 전처리 도구 로드
model = VisionEncoderDecoderModel.from_pretrained("nlpconnect/vit-gpt2-image-captioning")
feature_extractor = ViTImageProcessor.from_pretrained("nlpconnect/vit-gpt2-image-captioning")
tokenizer = AutoTokenizer.from_pretrained("nlpconnect/vit-gpt2-image-captioning")

device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
model.to(device)

max_length = 16
num_beams = 4
gen_kwargs = {"max_length": max_length, "num_beams": num_beams}


def predict_caption(image_path): 
    
    i_image = Image.open(image_path)
    if i_image.mode != "RGB":
        i_image = i_image.convert(mode="RGB")

    pixel_values = feature_extractor(images=[i_image], return_tensors="pt").pixel_values
    pixel_values = pixel_values.to(device)

    output_ids = model.generate(pixel_values, max_length=16, num_beams=4)
    caption = tokenizer.decode(output_ids[0], skip_special_tokens=True)
    return caption


# 이미지와 캡션을 시각화
def show_image_with_caption(image_path):
    caption = predict_caption_korean(image_path)
    # print('CAPTION: ', caption)

    image = Image.open(image_path)
    plt.rcParams['font.family'] = 'Malgun Gothic'
    plt.rcParams['axes.unicode_minus'] = False

    plt.figure(figsize=(8, 8))
    plt.imshow(image)
    plt.axis("off")  # 축 제거
    plt.title(caption, fontsize=15, color="blue", weight="bold", loc="center")
    plt.show()


# google 한국어 API 이용
def predict_caption_korean(image_path):
    caption = predict_caption(image_path)
    print('ENG CAPTION: ', caption)  
    translator = Translator()
    translated_caption = translator.translate(caption, dest='ko').text
    return translated_caption

image_path = 'pics/bada1.jpg'  # 테스트 이미지 경로 입력
show_image_with_caption(image_path)
korean_caption = predict_caption_korean(image_path)
print('KO CAPTION: ', korean_caption)