import {LineData} from "lightweight-charts";

export interface StockPrice {
    ticker: string
    price: number
    timestamp: number
    }

export interface MarketQuote {
    currentPrice: number
    change: number
    percentChange: number
    high: number
    low: number
    open: number
    previousClose: number
    symbol: string
    timestamp: number
    }

export interface Market {
    id: string
    name: string
    timezone: string
    openHour: number
    openMinute: number
    closeHour: number
    closeMinute: number
}

export interface CandleData {
    time: string | number
    open: number
    high: number
    low: number
    close: number
    volume: number
}

export interface BollingerBandPoint {
    time: string | number
    upper: number
    middle: number
    lower: number
}

export interface MovingAverageSeriesData {
    key: string
    color: string
    data: LineData[]
}

export interface IndicatorData {
    movingAverages?: MovingAverageSeriesData[]
    rsi?: LineData[]
    rsiColor?: string
    bb?: BollingerBandPoint[]
    bbColor?: string
}

export interface RawIndicatorsResponse {
    rsi?: LineData[]
    bb?: BollingerBandPoint[]
    [key: string]: any
}

export interface ChartResponse extends RawIndicatorsResponse {
    candles: CandleData[]
}

export interface HoveredCandle {
    open: number
    high: number
    low: number
    close: number
    rsi?: number
}

export interface CandlestickChartProps {
    data: CandleData[]
    indicators?: IndicatorData
    onNeedMoreData: () => void
    onCrosshairMove?: (candle: HoveredCandle | null) => void
}

export interface SearchResult {
    symbol: string
    name: string
    type: string
}
