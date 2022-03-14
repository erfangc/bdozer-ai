from pydantic import BaseModel


class ScoredSentence(BaseModel):
    sentence: str
    score: float