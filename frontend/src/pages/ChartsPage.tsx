import React, {useEffect, useRef, useState} from "react";
import {ChartResponse, CandleData} from "../types/market.ts";
import {INTERVAL_DEFAULT_RANGE, INTERVAL_OFFSET_MAP, toDate, toDateString} from "../utils/chartHelpers.ts"
import CandleStickChart from "../components/CandleStickChart.tsx";

function ChartsPage() {
    const [symbol, setSymbol] = useState<string>('NOW');
    const [searchQuery, setSearchQuery] = useState<string>('NOW');
    const [interval, setInterval] = useState<string>('1d');
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [chartData, setChartData] = useState<CandleData[]>([]);


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
            setChartData([...mergedData]);
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
        const range = INTERVAL_DEFAULT_RANGE[currentIntervalRef.current]
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
                    setChartData(jsonData.candles);
                } else {
                    oldestTimeRef.current = null;
                    setChartData([]);
                }
            } catch (err: any) {
                setError(err.message || 'Something went wrong');
                currentDataRef.current = [];
                oldestTimeRef.current = null;
                setChartData([]);
            } finally {
                setLoading(false);
            }
        };

        fetchChartData();
    }, [searchQuery, interval]);

    const handleSearch = (e: React.SyntheticEvent) => {
        e.preventDefault();
        if (symbol.trim()) {
            setSearchQuery(symbol.toUpperCase().trim());
        }
    };

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
                    onChange={(e) => setInterval(e.target.value)}
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
            <CandleStickChart data ={chartData} onNeedMoreData= {loadMoreData}/>
        </div>
    );
}
export default ChartsPage