import spacy
from fastapi import FastAPI
from sentence_transformers import CrossEncoder
from sentence_transformers import SentenceTransformer
from transformers import AutoTokenizer, AutoModelForQuestionAnswering, AutoModelForSequenceClassification

from download_pretrained_models import cross_encoder_model_name, question_answer_model_name, \
    zero_shot_classification_model_name

app = FastAPI()

nlp = spacy.load("en_core_web_trf")
cross_encoder = CrossEncoder(cross_encoder_model_name)

question_answer_tokenizer = AutoTokenizer.from_pretrained(question_answer_model_name)
question_answer_model = AutoModelForQuestionAnswering.from_pretrained(question_answer_model_name)

zero_shot_classification_tokenizer = AutoTokenizer.from_pretrained(zero_shot_classification_model_name)
zero_shot_classification_model = AutoModelForSequenceClassification.from_pretrained(zero_shot_classification_model_name)

classifier = SentenceTransformer('all-MiniLM-L6-v2')
