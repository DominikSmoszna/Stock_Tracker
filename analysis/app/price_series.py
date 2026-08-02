import logging
from concurrent.futures import ThreadPoolExecutor, as_completed
import pandas as pd
import yfinance as yf


def get_historical_prices(
    symbol: str, start_date: str | pd.Timestamp, end_date: str | pd.Timestamp
) -> pd.Series:
    ts_start = pd.to_datetime(start_date).tz_localize(None)
    ts_end = pd.to_datetime(end_date).tz_localize(None)

    api_end = ts_end + pd.Timedelta(days=1)

    df = yf.download(
        tickers=symbol,
        start=ts_start.strftime("%Y-%m-%d"),
        end=api_end.strftime("%Y-%m-%d"),
        auto_adjust=True,
        progress=False,
        threads=False,
    )

    if df is None:
        return pd.Series(dtype="float64", name=symbol, index=pd.DatetimeIndex([]))

    if df.empty or "Close" not in df.columns:
        return pd.Series(dtype="float64", name=symbol, index=pd.DatetimeIndex([]))

    series = df["Close"]

    if isinstance(series, pd.DataFrame):
        series = series.iloc[:,0]

    series = series.dropna()

    series.index = pd.to_datetime(series.index).tz_localize(None)
    series = series.sort_index()

    series = series.loc[ts_start:ts_end]

    series.name = symbol

    return series

def fetch_multiple_price_series(
    symbols: list[str],
    start_date: str | pd.Timestamp,
    end_date: str | pd.Timestamp,
    max_workers: int = 5,
) -> dict[str, pd.Series]:
    results: dict[str, pd.Series] = {}

    if not symbols:
        return results

    unique_symbols = set(symbols)

    with ThreadPoolExecutor(max_workers=max_workers) as executor:
        future_to_symbol = {
            executor.submit(get_historical_prices, symbol, start_date, end_date): symbol
            for symbol in unique_symbols
        }
        for future in as_completed(future_to_symbol):
            symbol = future_to_symbol[future]
            try:
                series = future.result()
                results[symbol] = series
            except Exception as e:
                logging.exception(f"Error during fetching data for {symbol}")
                results[symbol] = pd.Series(dtype="float64", name=symbol, index=pd.DatetimeIndex([]))
    return results

def align_and_fill_series(
    series_dict: dict[str, pd.Series],
    start_date: str | pd.Timestamp,
    end_date: str | pd.Timestamp,
) -> pd.DataFrame:
    start_ts = pd.Timestamp(start_date).tz_localize(None)
    end_ts = pd.Timestamp(end_date).tz_localize(None)

    response_index = pd.date_range(
        start=start_ts,
        end=end_ts,
        freq="D",
    )

    if not series_dict or all(series.empty for series in series_dict.values()):
        return pd.DataFrame(
            index=response_index,
            columns=list(series_dict.keys()),
            dtype="float64",
        )

    data_start = min(
        series.index.min()
        for series in series_dict.values()
        if not series.empty
    )

    full_index = pd.date_range(
        start=data_start,
        end=end_ts,
        freq="D",
    )

    df = pd.DataFrame(series_dict)
    df = df.reindex(full_index)
    df = df.ffill()
    df = df.reindex(response_index)

    return df

def validate_aligned_data(df: pd.DataFrame) -> list[str]:
    return [
        column
        for column in df.columns
        if df[column].isna().all()
    ]


if __name__ == "__main__":
    price = get_historical_prices("AAPL", "2026-01-02", "2026-01-02")
    fx = get_historical_prices("USDPLN=X", "2026-01-02", "2026-01-02")

    print(f"AAPL price: {price.iloc[0]}")
    print(f"USD/PLN rate: {fx.iloc[0]}")
    print(f"Manual calc: {2 * price.iloc[0] * fx.iloc[0]}")

