from meta_clip.metaclip_utils import metaclip_res

def generate_metaclip_keywords(image_path, word_list):
    # MetaCLIP으로 유사도 기반 키워드 추출
    keywords = metaclip_res(image_path, word_list)
    return keywords
