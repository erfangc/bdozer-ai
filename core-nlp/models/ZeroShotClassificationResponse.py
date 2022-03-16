from pydantic import BaseModel


class SingleLabelClassificationResult(BaseModel):
    label: str
    score: float


class ZeroShotClassificationResponse(BaseModel):
    result: list[SingleLabelClassificationResult]
