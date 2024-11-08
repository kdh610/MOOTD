
from transformers import VisionEncoderDecoderModel, ViTImageProcessor, AutoTokenizer
import torch
from PIL import Image
import matplotlib.pyplot as plt

# 모델과 전처리 도구 로드
model = VisionEncoderDecoderModel.from_pretrained("nlpconnect/vit-gpt2-image-captioning")
feature_extractor = ViTImageProcessor.from_pretrained("nlpconnect/vit-gpt2-image-captioning")
tokenizer = AutoTokenizer.from_pretrained("nlpconnect/vit-gpt2-image-captioning")

device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
model.to(device)



max_length = 16
num_beams = 4
gen_kwargs = {"max_length": max_length, "num_beams": num_beams}

'''
# 여러 이미지 파일 경로를 리스트로 받기
 여러 이미지를 한 번에 처리하고 각 이미지에 대해 생성된 캡션 목록을 반환
def predict_caption(image_paths):
  images = []
  for image_path in image_paths:
    i_image = Image.open(image_path)
    if i_image.mode != "RGB":
      i_image = i_image.convert(mode="RGB")

    images.append(i_image)

  pixel_values = feature_extractor(images=images, return_tensors="pt").pixel_values
  pixel_values = pixel_values.to(device)

  output_ids = model.generate(pixel_values, **gen_kwargs)

  captions = tokenizer.batch_decode(output_ids, skip_special_tokens=True)
  captions = [caption.strip() for caption in captions]
  return captions
'''

# 캡션 생성 함수
# 하나의 이미지에 대해서만 처리
# 하나의 이미지에 대해서만 캡션 생성 및 결과 단일 문자열 반환
def predict_caption(image_path): 
    
    i_image = Image.open(image_path)
    if i_image.mode != "RGB":
        i_image = i_image.convert(mode="RGB")

    pixel_values = feature_extractor(images=[i_image], return_tensors="pt").pixel_values
    pixel_values = pixel_values.to(device)

    output_ids = model.generate(pixel_values, max_length=16, num_beams=4)
    caption = tokenizer.decode(output_ids[0], skip_special_tokens=True)
    return caption


# 단어 리스트를 반환해 볼까??
def make_caption_list(image_path): 
    word_list = []

    i_image = Image.open(image_path)
    if i_image.mode != "RGB":
        i_image = i_image.convert(mode="RGB")

    pixel_values = feature_extractor(images=[i_image], return_tensors="pt").pixel_values
    pixel_values = pixel_values.to(device)

    output_ids = model.generate(pixel_values, max_length=16, num_beams=4)
    caption = tokenizer.decode(output_ids[0], skip_special_tokens=True)

    word_list = [ word for word in caption.split() if len(word) > 2] # 풍경에서 2글자 이하인게 있나?

    # print('CAPTION: ', caption)
    # print('word_list: ', word_list)
    # print('typeOfCaption', type(caption))
    return word_list


# 이미지와 캡션을 시각화
def show_image_with_caption(image_path):
    caption = predict_caption(image_path)
    print('CAPTION: ', caption)
    # 이미지 로드
    image = Image.open(image_path)

    # 시각화
    plt.figure(figsize=(8, 8))
    plt.imshow(image)
    plt.axis("off")  # 축 제거
    plt.title(caption, fontsize=15, color="blue", weight="bold", loc="center")
    plt.show()


# 테스트할 이미지 경로
image_path = 'pics/bada3.jpg'  # 테스트 이미지 경로 입력
# make_caption_list(image_path)
