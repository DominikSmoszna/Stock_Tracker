from datetime import date
from fastapi import APIRouter, Path, Query, HTTPException, status
from pydantic import BaseModel, Field

from app.price_series import get_historical_prices

router = APIRouter(prefix="/api/fx", tags=["FX"])

class FxRatePoint(BaseModel):
    date: str = Field(
        ...,
        description = "The date the price was last updated. YYYY-MM-DD",
        examples = ["2022-01-01"]
    )
    rate: float = Field(
        ...,
        description = "The rate of the price at that date",
        examples = [4.0152]
    )

@router.get(
    "/{pair}",
    response_model=list[FxRatePoint],
    summary = "Get the price at that date",
    status_code = status.HTTP_200_OK
)
def get_fx_rates(
        pair: str = Path(
            ...,
            description = "The pair of currency to get prices",
            pattern = r"^[a-zA-Z]{6}$"
        ),
        start_date: date = Query(
            ...,
            description = "The date the price was last updated. YYYY-MM-DD",
        ),
        end_date: date = Query(
            ...,
            description = "The date the price was last updated. YYYY-MM-DD",
        )
) -> list[FxRatePoint]:
    if (start_date > end_date):
        raise HTTPException(
            status_code = status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail = "The start date is greater than the end date."
        )
    ticker_yf = f"{pair.upper()}=X"

    prices_series = get_historical_prices(
        symbol = ticker_yf,
        start_date = start_date,
        end_date = end_date
    )

    if prices_series.empty:
        return []

    return [
        FxRatePoint(
            date = timestamp.strftime("%Y-%m-%d"),
            rate = round(float(val),4)
        )
        for timestamp, val in prices_series.items()
    ]