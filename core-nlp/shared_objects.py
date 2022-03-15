import spacy
from fastapi import FastAPI
from sentence_transformers import CrossEncoder
from transformers import AutoTokenizer, AutoModelForQuestionAnswering

from download_pretrained_models import cross_encoder_model, q_a_model

app = FastAPI()

nlp = spacy.load("en_core_web_trf")
cross_encoder = CrossEncoder(cross_encoder_model)

q_a_tokenizer = AutoTokenizer.from_pretrained(q_a_model)
q_a_model = AutoModelForQuestionAnswering.from_pretrained(q_a_model)
