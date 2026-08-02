from datetime import date

from pydantic import BaseModel, ConfigDict, Field, field_validator
from uuid import UUID

class PositionInput(BaseModel):
    model_config = ConfigDict(extra="forbid")
    position_id: UUID = Field(
        ...,
        description="Unique identifier for the position",
    )
    symbol: str = Field(
        ...,
        min_length=1,
        description="Symbol of the position"
    )
    buy_date: date = Field(
        ...,
        description="Date of the buy position"
    )
    quantity: float = Field(
        ...,
        gt=0,
        description="Quantity of the position"
    )
    currency: str = Field(
        ...,
        min_length=3,
        max_length=3,
        description="Currency of the position",
    )

    @field_validator("currency")
    @classmethod
    def normalize_currency(cls, value: str) -> str:
        value = value.strip().upper()
        if len(value) != 3 or not value.isalpha():
            raise ValueError("Invalid currency code")
        return value



class PortfolioBackfillRequest(BaseModel):
    model_config = ConfigDict(extra="forbid")
    positions: list[PositionInput] = Field(
        ...,
        min_length=1,
        description="List of positions to buy or sell"
    )
    target_currency: str = Field(
        default="PLN",
        min_length=3,
        max_length=3,
        description="Currency of the target position"
    )
    end_date: date | None = Field(
        default=None,
        description="End date of the backfill."
    )

    @field_validator("target_currency")
    @classmethod
    def normalize_currency(cls, value: str) -> str:
        value = value.strip().upper()
        if len(value) != 3 or not value.isalpha():
            raise ValueError("Invalid currency code")
        return value

class TotalValuePoint(BaseModel):
    model_config = ConfigDict(extra="forbid")
    date: date
    total_value: float

class PositionValuePoint(BaseModel):
    model_config = ConfigDict(extra="forbid")
    date: date
    value: float

class PositionHistory(BaseModel):
    model_config = ConfigDict(extra="forbid")
    position_id: UUID
    symbol: str
    values: list[PositionValuePoint]

class PortfolioBackfillResponse(BaseModel):
    model_config = ConfigDict(extra="forbid")
    total: list[TotalValuePoint]
    positions: list[PositionHistory]
