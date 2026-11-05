import { ChartResponse, MarketQuote, SearchResult } from "../types/market.ts";

const trimTrailingSlash = (url: string): string => url.replace(/\/+$/, "");

export const API_BASE_URL = trimTrailingSlash(
    import.meta.env.VITE_API_URL || "http://localhost:8080"
);

export const PYTHON_API_BASE_URL = trimTrailingSlash(
    import.meta.env.VITE_PYTHON_API_URL || "http://localhost:8000"
);

export const WS_URL = import.meta.env.VITE_WS_URL || `${API_BASE_URL}/ws`;

export async function fetchMarketQuote(symbol: string): Promise<MarketQuote> {
    const response = await fetch(
        `${API_BASE_URL}/api/market/quote/${encodeURIComponent(symbol)}`
    );
    if (!response.ok) {
        throw new Error(`Error occurred during fetching symbol: ${symbol}`);
    }
    return response.json();
}

export interface FetchChartParams {
    interval: string;
    range?: string;
    start?: string;
    end?: string;
    indicators?: string;
}

export async function fetchChartData(
    symbol: string,
    params: FetchChartParams
): Promise<ChartResponse> {
    const searchParams = new URLSearchParams({ interval: params.interval });
    if (params.range) searchParams.set("range", params.range);
    if (params.start) searchParams.set("start", params.start);
    if (params.end) searchParams.set("end", params.end);
    if (params.indicators) searchParams.set("indicators", params.indicators);

    const response = await fetch(
        `${PYTHON_API_BASE_URL}/api/chart/${encodeURIComponent(symbol)}?${searchParams.toString()}`
    );
    if (!response.ok) {
        throw new Error("Error fetching chart data");
    }
    return response.json();
}

export async function searchSymbols(query: string): Promise<SearchResult[]> {
    const searchParams = new URLSearchParams({ q: query });
    const response = await fetch(
        `${PYTHON_API_BASE_URL}/api/search?${searchParams.toString()}`
    );
    if (!response.ok) {
        throw new Error("Error searching symbols");
    }
    return response.json();
}
