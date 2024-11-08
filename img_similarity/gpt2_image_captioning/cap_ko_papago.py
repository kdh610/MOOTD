
from transformers import VisionEncoderDecoderModel, ViTImageProcessor, AutoTokenizer, MarianMTModel, MarianTokenizer
import torch
from PIL import Image
import matplotlib.pyplot as plt

# 모델 및 전처리 도구 설정
caption_model = VisionEncoderDecoderModel.from_pretrained("nlpconnect/vit-gpt2-image-captioning")
caption_feature_extractor = ViTImageProcessor.from_pretrained("nlpconnect/vit-gpt2-image-captioning")
caption_tokenizer = AutoTokenizer.from_pretrained("nlpconnect/vit-gpt2-image-captioning")

device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
caption_model.to(device)

max_length = 16
num_beams = 4
gen_kwargs = {"max_length": max_length, "num_beams": num_beams}

# MarianMT (영어 -> 한국어)
model_name = "obokkkk/opus-mt-ko-en-finetuned-en-to-ko"
# token = "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"
# token = "YOUR_HUGGINGFACE_TOKEN"
translation_model = MarianMTModel.from_pretrained(model_name)
translation_tokenizer = MarianTokenizer.from_pretrained(model_name)
translation_model.to("cuda" if torch.cuda.is_available() else "cpu")


def generate_eng_caption(image_path): 
    
    i_image = Image.open(image_path)
    if i_image.mode != "RGB":
        i_image = i_image.convert(mode="RGB")

    pixel_values = caption_feature_extractor(images=[i_image], return_tensors="pt").pixel_values
    pixel_values = pixel_values.to(device)

    output_ids = caption_model.generate(pixel_values, max_length=16, num_beams=4)
    caption = caption_tokenizer.decode(output_ids[0], skip_special_tokens=True)
    return caption


# eng -> kor
def translate_to_korean(eng_text):
    inputs = translation_tokenizer(eng_text, return_tensors="pt").input_ids.to(translation_model.device)
    translated_ids = translation_model.generate(inputs, max_length=40, num_beams=4)
    korean_text = translation_tokenizer.decode(translated_ids[0], skip_special_tokens=True)
    return korean_text


# 이미지 & 캡션 시각화
def show_image_with_caption(image_path):
    eng_caption = generate_eng_caption(image_path)
    kor_caption = translate_to_korean(eng_caption)
    # print('CAPTION: ', caption)

    image = Image.open(image_path)
    plt.rcParams['font.family'] = 'Malgun Gothic'
    plt.rcParams['axes.unicode_minus'] = False

    plt.figure(figsize=(8, 8))
    plt.imshow(image)
    plt.axis("off")  # 축 제거
    plt.title(kor_caption, fontsize=15, color="blue", weight="bold", loc="center")
    plt.show()


image_path = 'pics/bada1.jpg'  # 테스트 이미지 경로 입력
show_image_with_caption(image_path)
eng_caption = generate_eng_caption(image_path)
korean_caption = translate_to_korean(eng_caption)
print('KOR CAPTION: ', korean_caption)