from fastapi import APIRouter, Query
import yfinance as yf
import numpy as np

router = APIRouter()

@router.get("/chart/{symbol}")
async def get_chart_data(
        symbol: str,
        interval: str = Query(default="1d", description="Data interval"),
        period: str = Query(default="1mo", alias="range", description="Period"),
    ):
        ticker = yf.Ticker(symbol)
        df = ticker.history(period=period, interval=interval)

        if df.empty:
            return []

        df = df.reset_index()

        date_col = 'Date' if 'Date' in df.columns else 'Datetime'

        if date_col in df.columns:
            if interval in ['1d', '5d', '1wk', '1mo', '3mo']:
                df[date_col] = df[date_col].dt.strftime('%Y-%m-%d')
            else:
                df[date_col] = df[date_col].dt.strftime('%Y-%m-%d %H:%M:%S')

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

        chart_data = df.to_dict('records')

        return chart_data