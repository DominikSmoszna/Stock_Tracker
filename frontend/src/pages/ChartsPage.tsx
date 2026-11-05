import {useEffect, useRef, useState} from "react";
import {CandleData, SearchResult} from "../types/market.ts";
import {INTERVAL_DEFAULT_RANGE, INTERVAL_OFFSET_MAP, toDate, toDateString} from "../utils/chartHelpers.ts"
import CandleStickChart from "../components/CandleStickChart.tsx";
import {fetchChartData, searchSymbols} from "../api/client.ts";

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
        if (isLoadingMoreRef.current || !oldestTimeRef.current || !hasMoreDataRef.current) return;
        isLoadingMoreRef.current = true;

        try {
            const currentOldestDate = toDate(oldestTimeRef.current);
            const startDate = new Date(currentOldestDate);

            const daysToSubtract = INTERVAL_OFFSET_MAP[currentIntervalRef.current] ?? 30;
            startDate.setDate(startDate.getDate() - daysToSubtract);

            const startParam = toDateString(startDate);
            const endParam = toDateString(oldestTimeRef.current);

            const newData = await fetchChartData(currentSymbolRef.current, {
                interval: currentIntervalRef.current,
                start: startParam,
                end: endParam,
            });
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
        } catch (err) {
            console.error("Error during fetching data", err);
        } finally {
            isLoadingMoreRef.current = false;
        }
    };

    useEffect(() => {
        if (!searchQuery) return;
        currentSymbolRef.current = searchQuery;
        currentIntervalRef.current = interval;
        hasMoreDataRef.current = true;
        const range = INTERVAL_DEFAULT_RANGE[currentIntervalRef.current];

        const loadInitialChartData = async () => {
            setLoading(true);
            setError(null);
            try {
                const jsonData = await fetchChartData(searchQuery, {
                    interval,
                    range,
                });
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

        loadInitialChartData();
    }, [searchQuery, interval]);

    useEffect(() => {
        if (symbol.length < 2) {
            setSuggestion([]);
            return;
        }
        if (symbol === lastSelectedRef.current) return;
        const timer = setTimeout(async () => {
            const data = await searchSymbols(symbol);
            setSuggestion(data);
            setShowSuggestion(true);
        }, 300);
        return () => clearTimeout(timer);
    }, [symbol]);

    const handleSelectSuggestion = (selectedSymbol: string) => {
        setSuggestion([]);
        setShowSuggestion(false);
        setSymbol(selectedSymbol);
        setSearchQuery(selectedSymbol);
        lastSelectedRef.current = selectedSymbol;
    };

    return (
        <div className="flex flex-col lg:flex-row gap-4 p-4 sm:p-6 min-h-dvh lg:h-dvh">
            <div className="flex-1 min-h-96 bg-slate-900 border border-slate-800 rounded-xl overflow-hidden">
                <CandleStickChart data={chartData} onNeedMoreData={loadMoreData}/>
            </div>

            <aside className="w-full lg:w-80 shrink-0 bg-slate-900 border border-slate-800 p-4 rounded-xl flex flex-col gap-3">
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
                        placeholder="Symbol (e.g. AAPL)"
                        className="w-full px-3 py-2 rounded-lg bg-slate-800 border border-slate-700 text-sm text-slate-100 uppercase font-medium placeholder:normal-case placeholder:text-slate-500 focus:border-slate-500 outline-none"
                    />
                    {showSuggestion && suggestion.length > 0 && (
                        <div className="absolute top-full left-0 z-50 mt-1 w-full bg-slate-800 border border-slate-700 rounded-lg shadow-xl overflow-hidden divide-y divide-slate-700/60">
                            {suggestion.map((item) => (
                                <button
                                    type="button"
                                    key={item.symbol}
                                    onClick={() => handleSelectSuggestion(item.symbol)}
                                    className="w-full text-left px-3 py-2 hover:bg-slate-700/70 transition-colors flex items-baseline justify-between gap-2"
                                >
                                    <span className="text-sm font-semibold text-slate-100">{item.symbol}</span>
                                    <span className="text-xs text-slate-400 truncate">{item.name}</span>
                                </button>
                            ))}
                        </div>
                    )}
                </div>

                <select
                    value={interval}
                    onChange={(e) => setInterval(e.target.value)}
                    className="w-full px-3 py-2 rounded-lg bg-slate-800 border border-slate-700 text-sm text-slate-100 font-medium focus:border-slate-500 outline-none"
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
            </aside>
        </div>
    );
}

export default ChartsPage
