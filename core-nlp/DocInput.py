from pydantic import BaseModel


class DocInput(BaseModel):
    doc: str