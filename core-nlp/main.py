import torch
from sentence_transformers import util

from models.AnswerQuestionRequest import AnswerQuestionResponse, AnswerQuestionRequest
from models.CrossEncodeInput import CrossEncodeInput
from models.DocInput import DocInput
from models.ScoredSentence import ScoredSentence
from models.Sentences import Sentences
from models.ZeroShotClassificationRequest import ZeroShotClassificationRequest
from models.ZeroShotClassificationResponse import ZeroShotClassificationResponse, SingleLabelClassificationResult
from shared_objects import cross_encoder, app, nlp, question_answer_model, question_answer_tokenizer, classifier


@app.post(
    path="/zero_shot_classification",
    operation_id="zero_shot_classification",
    response_model=ZeroShotClassificationResponse,
)
def zero_shot_classification(request: ZeroShotClassificationRequest) -> ZeroShotClassificationResponse:
    sentence = request.sentence
    candidate_labels = request.candidate_labels
    sentences = [sentence] + candidate_labels
    embeddings = classifier.encode(sentences, convert_to_tensor=True)
    cosine_scores = util.cos_sim(embeddings, embeddings)
    print(f"sentence={sentence} candidate_labels={candidate_labels}")

    scores = cosine_scores[0]
    result = [
        SingleLabelClassificationResult(label=candidate_labels[idx - 1], score=score)
        for idx, score in enumerate(scores) if idx != 0
    ]
    result = sorted(result, key=lambda r: r.score, reverse=True)

    return ZeroShotClassificationResponse(result=result)


@app.post(
    path="/answer_question",
    operation_id="answer_question",
    response_model=list[AnswerQuestionResponse]
)
def answer_question(request: AnswerQuestionRequest) -> list[AnswerQuestionResponse]:
    answers = []
    for question in request.questions:
        inputs = question_answer_tokenizer(
            question, request.context, add_special_tokens=True, return_tensors="pt"
        )
        input_ids = inputs["input_ids"].tolist()[0]

        outputs = question_answer_model(**inputs)
        answer_start_scores = outputs.start_logits
        answer_end_scores = outputs.end_logits

        # Get the most likely beginning of answer with the argmax of the score
        answer_start = torch.argmax(answer_start_scores)
        # Get the most likely end of answer with the argmax of the score
        answer_end = torch.argmax(answer_end_scores) + 1

        answer = question_answer_tokenizer.convert_tokens_to_string(
            question_answer_tokenizer.convert_ids_to_tokens(input_ids[answer_start:answer_end])
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
    print(f'running cross encoder for {cross_encode_input.reference}')
    references = cross_encode_input.reference
    comparisons = cross_encode_input.comparisons
    model_input = [[references, sent] for sent in comparisons]
    cross_scores = cross_encoder.predict(model_input)
    model_output = list(zip(cross_scores.tolist(), comparisons))
    ret = [
        ScoredSentence(score=elm[0], sentence=elm[1]) for elm in
        sorted(model_output, key=lambda x: x[0], reverse=True)
    ]
    return ret
