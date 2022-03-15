from pydantic import BaseModel


class AnswerQuestionRequest(BaseModel):
    questions: list[str]
    context: str


class AnswerQuestionResponse(BaseModel):
    question: str
    best_answer: str