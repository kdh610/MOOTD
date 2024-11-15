import torch
from PIL import Image
import open_clip
import matplotlib.pyplot as plt
from googletrans import Translator
  

def metaclip_res(image_path, word_list):
    # MetaCLIP 모델 불러오기
    model, _, preprocess = open_clip.create_model_and_transforms('ViT-B-32-quickgelu', pretrained='metaclip_400m')

    image = preprocess(Image.open(image_path)).unsqueeze(0)
    
    # text = ['sea', 'river', 'ocean', 'beach']
    # text = ['a castle'] 키워드 한개는 안되나봐요..?/ 키워드 최소 2개 이상부터 가능

    # 이미지와 텍스트 유사도 계산
    with torch.no_grad():
        image_features = model.encode_image(image)
        # text_features = model.encode_text(open_clip.tokenize(text))
        text_features = model.encode_text(open_clip.tokenize(word_list))
        image_features /= image_features.norm(dim=-1, keepdim=True)
        text_features /= text_features.norm(dim=-1, keepdim=True)
        text_probs = (100.0 * image_features @ text_features.T).softmax(dim=-1).squeeze()

    # text_probs를 list로 변환
    text_probs = text_probs.tolist()
    formatted_probs = [round(prob * 100, 2) for prob in text_probs]
    # print("Word list:", text_probs)
    print("Label probs:", formatted_probs)


    # 상위 두 개 유사도 인덱스 찾기
    top_two_indices = sorted(range(len(formatted_probs)), key=lambda i: formatted_probs[i], reverse=True)[:2]
    
    # keywords_lst = [text[i] for i in top_two_indices]
    keywords_lst = [word_list[i] for i in top_two_indices]

    # 가장 높은 유사도 뽑
    # keywords_lst = []
    # max_index = formatted_probs.index(max(formatted_probs))
    # keywords_lst = [word_list[max_index]]

    # Google 번역기 초기화
    translator = Translator()

    # 영어 키워드를 한국어로 번역
    translated_keywords = [translator.translate(keyword, src='en', dest='ko').text for keyword in keywords_lst]

    print('Before Translate: ', keywords_lst)
    print("Translated keywords:", translated_keywords)

    return keywords_lst

    

    '''
    # 결과 시각화 - 텍스트 여백 분리
    fig, (ax_img, ax_text) = plt.subplots(2, 1, gridspec_kw={'height_ratios': [4, 1]}, figsize=(12, 12))

    # 그림과 텍스트 사이 간격 조정
    fig.subplots_adjust(hspace=0)  # hspace 값을 조절하여 간격을 설정

    # 이미지 표시 (첫 번째 subplot)
    ax_img.imshow(Image.open(image_path))
    ax_img.axis('off')  # 축 제거

    # 텍스트 표시 (두 번째 subplot)
    ax_text.axis('off')  # 축 제거
    for i, (label, prob) in enumerate(zip(word_list, text_probs)):
        ax_text.text(0.05, 0.8 - i * 0.3, f"{label}: {prob:.2%}", color='white', fontsize=11, 
                    ha='left', backgroundcolor=(0, 0, 0, 0.6))  # 텍스트와 투명한 배경 설정


    plt.show()
    '''
