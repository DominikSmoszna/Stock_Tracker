import React, {useEffect, useRef, useState} from "react";
import {createChart, CandlestickSeries} from "lightweight-charts";
import {ChartResponse, CandleData} from "../types/market.ts";

const INTERVAL_RANGE_MAP: Record<string, string[]> = {
    '1m':  ['1d','5d'],
    '5m':  ['1d','5d', '1mo'],
    '15m': ['1d','5d', '1mo'],
    '30m': ['1d','5d', '1mo','3mo'],
    '1h':  ['5d', '1mo', '3mo', '6mo'],
    '4h':  ['1mo', '3mo', '6mo', '1y'],
    '1d':  ['1mo', '3mo', '6mo', '1y', 'max'],
    '1wk': ['3mo', '6mo', '1y', 'max'],
}

const INTERVAL_OFFSET_MAP: Record<string, number> = {
    '1m': 3,
    '5m': 15,
    '15m': 15,
    '30m': 15,
    '1h': 60,
    '4h': 60,
    '1d': 90,
    '1wk': 365,
}

const RANGE_LABELS: Record<string, string> = {
    '1d': '1 day',
    '5d': '5 days',
    '1mo': '1 month',
    '3mo': '3 months',
    '6mo': '6 months',
    '1y': '1 year',
    'max': 'Max'
}

const toDate = (time: string | number | null): Date => {
    if (!time) return new Date();
    if (typeof time === "number") {
        return new Date(time*1000);
    }
    return new Date(time);
}

const toDateString = (time: string | number| Date): string => {
    if (time instanceof Date) return time.toISOString().split('T')[0];
    if (typeof time === "string") return time;
    return new Date(time*1000).toISOString().split('T')[0];
}

