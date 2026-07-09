export const INTERVAL_DEFAULT_RANGE: Record<string, string> = {
    '1m': '1d',
    '5m': '1d',
    '15m': '1d',
    '30m': '5d',
    '1h': '5d',
    '4h': '1mo',
    '1d': '1mo',
    '1wk': '6mo',
}

export const INTERVAL_OFFSET_MAP: Record<string, number> = {
    '1m': 3,
    '5m': 15,
    '15m': 15,
    '30m': 15,
    '1h': 60,
    '4h': 60,
    '1d': 90,
    '1wk': 365,
}

export const toDate = (time: string | number | null): Date => {
    if (!time) return new Date();
    if (typeof time === "number") {
        return new Date(time*1000);
    }
    return new Date(time);
}

export const toDateString = (time: string | number| Date): string => {
    if (time instanceof Date) return time.toISOString().split('T')[0];
    if (typeof time === "string") return time;
    return new Date(time*1000).toISOString().split('T')[0];
}