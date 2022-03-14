from CrossEncodeInput import CrossEncodeInput
from DocInput import DocInput
from ScoredSentence import ScoredSentence
from Sentences import Sentences
from shared_objects import cross_encoder, app, nlp


@app.post(
    path="/sentence_producer",
    operation_id="get_sentences",
    description="""Turns a input document into sentences using `en_core_web_trf` transformer models""",
    response_model=Sentences,
)
def sentence_producer(doc_input: DocInput) -> Sentences:
    doc = nlp(doc_input.doc)
    sentences = [sent.text for sent in doc.sents]
    ret = Sentences(sentences=sentences)
    return ret


@app.post(
    path="/cross_encode",
    operation_id="cross_encode",
    description="Cross Encoder",
    response_model=list[ScoredSentence]
)
def cross_encode(cross_encode_input: CrossEncodeInput) -> list[ScoredSentence]:
    print(f'running cross encoder for {cross_encode_input.query}')
    query = cross_encode_input.query
    sentences = cross_encode_input.sentences
    model_input = [[query, sent] for sent in sentences]
    cross_scores = cross_encoder.predict(model_input)
    model_output = list(zip(cross_scores.tolist(), sentences))
    ret = [
        ScoredSentence(score=elm[0], sentence=elm[1]) for elm in
        sorted(model_output, key=lambda x: x[0], reverse=True)
    ]
    return ret
