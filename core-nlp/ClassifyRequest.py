from pydantic import BaseModel


class ClassifyRequest(BaseModel):
    sentence: str
    labels: list[str]