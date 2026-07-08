import pandas as pd
import numpy as np
from typing import List, Dict, Any

DEFAULT_PERIODS = {
    "sma": 20,
    "ema": 20,
    "rsi": 14,
    "bb": 20,
}

def parse_indicators(indicators_str: str) -> dict[str, int]:
    indicators_dict = {}
    for pair in indicators_str.split(','):
        pair = pair.strip()
        if not pair:
            continue

        element = pair.split(":")
        if len(element) == 2:
            key = element[0].strip()
            value = element[1].strip()
            if value.isdigit():
                indicators_dict[key] = int(value)
            else:
                value = DEFAULT_PERIODS[key]
                indicators_dict[key] = int(value)
        else:
            key = element[0].strip()
            value = DEFAULT_PERIODS[key]
            indicators_dict[key] = int(value)
    return indicators_dict

def _calculate_bb(close: pd.Series, period: int) -> pd.DataFrame:
    sma = close.rolling(window = period).mean()
    rolling_std = close.rolling(window = period).std()
    upper = sma + (rolling_std*2)
    lower = sma - (rolling_std*2)
    return pd.DataFrame({'upper': upper,'middle': sma, 'lower': lower})

def _calculate_rsi(close: pd.Series, period: int) -> pd.Series:
    delta = close.diff()
    gain = delta.clip(lower=0)
    loss = -delta.clip(upper=0)
    avg_gain = gain.ewm(com = period-1, min_periods = period).mean()
    avg_loss = loss.ewm(com = period-1, min_periods = period).mean()
    rs = avg_gain / avg_loss
    rsi = 100 - 100 / (1 + rs)
    rsi = rsi.where(avg_loss != 0, 100)
    rsi = rsi.where(avg_gain != 0, 0)
    return rsi


def calculate_sma(df: pd.DataFrame, period: int) -> List[Dict[str, Any]]:
    if df.empty or len(df) < period:
        return []
    local_df = df.copy()
    local_df['sma'] = local_df['close'].rolling(window = period).mean()
    local_df['sma'] = local_df['sma'].replace({np.nan: None, np.inf: None, -np.inf: None})
    return local_df[['time', 'sma']].dropna().rename(columns={'sma': 'value'}).to_dict('records')

def calculate_ema(df: pd.DataFrame, period: int) -> List[Dict[str, Any]]:
    if df.empty or len(df) < period:
        return []
    local_df = df.copy()
    local_df['ema'] = local_df['close'].ewm(span = period).mean()
    local_df['ema'] = local_df['ema'].replace({np.nan: None, np.inf: None, -np.inf: None})
    return local_df[['time', 'ema']].dropna().rename(columns={'ema': 'value'}).to_dict('records')

def calculate_bb(df: pd.DataFrame, period: int) -> List[Dict[str, Any]]:
    if df.empty or len(df) < period:
        return []
    local_df = df.copy()
    bb_df = _calculate_bb(local_df['close'], period)
    combined_df = pd.concat([local_df['time'], bb_df], axis=1)
    cols = ['upper', 'middle', 'lower']
    for col in cols:
        combined_df[col] = combined_df[col].replace(
            {np.nan: None, np.inf: None, -np.inf: None}
        )
    return combined_df[['time'] + cols].dropna(subset=cols).to_dict('records')

def calculate_rsi(df: pd.DataFrame, period: int) -> List[Dict[str, Any]]:
    if df.empty or len(df) < period:
        return []
    local_df = df.copy()
    local_df['rsi'] = _calculate_rsi(local_df['close'], period)
    local_df['rsi'] = local_df['rsi'].replace(
        {np.nan: None, np.inf: None, -np.inf: None}
    )
    return local_df[['time', 'rsi']].dropna(subset=['rsi']).rename(columns={'rsi': 'value'}).to_dict('records')