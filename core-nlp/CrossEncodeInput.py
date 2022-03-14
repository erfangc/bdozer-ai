from pydantic import BaseModel


class CrossEncodeInput(BaseModel):
    query: str
    sentences: list[str]