from pydantic import BaseModel


class CrossEncodeInput(BaseModel):
    reference: str
    comparisons: list[str]