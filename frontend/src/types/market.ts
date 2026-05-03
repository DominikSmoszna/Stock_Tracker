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

export interface HistoricalPrice {
    ticker: string
    date: string
    open: number
    high: number
    low: number
    close: number
    volume: number
    change: number
    changePercent: number
    vwap: number
    }