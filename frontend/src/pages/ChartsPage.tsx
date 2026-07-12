import {useEffect, useRef, useState} from "react";
import {ChartResponse, CandleData, SearchResult} from "../types/market.ts";
import {INTERVAL_DEFAULT_RANGE, INTERVAL_OFFSET_MAP, toDate, toDateString} from "../utils/chartHelpers.ts"
import CandleStickChart from "../components/CandleStickChart.tsx";

function ChartsPage() {
    const [symbol, setSymbol] = useState<string>("");
    const [searchQuery, setSearchQuery] = useState<string>("");
    const [interval, setInterval] = useState<string>('1d');
    const [, setLoading] = useState<boolean>(false);
    const [, setError] = useState<string | null>(null);
    const [chartData, setChartData] = useState<CandleData[]>([]);
    const [suggestion, setSuggestion] = useState<SearchResult[]>([]);
    const [showSuggestion, setShowSuggestion] = useState<boolean>(false);

    const oldestTimeRef = useRef<string | number | null>(null)
    const isLoadingMoreRef = useRef<boolean>(false);
    const currentDataRef = useRef<CandleData[]>([])
    const currentSymbolRef = useRef<string>(searchQuery);
    const currentIntervalRef = useRef<string>(interval);
    const hasMoreDataRef = useRef<boolean>(true);
    const lastSelectedRef = useRef<string>('');

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

    useEffect(() => {
        if (symbol.length < 2) {
            setSuggestion([]);
            return;
        }
        if (symbol === lastSelectedRef.current) return;
        const timer = setTimeout(async() => {
            const response = await fetch(`http://localhost:8000/api/search?q=${symbol}`)
            const data = await response.json();
            setSuggestion(data);
            setShowSuggestion(true);
        }, 300)
        return () => clearTimeout(timer);
    }, [symbol]);

    const handleSelectSuggestion = (symbol: string) => {
        setSuggestion([]);
        setShowSuggestion(false)
        setSymbol(symbol);
        setSearchQuery(symbol);
        lastSelectedRef.current = symbol;
    }

    return (
        <div className="w-[calc(100%-4rem)] max-w-full font-sans flex flex-col lg:flex-row gap-4 p-4 min-h-dvh text-white ml-16">
            <div className="flex-1 min-w-75 h-full lg:h-auto rounded-xl overflow-hidden">
                <CandleStickChart data ={chartData} onNeedMoreData= {loadMoreData}/>
            </div>
            <div className="w-full lg:w-[25vw] min-w-70 bg-gray-900 p-3 rounded-xl flex flex-col gap-3">
                <div className="w-full gap-2 flex flex-col items-stretch">
                    <div className="relative">
                    <input
                        type="text"
                        value={symbol}
                        onChange={(e) => setSymbol(e.target.value)}
                        onKeyDown={(e) => {
                            if (e.key === "Enter" && symbol.trim()) {
                                setSearchQuery(symbol.toUpperCase().trim());
                                setShowSuggestion(false);
                            }
                        }}
                        placeholder="Symbol (np. AAPL)"
                        className="px-2 py-1 rounded-xl text-white text-base uppercase font-semibold w-full bg-gray-600 outline-none text-center"
                    />
                    {showSuggestion && suggestion.length > 0 && (
                        <div className="absolute top-full left-0 z-50 rounded-xl mt-1 w-full bg-gray-600">
                            {suggestion.map((item) => (
                                <div key = {item.symbol} onClick={()=>handleSelectSuggestion(item.symbol)}
                                className="px-4 py-2 hover:bg-gray-500 cursor-pointer rounded-xl">
                                <span className="font-semibold">{item.symbol}</span>
                                <span className="text-sm ml-2">{item.name}</span>
                                </div>
                            ))}
                        </div>
                    )}
                    </div>
                    <select
                        value={interval}
                        onChange={(e) => setInterval(e.target.value)}
                        className="px-2 py-1 rounded-xl text-white bg-gray-600 w-full font-semibold outline-none text-center"
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
                </div>
            </div>
        </div>
    );
}
export default ChartsPage