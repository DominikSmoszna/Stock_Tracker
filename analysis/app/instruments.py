import logging
from concurrent.futures import ThreadPoolExecutor
from typing import Any

import yfinance as yf

# Yahoo Finance reports some exchanges in minor currency units.
# Maps raw quote currency -> (ISO 4217 major currency, divisor).
MINOR_CURRENCY_UNITS: dict[str, tuple[str, int]] = {
    "GBp": ("GBP", 100),
    "GBX": ("GBP", 100),
    "ZAc": ("ZAR", 100),
    "ZAC": ("ZAR", 100),
    "ILA": ("ILS", 100),
}


def normalize_currency(raw_currency: str | None) -> tuple[str | None, int]:
    """Returns (normalized ISO currency, price divisor) for a Yahoo quote currency."""
    if not raw_currency:
        return None, 1
    raw_currency = raw_currency.strip()
    if raw_currency in MINOR_CURRENCY_UNITS:
        return MINOR_CURRENCY_UNITS[raw_currency]
    return raw_currency.upper(), 1


def _safe_fast_info(ticker: yf.Ticker) -> dict[str, Any]:
    try:
        fast_info = ticker.fast_info
        return {
            "currency": fast_info.get("currency"),
            "exchange": fast_info.get("exchange"),
            "quote_type": fast_info.get("quoteType"),
            "market_cap": fast_info.get("marketCap"),
        }
    except Exception as e:
        logging.warning("fast_info unavailable for %s: %s", ticker.ticker, e)
        return {}


def _safe_info(ticker: yf.Ticker) -> dict[str, Any]:
    try:
        return ticker.info or {}
    except Exception as e:
        logging.warning("info unavailable for %s: %s", ticker.ticker, e)
        return {}


def get_instrument_info(symbol: str) -> dict[str, Any] | None:
    """Fetches instrument metadata. Returns None if Yahoo does not know the symbol."""
    ticker = yf.Ticker(symbol)
    info = _safe_info(ticker)
    fast = _safe_fast_info(ticker) if not info.get("currency") else {}

    raw_currency = info.get("currency") or fast.get("currency")
    name = info.get("longName") or info.get("shortName")

    if not raw_currency and not name:
        return None

    currency, divisor = normalize_currency(raw_currency)
    market_cap = info.get("marketCap") or fast.get("market_cap")

    return {
        "symbol": info.get("symbol") or symbol,
        "name": name,
        "currency": currency,
        "quote_currency": raw_currency,
        "price_divisor": divisor,
        "exchange": info.get("exchange") or fast.get("exchange"),
        "quote_type": info.get("quoteType") or fast.get("quote_type"),
        "sector": info.get("sector"),
        "industry": info.get("industry"),
        "country": info.get("country"),
        "market_cap": float(market_cap) if market_cap else None,
    }


# Cache of successfully resolved divisors. A symbol's quote currency practically never changes,
# so we avoid an extra Yahoo request on every backfill. Failures are NOT cached.
_PRICE_DIVISOR_CACHE: dict[str, int] = {}


def get_price_divisor(symbol: str) -> int:
    """
    Returns the factor by which Yahoo quotes for `symbol` must be divided to express them in the
    normalized (major) currency, e.g. 100 for LSE stocks quoted in GBp. Falls back to 1 on error.
    """
    if symbol in _PRICE_DIVISOR_CACHE:
        return _PRICE_DIVISOR_CACHE[symbol]

    ticker = yf.Ticker(symbol)
    raw_currency = _safe_fast_info(ticker).get("currency")
    if not raw_currency:
        raw_currency = _safe_info(ticker).get("currency")
    if not raw_currency:
        logging.warning("Could not determine quote currency for %s, assuming divisor 1", symbol)
        return 1

    _, divisor = normalize_currency(raw_currency)
    _PRICE_DIVISOR_CACHE[symbol] = divisor
    return divisor


def get_price_divisors(symbols: list[str], max_workers: int = 5) -> dict[str, int]:
    """Resolves price divisors for many symbols in parallel."""
    unique_symbols = list(dict.fromkeys(symbols))
    if not unique_symbols:
        return {}
    with ThreadPoolExecutor(max_workers=max_workers) as executor:
        return dict(zip(unique_symbols, executor.map(get_price_divisor, unique_symbols)))
