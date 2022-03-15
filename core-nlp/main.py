import torch

from AnswerQuestionRequest import AnswerQuestionRequest, AnswerQuestionResponse
from CrossEncodeInput import CrossEncodeInput
from DocInput import DocInput
from ScoredSentence import ScoredSentence
from Sentences import Sentences
from shared_objects import cross_encoder, app, nlp, q_a_model, q_a_tokenizer


@app.post(
    path="/answer_question",
    response_model=list[AnswerQuestionResponse]
)
def classification(request: AnswerQuestionRequest) -> list[AnswerQuestionResponse]:
    answers = []
    for question in request.questions:
        inputs = q_a_tokenizer(question, request.context, add_special_tokens=True, return_tensors="pt")
        input_ids = inputs["input_ids"].tolist()[0]

        outputs = q_a_model(**inputs)
        answer_start_scores = outputs.start_logits
        answer_end_scores = outputs.end_logits

        # Get the most likely beginning of answer with the argmax of the score
        answer_start = torch.argmax(answer_start_scores)
        # Get the most likely end of answer with the argmax of the score
        answer_end = torch.argmax(answer_end_scores) + 1

        answer = q_a_tokenizer.convert_tokens_to_string(
            q_a_tokenizer.convert_ids_to_tokens(input_ids[answer_start:answer_end])
        )
        answers.append(AnswerQuestionResponse(question=question, best_answer=answer))
    return answers


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
