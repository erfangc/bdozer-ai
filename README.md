# bdozer-ai

This is a multimodule project with both Kotlin and Python code

The Python code hosts Pytorch/HuggingFace transformer models that exposes API operations for
embedding / comparing sentences 

The Kotlin code handles ETL, metadata decoration and CRUD of the raw data sources

## Generate the SDK for Kotlin server to call NLP server

```shell
# make sure you are in the git project root directory, eg.
cd ~/IdeaProjects/bdozer-ai/

# launch the python server locally
cd core-nlp; uvicorn main:app --reload

# prepare and install openapi-generator-cli
npm install @openapitools/openapi-generator-cli -g
openapi-generator-cli version

# run SDK generation
openapi-generator-cli generate -i http://localhost:8000/openapi.json -g java -o core-nlp-sdk -c config.yaml 
```
