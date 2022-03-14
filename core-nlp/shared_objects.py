import spacy
from fastapi import FastAPI
from sentence_transformers import CrossEncoder

from download_pretrained_models import cross_encoder_model

app = FastAPI()

nlp = spacy.load("en_core_web_trf")
cross_encoder = CrossEncoder(cross_encoder_model)
