FROM python:3.9

WORKDIR /code

COPY ./requirements.txt /code/requirements.txt

RUN pip install --no-cache-dir --upgrade -r /code/requirements.txt

COPY .app /code/app

# download pre-trained models so they are not downloaded at server startup which will 
# cause the server boot up to slow
RUN /code/app/download_pretrained_models.py
RUN python -m spacy download en_core_web_sm

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "80"]
