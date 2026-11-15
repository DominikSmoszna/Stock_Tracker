from fastapi import APIRouter, HTTPException, Path, status

from app.instruments import get_instrument_info
from app.schemas.instrument import InstrumentInfo

router = APIRouter(prefix="/api/instrument", tags=["Instrument"])


@router.get(
    "/{symbol}",
    response_model=InstrumentInfo,
    summary="Get instrument currency and metadata",
)
def get_instrument(
        symbol: str = Path(
            ...,
            min_length=1,
            max_length=32,
            description="Yahoo Finance ticker symbol, e.g. AAPL, VUAA.L, PKN.WA",
        ),
) -> InstrumentInfo:
    data = get_instrument_info(symbol.strip().upper())
    if data is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Instrument not found: {symbol}",
        )
    return InstrumentInfo(**data)
