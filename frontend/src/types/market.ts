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
