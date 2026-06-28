import React, {useEffect, useRef, useState} from "react";
import {createChart, CandlestickSeries} from "lightweight-charts";
import {CandleData} from "../types/market.ts";

function ChartsPage() {
    const [symbol, setSymbol] = useState<string>('NOW');
    const [searchQuery, setSearchQuery] = useState<string>('NOW');
    const [interval, setInterval] = useState<string>('1d');
    const [range, setRange] = useState<string>('1mo');

    const [data, setData] = useState<CandleData[]>([])
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);

    const chartContainerRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
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
                const jsonData = await response.json();
                setData(jsonData);
            } catch (err: any) {
                setError(err.message || 'Something went wrong');
                setData([]);
            } finally {
                setLoading(false);
            }
        };

        fetchChartData();
    }, [searchQuery,interval, range]);

    useEffect(() => {
        if (!chartContainerRef.current || data.length === 0) return;

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

       candleSeries.setData(data);
       chart.timeScale().fitContent();
       return () => {
           chart.remove();
       };
    }, [data]);

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
                    <option value="1m">1 min</option>
                    <option value="5m">5 min</option>
                    <option value="15m">15 min</option>
                    <option value="1h">1 hour</option>
                    <option value="1d">1 day</option>
                    <option value="1wk">1 week</option>
                </select>

                <select
                    value={range}
                    onChange={(e) => setRange(e.target.value)}
                    className="px-3 py-2 border border-gray-300 rounded-lg bg-white shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                    <option value="1d">1 day</option>
                    <option value="5d">5 days</option>
                    <option value="1mo">1 month</option>
                    <option value="3mo">3 months</option>
                    <option value="6mo">6 months</option>
                    <option value="1y">1 year</option>
                    <option value="max">max</option>
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