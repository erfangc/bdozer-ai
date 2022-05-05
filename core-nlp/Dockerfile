FROM python:3.9

WORKDIR /app
ADD . /app

RUN pip install --no-cache-dir --upgrade -r requirements.txt
RUN ls -lR

# download pre-trained models so they are not downloaded at server startup which will 
# cause the server boot up to slow
RUN python download_pretrained_models.py
RUN python -m spacy download en_core_web_sm

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "80"]
