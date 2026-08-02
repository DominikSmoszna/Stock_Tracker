from datetime import date
from uuid import UUID
import pandas as pd
from fastapi import APIRouter, HTTPException, status
from app.schemas.portfolio import (PortfolioBackfillRequest, PortfolioBackfillResponse, PositionHistory, PositionValuePoint, TotalValuePoint)
from ..price_series import (align_and_fill_series, fetch_multiple_price_series, validate_aligned_data,)

router = APIRouter(prefix="/api/portfolio", tags=["Portfolio"])

BUFFER_DAYS = 7

@router.post("/backfill", response_model=PortfolioBackfillResponse,)
def portfolio_backfill(
        request: PortfolioBackfillRequest,
) -> PortfolioBackfillResponse:
    start_date = min(position.buy_date for position in request.positions)
    end_date = request.end_date or date.today()

    fetch_start = (
        pd.Timestamp(start_date) - pd.Timedelta(days=BUFFER_DAYS)
    ).date()

    symbols = list(
        dict.fromkeys(
            position.symbol
            for position in request.positions
        )
    )

    prices_dict = fetch_multiple_price_series(
        symbols=symbols,
        start_date=fetch_start,
        end_date=end_date,
    )

    currencies = list(
        dict.fromkeys(
            position.currency
            for position in request.positions
        )
    )

    fx_currencies = [
        currency
        for currency in currencies
        if currency != request.target_currency
    ]

    ticker_by_currency = {
        currency: f"{currency}{request.target_currency}=X"
        for currency in fx_currencies
    }

    raw_fx_dict = fetch_multiple_price_series(
        symbols=list(ticker_by_currency.values()),
        start_date=fetch_start,
        end_date=end_date,
    )

    fx_dict: dict[str, pd.Series] = {}

    for currency in fx_currencies:
        ticker = ticker_by_currency[currency]
        fx_dict[currency] = raw_fx_dict[ticker]

    response_index = pd.date_range(start=start_date, end=end_date, freq="D")

    fx_dict[request.target_currency] = pd.Series(
        1.0,
        index=response_index,
        name=request.target_currency,
    )

    prices_df = align_and_fill_series(
        prices_dict,
        start_date,
        end_date,
    )

    fx_df = align_and_fill_series(
        fx_dict,
        start_date,
        end_date,
    )

    price_errors = validate_aligned_data(prices_df)
    fx_errors = validate_aligned_data(fx_df)

    if price_errors or fx_errors:
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail={
                "message": "Failed to fetch historical market data",
                "price_symbols": price_errors,
                "fx_symbols": fx_errors,
            },
        )

    prices_df = prices_df.fillna(0.0)
    fx_df = fx_df.fillna(0.0)

    assert prices_df.index.equals(fx_df.index)

    total_series = pd.Series(
        0.0,
        index=prices_df.index,
        dtype=float,
    )

    position_histories: list[PositionHistory] = []

    for position in request.positions:
        value_series = (
            position.quantity * prices_df[position.symbol] * fx_df[position.currency]
        )
        buy_timestamp = pd.Timestamp(position.buy_date)
        value_series.loc[value_series.index < buy_timestamp] = 0.0
        total_series += value_series

        values = [
            PositionValuePoint(
                date=timestamp.date(),
                value=float(value),
            )
            for timestamp, value in value_series.items()
        ]

        position_histories.append(
            PositionHistory(
                position_id=position.position_id,
                symbol=position.symbol,
                values=values,
            )
        )
    total = [TotalValuePoint(
        date=timestamp.date(),
        total_value=float(value),
        )
        for timestamp, value in total_series.items()
    ]

    return PortfolioBackfillResponse(
        total=total,
        positions=position_histories,
    )