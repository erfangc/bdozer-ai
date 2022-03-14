import spacy
from fastapi import FastAPI
from sentence_transformers import CrossEncoder

app = FastAPI()

nlp = spacy.load("en_core_web_trf")
cross_encoder = CrossEncoder('cross-encoder/ms-marco-MiniLM-L-6-v2')
