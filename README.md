# bdozer-ai

This is a multimodule project with both Kotlin and Python code

There are `3` servers/components to run

- `core-nlp` a Python FastAPI application that uses HuggingFace models to host endpoints for zero-shot classification and sentence tokenization
- `html-to-text-converter` NodeJS expressed based microservice to convert DOM to nicely formatted plaintext
- `ai-server` a Kotlin Spring Boot server that uses the other services as well as OpenAPI GPT3 APIs to orchestrate semantic search and facilitate main business logic

# Get Started

```shell
# verify python version
python3 --version
Python 3.9.9
```

```shell
# on the macOS
$ pip3 install uvicorn
```

Start the Python server locally:

```shell
cd ~/IdeaProjects/bdozer-ai/core-nlp
uvicorn main:app --reload
```

## Install Elasticsearch

 > Download 8.1.0 https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-7.13.4-darwin-x86_64.tar.gz
 > Click to unzip 

```shell
mv ~/Downloads/elasticsearch-7.13.4 ~
cd ~/elasticsearch-7.13.4
bin/elasticsearch
```

## Install Kibana

 > Download https://artifacts.elastic.co/downloads/kibana/kibana-7.13.4-darwin-x86_64.tar.gz
 > Click to unzip

```shell
mv ~/Downloads/kibana-7.13.4-darwin-x86_64 ~
cd ~/kibana-7.13.4-darwin-x86_64
bin/kibana
```


## Generate the SDK for Kotlin server to call NLP server

```shell
# make sure you are in the git project root directory, eg.
cd ~/IdeaProjects/bdozer-ai/

# launch the python server locally
cd core-nlp; uvicorn main:app --reload

# prepare and install openapi-generator-cli
npm install @openapitools/openapi-generator-cli -g
openapi-generator-cli version

# run SDK generation (from the root directory)
cd ~/IdeaProjects/bdozer-ai/ && openapi-generator-cli generate \
 -i http://localhost:8000/openapi.json \
 -g java \
 -o core-nlp-sdk \
 -c config.yaml 
```

## Deploying the Python code

 > Deploy to GPU enabled instance
 
```shell
cd ~/IdeaProjects/bdozer-ai/core-nlp
# download models from HuggingFace hub to the local working directory, so the entire application can be properly packaged
# and shipped up to some kind of Deployment package
python3 download_pretrained_models.py
```
