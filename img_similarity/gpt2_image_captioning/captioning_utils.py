
from transformers import VisionEncoderDecoderModel, ViTImageProcessor, AutoTokenizer
# from transformers import AutoModelForCausalLM, AutoTokenizer
import torch
from PIL import Image
import matplotlib.pyplot as plt

# 모델과 전처리 도구 로드
model = VisionEncoderDecoderModel.from_pretrained("nlpconnect/vit-gpt2-image-captioning")
processor = ViTImageProcessor.from_pretrained("nlpconnect/vit-gpt2-image-captioning")
# feature_extractor = ViTImageProcessor.from_pretrained("nlpconnect/vit-gpt2-image-captioning")
tokenizer = AutoTokenizer.from_pretrained("nlpconnect/vit-gpt2-image-captioning")

device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
model.to(device)

# max_length = 16
max_length = 20
num_beams = 4
gen_kwargs = {"max_length": max_length, "num_beams": num_beams}

preposition_lst = ['above', 'below', 'over', 'under', 'down', 'into', 'accross', 'along',
                    'through', 'around', 'behind', 'fornt', 'between', 'among', 'for', 'with', 'from', 'and']

# 이전 버전
# def predict_caption(image_path): 
    
#     i_image = Image.open(image_path)
#     if i_image.mode != "RGB":
#         i_image = i_image.convert(mode="RGB")

#     pixel_values = feature_extractor(images=[i_image], return_tensors="pt").pixel_values
#     pixel_values = pixel_values.to(device)

#     output_ids = model.generate(pixel_values, max_length=16, num_beams=4)
#     caption = tokenizer.decode(output_ids[0], skip_special_tokens=True)
#     return caption

# new 버전
def predict_caption(image_path):
    i_image = Image.open(image_path)
    if i_image.mode != "RGB":
        i_image = i_image.convert(mode="RGB")

    pixel_values = processor(images=i_image, return_tensors="pt").pixel_values.to(device)
    output_ids = model.generate(pixel_values, **gen_kwargs)
    caption = tokenizer.decode(output_ids[0], skip_special_tokens=True)
    return caption


# input: img_path(string) -> output: word_lst[]
def make_caption_list(image_path): 
    word_list = []
    words_ch = set()

    i_image = Image.open(image_path)
    if i_image.mode != "RGB":
        i_image = i_image.convert(mode="RGB")

    pixel_values = processor(images=[i_image], return_tensors="pt").pixel_values
    pixel_values = pixel_values.to(device)

    output_ids = model.generate(pixel_values, max_length=16, num_beams=4)
    caption = tokenizer.decode(output_ids[0], skip_special_tokens=True)

    print('CAPTION: ', caption)

    word_list = [ word for word in caption.split() 
                 if len(word) > 2 and word not in preposition_lst and not (word in words_ch or words_ch.add(word))] # 풍경에서 2글자 이하인게 있나?
    # print(word_list)
    return word_list


# 시각화
def show_image_with_caption(image_path):
    caption = predict_caption(image_path)
    print('CAPTION: ', caption)
    image = Image.open(image_path)

    plt.figure(figsize=(8, 8))
    plt.imshow(image)
    plt.axis("off")  # 축 제거
    plt.title(caption, fontsize=15, color="blue", weight="bold", loc="center")
    plt.show()

