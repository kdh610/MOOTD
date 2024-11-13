from unittest import TestCase
from asian_bart import AsianBartTokenizer, AsianBartForConditionalGeneration


class TestECJK(TestCase):

    def test_tokenizer(self):
        tokenizer = AsianBartTokenizer.from_pretrained(
            "hyunwoongko/asian-bart-ecjk")

        tokens = tokenizer.prepare_seq2seq_batch(
            src_texts="안녕하세요.",
            src_langs="ko_KR",
            tgt_texts="hello.",
            tgt_langs="en_XX",
        )

        print(tokens)

    def test_model(self):
        tokenizer = AsianBartTokenizer.from_pretrained(
            "hyunwoongko/asian-bart-ecjk")

        model = AsianBartForConditionalGeneration.from_pretrained(
            "hyunwoongko/asian-bart-ecjk")

        # tokens = tokenizer.prepare_seq2seq_batch(
        #     src_texts="Kevin is the <mask> man in the world.",
        #     src_langs="en_XX",
        #     tgt_langs = "ko_KR",
        #     return_tensors="pt"
        # )

        tokens = tokenizer(
            "Kevin is the most amazing person in the world.",
            src_lang="en_XX",  # 영어를 소스 언어로 설정
            tgt_lang="ko_KR",  # 한국어를 목표 언어로 설정
            return_tensors="pt"
        )
        
        output = model.generate(
            input_ids=tokens["input_ids"],
            attention_mask=tokens["attention_mask"],
            decoder_start_token_id=tokenizer.lang_code_to_id["ko_KR"],
        )

        print(tokenizer.decode(output.tolist()[0]))
        # en_XX<s> Kevin is the most beautiful man in the world.</s>

if __name__ == "__main__":
    test_instance = TestECJK()
    # test_instance.test_tokenizer()
    test_instance.test_model()