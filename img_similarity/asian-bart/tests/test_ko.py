# from unittest import TestCase
import unittest
from asian_bart import AsianBartTokenizer, AsianBartForConditionalGeneration

class TestKo(unittest.TestCase):

    def test_tokenizer(self):
        tokenizer = AsianBartTokenizer.from_pretrained(
            "hyunwoongko/asian-bart-ko")
        print("이얍호")
        tokens = tokenizer.prepare_seq2seq_batch(
            # src_texts="hello.",
            # src_langs="en_XX",
            # tgt_texts="nice to meet you.",
            # tgt_langs="en_XX",
            src_texts="안녕하세요.",
            src_langs="ko_KR",
            tgt_texts="반갑습니다.",
            tgt_langs="ko_KR",
        )


    def test_model(self):
        tokenizer = AsianBartTokenizer.from_pretrained(
            "hyunwoongko/asian-bart-en")

        model = AsianBartForConditionalGeneration.from_pretrained(
            "hyunwoongko/asian-bart-en")

        tokens = tokenizer.prepare_seq2seq_batch(
            src_texts="a man standing next to door.",
            src_langs="en_XX",
        )

        output = model.generate(
            input_ids=tokens["input_ids"],
            attention_mask=tokens["attention_mask"],
            decoder_start_token_id=tokenizer.lang_code_to_id["en_XX"],
        )

        print(tokenizer.decode(output.tolist()[0]))
        # ko_KR<s> 가장 존경하는 사람 중 한 사람이다.</s>


if __name__ == "__main__":
    unittest.main()
    TestKo.test_tokenizer()
# eng2ko = TestKo()
# eng2ko.test_tokenizer   