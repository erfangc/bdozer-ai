from pydantic import BaseModel


class ZeroShotClassificationRequest(BaseModel):
    candidate_labels: list[str]
    sentence: str