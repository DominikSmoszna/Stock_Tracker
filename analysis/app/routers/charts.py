from fastapi import APIRouter, Query
from pydantic import BaseModel
import yfinance as yf
import numpy as np
import pandas as pd
from app.indicators import parse_indicators, calculate_sma, calculate_ema, calculate_rsi, calculate_bb

router = APIRouter()

INDICATOR_MAP = {
    "sma": calculate_sma,
    "ema": calculate_ema,
    "rsi": calculate_rsi,
    "bb": calculate_bb,
}

@router.get("/chart/{symbol}")
async def get_chart_data(
        symbol: str,
        interval: str = Query(default="1d", description="Data interval"),
        period: str = Query(default="1mo", alias="range", description="Period"),
        start: str = Query(default=None, description="Start date"),
        end: str = Query(default=None, description="End date"),
        indicators: str = Query(default=None, description="Comma separated indicators"),
    ):
        ticker = yf.Ticker(symbol)
        indicator_dict = parse_indicators(indicators) if indicators else {}

        if start:
            df = ticker.history(start=start,end=end, interval=interval)
        else:
            df = ticker.history(period=period, interval=interval)

        if df.empty:
            return {"candles": []}

        df = df.reset_index()

        date_col = 'Date' if 'Date' in df.columns else 'Datetime'

        if date_col in df.columns:
            if interval in ['1d', '5d', '1wk', '1mo', '3mo']:
                df[date_col] = df[date_col].dt.strftime('%Y-%m-%d')
            else:
                df[date_col] = df[date_col].dt.tz_convert('UTC').astype('int64')

        df =df.rename(columns={
            date_col: 'time',
            'Open': 'open',
            'High': 'high',
            'Low': 'low',
            'Close': 'close',
            'Volume': 'volume',
        })

        required_columns = ['time', 'open', 'high', 'low', 'close', 'volume']
        df = df[required_columns]

        df = df.replace({np.nan: None, np.inf: None, -np.inf: None})

        df = df.drop_duplicates(subset=['time'])
        df = df.sort_values('time')
        chart_data = df.to_dict('records')

        result = {"candles": chart_data}

        if "sma" in indicator_dict:
            result["sma"] = calculate_sma(df, period=indicator_dict['sma'])
        if "ema" in indicator_dict:
            result["ema"] = calculate_ema(df, period=indicator_dict['ema'])
        if "rsi" in indicator_dict:
            result["rsi"] = calculate_rsi(df, period=indicator_dict['rsi'])
        if "bb" in indicator_dict:
            result["bb"] = calculate_bb(df, period=indicator_dict['bb'])

        return result

class CandleModel(BaseModel):
    time: str | int
    open: float
    high: float
    low: float
    close: float
    volume: int

class ChartAPI(BaseModel):
    candles: list[CandleModel]
    indicators: dict[str, int]

@router.post("/indicators")
async def get_chart_data(chart_data: ChartAPI):
    if not chart_data.candles:
        return {}
    df = pd.DataFrame(c.model_dump() for c in chart_data.candles)
    df = df.sort_values('time').reset_index(drop=True)
    result = {}

    for indicator in chart_data.indicators:
        calc_function = INDICATOR_MAP.get(indicator)
        if calc_function is None:
            continue
        result[indicator] = calc_function(df,chart_data.indicators[indicator])
    return result

class SearchResult(BaseModel):
    symbol: str
    name: str
    type: str

@router.get("/search")
async def get_search_results(
        q: str,
    ):
    search_data = yf.Search(q)
    results = []

    if search_data.quotes:
        for quote in search_data.quotes[:10]:
            name = quote.get("shortname") or quote.get("longname") or "Unknown"
            results.append(
                SearchResult(
                    symbol=quote.get("symbol", ""),
                    name=name,
                    type=quote.get("typeDisp", "Unknown"),
                )
            )
    return results

