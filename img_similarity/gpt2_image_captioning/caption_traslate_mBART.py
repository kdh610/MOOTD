
from transformers import VisionEncoderDecoderModel, ViTImageProcessor, AutoTokenizer
from asian_bart import AsianBartForConditionalGeneration, AsianBartTokenizer  # asian_bart 모델 import
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

# asian-BART 모델과 토크나이저 로드
model_name = "hyunwoongko/asian-bart-ecjk"
translation_model = AsianBartForConditionalGeneration.from_pretrained(model_name)
translation_tokenizer = AsianBartTokenizer.from_pretrained(model_name)
# translation_model.to(device)
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
translation_model.to(device)


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
def translate_to_korean(text):
    # 입력 텍스트에 언어 코드 추가
    inputs = translation_tokenizer(
        text,
        src_lang="en_XX",
        tgt_lang="ko_KR",
        return_tensors="pt",
        padding=True
    ).to(device)

    # attention_mask 추가
    input_ids = inputs["input_ids"]
    attention_mask = inputs["attention_mask"]

    # 번역 수행
    outputs = translation_model.generate(
        input_ids=input_ids,
        attention_mask=attention_mask,
        max_length=50,
        decoder_start_token_id=translation_tokenizer.lang_code_to_id["ko_KR"]
    )

    # 번역 결과 디코딩
    korean_text = translation_tokenizer.decode(outputs[0], skip_special_tokens=True)
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