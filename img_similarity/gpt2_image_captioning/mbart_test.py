from transformers import VisionEncoderDecoderModel, ViTImageProcessor, AutoTokenizer
from transformers import MBartForConditionalGeneration, MBart50TokenizerFast
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

# 번역 모델과 토크나이저 로드
translation_model_name = "facebook/mbart-large-50-many-to-many-mmt"
translation_model = MBartForConditionalGeneration.from_pretrained(translation_model_name)
translation_tokenizer = MBart50TokenizerFast.from_pretrained(translation_model_name)

# 번역 모델에서 사용할 언어 코드 설정
src_lang = "en_XX"  # 영어
tgt_lang = "ko_KR"  # 한국어
translation_tokenizer.src_lang = src_lang
translation_model.to(device)

# 영어 캡션 생성 함수
def generate_eng_caption(image_path):
    i_image = Image.open(image_path)
    if i_image.mode != "RGB":
        i_image = i_image.convert(mode="RGB")

    pixel_values = caption_feature_extractor(images=[i_image], return_tensors="pt").pixel_values
    pixel_values = pixel_values.to(device)

    output_ids = caption_model.generate(pixel_values, **gen_kwargs)
    caption = caption_tokenizer.decode(output_ids[0], skip_special_tokens=True)
    return caption

# 영어 -> 한국어 번역 함수
def translate_to_korean(text):
    # 입력 텍스트를 토큰화하여 텐서로 변환
    inputs = translation_tokenizer(
        text, 
        return_tensors="pt"
    ).to(device)

    outputs = translation_model.generate(
        input_ids=inputs["input_ids"],
        attention_mask=inputs["attention_mask"],
        max_length=50,
        forced_bos_token_id=translation_tokenizer.lang_code_to_id[tgt_lang]  # 대상 언어 코드 설정
    )
    
    # 번역 결과 디코딩
    korean_text = translation_tokenizer.decode(outputs[0], skip_special_tokens=True)
    return korean_text

# 이미지와 영어/한국어 캡션 시각화 함수
def show_image_with_captions(image_path):
    eng_caption = generate_eng_caption(image_path)
    kor_caption = translate_to_korean(eng_caption)

    image = Image.open(image_path)
    plt.rcParams['font.family'] = 'Malgun Gothic'  # 한글 폰트 설정
    plt.rcParams['axes.unicode_minus'] = False

    plt.figure(figsize=(8, 8))
    plt.imshow(image)
    plt.axis("off")  # 축 제거
    
    # 영어 캡션과 한국어 캡션을 두 줄로 표시
    plt.title(f"English: {eng_caption}\nKorean: {kor_caption}", fontsize=12, color="blue", weight="bold", loc="center")
    plt.show()

# 테스트 실행
image_path = 'pics/mount.jpg'  # 테스트 이미지 경로 입력
show_image_with_captions(image_path)
eng_caption = generate_eng_caption(image_path)
korean_caption = translate_to_korean(eng_caption)
print('English Caption:', eng_caption)
print('Korean Caption:', korean_caption)