function ChartsPage() {
    const [symbol, setSymbol] = useState<string>('NOW');
    const [searchQuery, setSearchQuery] = useState<string>('NOW');
    const [interval, setInterval] = useState<string>('1d');
    const [range, setRange] = useState<string>('1mo');

    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);

    const chartContainerRef = useRef<HTMLDivElement>(null);
    const chartRef = useRef<any>(null)
    const candleSeriesRef = useRef<any>(null)
    const oldestTimeRef = useRef<string | number | null>(null)
    const isLoadingMoreRef = useRef<boolean>(false);
    const currentDataRef = useRef<CandleData[]>([])
    const currentSymbolRef = useRef<string>(searchQuery);
    const currentIntervalRef = useRef<string>(interval);
    const hasMoreDataRef = useRef<boolean>(true);

    const loadMoreData = async () => {
        if ( isLoadingMoreRef.current || !oldestTimeRef.current || !hasMoreDataRef.current ) return;
        isLoadingMoreRef.current = true;

        try {
            const currentOldestDate = toDate(oldestTimeRef.current);
            const startDate = new Date(currentOldestDate);

            const daysToSubtract = INTERVAL_OFFSET_MAP[currentIntervalRef.current] ?? 30;
            startDate.setDate(startDate.getDate() - daysToSubtract);

            const startParam = toDateString(startDate);
            const endParam = toDateString(oldestTimeRef.current);

            const response = await fetch(
                `http://localhost:8000/api/chart/${currentSymbolRef.current}?interval=${currentIntervalRef.current}&start=${startParam}&end=${endParam}`
            );

            if (!response.ok) throw new Error();

            const newData: ChartResponse = await response.json();
            const oldestTime = oldestTimeRef.current;

            if (!newData.candles || newData.candles.length === 0) {
                hasMoreDataRef.current = false;
                return;
            }

            const filteredNewData = newData.candles.filter(
                (item) => item.time < oldestTime
            );

            if (filteredNewData.length === 0) {
                hasMoreDataRef.current = false;
                return;
            }

            const mergedData = [...filteredNewData, ...currentDataRef.current];

            currentDataRef.current = mergedData;
            oldestTimeRef.current = mergedData[0].time;
            const timeScale = chartRef.current.timeScale();
            const logicalRange = timeScale.getVisibleLogicalRange();
            candleSeriesRef.current.setData(mergedData);
            if (logicalRange) {
                const addedBarsCount = filteredNewData.length;
                timeScale.setVisibleLogicalRange({
                    from: logicalRange.from + addedBarsCount,
                    to: logicalRange.to + addedBarsCount
                });
            }
        }catch(err) {
            console.error("Error during fetching data", err);
        } finally {
            isLoadingMoreRef.current = false;
        }
    };

    useEffect(() => {
        currentSymbolRef.current = searchQuery;
        currentIntervalRef.current = interval;
        hasMoreDataRef.current = true;
        const fetchChartData = async () => {
            setLoading(true);
            setError(null);
            try {
                const response = await fetch(
                    `http://localhost:8000/api/chart/${searchQuery}?interval=${interval}&range=${range}`
                );
                if (!response.ok){
                    throw new Error('Error fetching chart data');
                }
                const jsonData: ChartResponse = await response.json();
                currentDataRef.current = jsonData.candles;

                if (jsonData.candles.length > 0) {
                    oldestTimeRef.current = jsonData.candles[0].time;
                    if (candleSeriesRef.current) {
                        candleSeriesRef.current.setData(jsonData.candles);
                        chartRef.current.timeScale().fitContent();
                    }
                } else {
                    oldestTimeRef.current = null;
                    if (candleSeriesRef.current) candleSeriesRef.current.setData([]);
                }
            } catch (err: any) {
                setError(err.message || 'Something went wrong');
                currentDataRef.current = [];
                oldestTimeRef.current = null;
                if (candleSeriesRef.current) candleSeriesRef.current.setData([]);
            } finally {
                setLoading(false);
            }
        };

        fetchChartData();
    }, [searchQuery,interval, range, ]);

    useEffect(() => {
        if (!chartContainerRef.current) return;

       const chart = createChart(chartContainerRef.current, {
           width: chartContainerRef.current.clientWidth,
           height: 400,
           layout: {
               background: { color: '#ffffff' },
               textColor: '#333333',
           },
           grid: {
               vertLines: { color: '#f0f0f0' },
               horzLines: { color: '#f0f0f0' },
           },
       });

       const candleSeries = chart.addSeries(CandlestickSeries, {
           upColor: '#26a69a',
           downColor: '#ef5350',
           borderVisible: false,
           wickUpColor: '#26a69a',
           wickDownColor: '#ef5350',
       });

       chartRef.current = chart;
       candleSeriesRef.current = candleSeries;

       chart.timeScale().subscribeVisibleLogicalRangeChange((range) => {
           if (range && range.from < 2) {
               loadMoreData();
           }
       });

       const handleResize = () => {
           if (chartContainerRef.current && chartRef.current) {
               chartRef.current.applyOptions({
                   width: chartContainerRef.current.clientWidth,
               });
           }
       };

       window.addEventListener('resize', handleResize);

       return () => {
           window.removeEventListener('resize', handleResize);
           chart.remove();
           chartRef.current = null;
           candleSeriesRef.current = null;
       };
    }, []);

    const handleSearch = (e: React.SyntheticEvent) => {
        e.preventDefault();
        if (symbol.trim()) {
            setSearchQuery(symbol.toUpperCase().trim());
        }
    };

    const handleIntervalChange = (newInterval: string) => {
        setInterval(newInterval);
        const availableRanges = INTERVAL_RANGE_MAP[newInterval] ?? ['1mo']
        setRange(availableRanges[0])
    }

    return (
        <div className="p-6 max-w-7xl mx-auto font-sans">
            <h1 className="text-3xl font-bold text-gray-800 mb-6">Charts Page</h1>
            <form onSubmit={handleSearch} className="mb-6 flex flex-wrap gap-4 items-center bg-gray-50 p-4 rounded-xl border border-gray-200 shadow-sm">
                <div className="relative">
                    <input
                        type="text"
                        value={symbol}
                        onChange={(e) => setSymbol(e.target.value)}
                        placeholder="Symbol (np. AAPL)"
                        className="px-4 py-2 border border-gray-300 rounded-lg text-base focus:outline-none focus:ring-2 focus:ring-blue-500 uppercase font-semibold w-48 bg-white"
                    />
                </div>
                <select
                    value={interval}
                    onChange={(e) => handleIntervalChange(e.target.value)}
                    className="px-3 py-2 border border-gray-300 rounded-lg bg-white shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                    <option value="1m">1 minute</option>
                    <option value="5m">5 minutes</option>
                    <option value="15m">15 minutes</option>
                    <option value="30m">30 minutes</option>
                    <option value="1h">1 hour</option>
                    <option value="4h">4 hours</option>
                    <option value="1d">1 day</option>
                    <option value="1wk">1 week</option>
                </select>
                <select
                    value={range}
                    onChange={(e) => setRange(e.target.value)}
                    className="px-3 py-2 border border-gray-300 rounded-lg bg-white shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                    {(INTERVAL_RANGE_MAP[interval] ?? ['1mo', '3mo', '6mo', '1y', 'max']).map(r => (
                        <option key={r} value={r}>
                            {RANGE_LABELS[r] ?? r}
                        </option>
                    ))}
                </select>
                <button
                    type="submit"
                    className="px-5 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg shadow-sm transition-colors duration-150 cursor-pointer"
                >
                    Szukaj
                </button>
            </form>
            {loading && (
                <div className="flex items-center gap-2 text-gray-600 mb-4 animate-pulse">
                    <span>Ładowanie danych z rynku...</span>
                </div>
            )}
            {error && (
                <div className="p-4 mb-4 bg-red-50 border border-red-200 text-red-700 rounded-lg">
                    <strong>Błąd:</strong> {error}
                </div>
            )}
            <div
                ref={chartContainerRef}
                className="relative border border-gray-200 rounded-xl mt-4 bg-white p-2 shadow-md min-h-[400px]"
            />
        </div>
    );
}
export default ChartsPage