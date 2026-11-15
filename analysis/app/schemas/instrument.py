from pydantic import BaseModel, ConfigDict, Field


class InstrumentInfo(BaseModel):
    model_config = ConfigDict(extra="forbid")

    symbol: str = Field(..., description="Ticker symbol as used by Yahoo Finance", examples=["VUAA.L"])
    name: str | None = Field(default=None, description="Full instrument name")
    currency: str | None = Field(
        default=None,
        description="Normalized ISO 4217 currency code (e.g. GBp -> GBP)",
        examples=["GBP"],
    )
    quote_currency: str | None = Field(
        default=None,
        description="Raw currency reported by Yahoo Finance (may be a minor unit, e.g. GBp)",
        examples=["GBp"],
    )
    price_divisor: int = Field(
        default=1,
        description="Divide quoted prices by this value to get prices in `currency` (100 for GBp/ZAc/ILA)",
        examples=[100],
    )
    exchange: str | None = Field(default=None, description="Exchange code", examples=["LSE"])
    quote_type: str | None = Field(default=None, description="Instrument type", examples=["ETF"])
    sector: str | None = None
    industry: str | None = None
    country: str | None = None
    market_cap: float | None = None
