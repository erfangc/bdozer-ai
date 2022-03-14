from pydantic import BaseModel


class Sentences(BaseModel):
    sentences: list[str]