from transformers import MBartForConditionalGeneration, MBart50TokenizerFast

# Load the mBART model and tokenizer
model_name = "facebook/mbart-large-50-many-to-many-mmt"
tokenizer = MBart50TokenizerFast.from_pretrained(model_name)
model = MBartForConditionalGeneration.from_pretrained(model_name)

# Set the source and target languages
src_lang = "en_XX"  # English
tgt_lang = "ko_KR"  # Korean
tokenizer.src_lang = src_lang

# Prepare the source text
src_text = "Kevin is the most amazing person in the world."
inputs = tokenizer(src_text, return_tensors="pt")

# Generate the translation with mBART
translated_tokens = model.generate(**inputs, forced_bos_token_id=tokenizer.lang_code_to_id[tgt_lang])
translated_text = tokenizer.decode(translated_tokens[0], skip_special_tokens=True)

print("Translated Text:", translated_text)
