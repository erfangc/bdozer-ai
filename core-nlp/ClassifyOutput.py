from pydantic import BaseModel


class ClassifyOutput(BaseModel):
    label: str
    score: str