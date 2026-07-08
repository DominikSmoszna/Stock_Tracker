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
    time: string
    open: number
    high: number
    low: number
    close: number
    volume: number
}

export interface BollingerData {
    upper: LineData[]
    middle: LineData[]
    lower: LineData[]
}

export interface ChartResponse {
    candles: CandleData[]
    sma?: LineData[]
    ema?: LineData[]
    rsi?: LineData[]
    bb?: BollingerData[]
}