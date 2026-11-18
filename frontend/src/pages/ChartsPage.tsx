import {useEffect, useMemo, useRef, useState} from "react";
import {ChevronDown, Plus, X} from "lucide-react";
import {CandleData, HoveredCandle, IndicatorData, RawIndicatorsResponse, SearchResult} from "../types/market.ts";
import {INTERVAL_DEFAULT_RANGE, INTERVAL_OFFSET_MAP, toDate, toDateString} from "../utils/chartHelpers.ts"
import CandleStickChart from "../components/CandleStickChart.tsx";
import {fetchChartData, fetchIndicators, searchSymbols} from "../api/client.ts";

const INTERVALS = [
    { value: "1m", label: "1m" },
    { value: "5m", label: "5m" },
    { value: "15m", label: "15m" },
    { value: "30m", label: "30m" },
    { value: "1h", label: "1h" },
    { value: "4h", label: "4h" },
    { value: "1d", label: "1D" },
    { value: "1wk", label: "1W" },
];

const SMA_COLORS = ["#38bdf8", "#60a5fa", "#2dd4bf", "#818cf8", "#34d399"];
const EMA_COLORS = ["#f59e0b", "#fb923c", "#facc15", "#f87171", "#e879f9"];
const DEFAULT_MA_PERIODS = [20, 50, 100, 200, 9];

interface MovingAverageItem {
    id: string;
    type: "sma" | "ema";
    period: number;
    enabled: boolean;
    color: string;
}

function buildIndicatorsMap(
    maItems: MovingAverageItem[],
    bbEnabled: boolean,
    bbPeriod: number,
    rsiEnabled: boolean,
    rsiPeriod: number
): Record<string, number> {
    const map: Record<string, number> = {};
    for (const item of maItems) {
        if (item.enabled) {
            map[`${item.type}:${item.period}`] = item.period;
        }
    }
    if (bbEnabled) map.bb = bbPeriod;
    if (rsiEnabled) map.rsi = rsiPeriod;
    return map;
}

function formatIndicatorsParam(indicatorsMap: Record<string, number>): string | undefined {
    const entries = Object.entries(indicatorsMap);
    if (entries.length === 0) return undefined;
    return entries
        .map(([key, period]) => (key.includes(":") ? key : `${key}:${period}`))
        .join(",");
}

function ChartsPage() {
    const [symbol, setSymbol] = useState<string>("");
    const [searchQuery, setSearchQuery] = useState<string>("");
    const [interval, setInterval] = useState<string>('1d');
    const [, setLoading] = useState<boolean>(false);
    const [, setError] = useState<string | null>(null);
    const [chartData, setChartData] = useState<CandleData[]>([]);
    const [rawIndicators, setRawIndicators] = useState<RawIndicatorsResponse>({});

    const [maItems, setMaItems] = useState<MovingAverageItem[]>([
        { id: "sma-1", type: "sma", period: 20, enabled: false, color: SMA_COLORS[0] },
        { id: "ema-1", type: "ema", period: 20, enabled: false, color: EMA_COLORS[0] },
    ]);
    const [bbEnabled, setBbEnabled] = useState<boolean>(false);
    const [bbPeriod, setBbPeriod] = useState<number>(20);
    const [bbColor, setBbColor] = useState<string>("#a855f7");
    const [rsiEnabled, setRsiEnabled] = useState<boolean>(false);
    const [rsiPeriod, setRsiPeriod] = useState<number>(14);
    const [rsiColor, setRsiColor] = useState<string>("#ec4899");

    const [isIndicatorMenuOpen, setIsIndicatorMenuOpen] = useState<boolean>(false);
    const [hoveredCandle, setHoveredCandle] = useState<HoveredCandle | null>(null);
    const [suggestion, setSuggestion] = useState<SearchResult[]>([]);
    const [showSuggestion, setShowSuggestion] = useState<boolean>(false);

    const oldestTimeRef = useRef<string | number | null>(null)
    const isLoadingMoreRef = useRef<boolean>(false);
    const currentDataRef = useRef<CandleData[]>([])
    const currentSymbolRef = useRef<string>(searchQuery);
    const currentIntervalRef = useRef<string>(interval);
    const hasMoreDataRef = useRef<boolean>(true);
    const lastSelectedRef = useRef<string>('');
    const indicatorMenuRef = useRef<HTMLDivElement>(null);
    const nextMaIdRef = useRef<number>(2);

    const activeIndicatorsMap = useMemo(
        () => buildIndicatorsMap(maItems, bbEnabled, bbPeriod, rsiEnabled, rsiPeriod),
        [maItems, bbEnabled, bbPeriod, rsiEnabled, rsiPeriod]
    );
    const activeIndicatorsJson = JSON.stringify(activeIndicatorsMap);
    const activeIndicatorsMapRef = useRef(activeIndicatorsMap);

    useEffect(() => {
        activeIndicatorsMapRef.current = activeIndicatorsMap;
    }, [activeIndicatorsMap]);

    const indicatorData: IndicatorData = useMemo(() => {
        const movingAverages = maItems
            .filter((item) => item.enabled)
            .map((item) => {
                const apiKey = `${item.type}:${item.period}`;
                return {
                    key: item.id,
                    color: item.color,
                    data: rawIndicators[apiKey] ?? [],
                };
            });

        return {
            movingAverages,
            bb: bbEnabled ? rawIndicators.bb : undefined,
            bbColor,
            rsi: rsiEnabled ? rawIndicators.rsi : undefined,
            rsiColor,
        };
    }, [rawIndicators, maItems, bbEnabled, bbColor, rsiEnabled, rsiColor]);

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (
                indicatorMenuRef.current &&
                !indicatorMenuRef.current.contains(event.target as Node)
            ) {
                setIsIndicatorMenuOpen(false);
            }
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    const loadMoreData = async () => {
        if (isLoadingMoreRef.current || !oldestTimeRef.current || !hasMoreDataRef.current) return;
        isLoadingMoreRef.current = true;

        try {
            const currentOldestDate = toDate(oldestTimeRef.current);
            const startDate = new Date(currentOldestDate);

            const daysToSubtract = INTERVAL_OFFSET_MAP[currentIntervalRef.current] ?? 30;
            startDate.setDate(startDate.getDate() - daysToSubtract);

            const endDate = new Date(currentOldestDate);
            endDate.setDate(endDate.getDate() +1);
            const startParam = toDateString(startDate);
            const endParam = toDateString(endDate);

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

            const map = activeIndicatorsMapRef.current;
            if (Object.keys(map).length > 0) {
                const recalculated = await fetchIndicators(mergedData, map);
                setRawIndicators(recalculated);
            }
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
        setHoveredCandle(null);
        const range = INTERVAL_DEFAULT_RANGE[currentIntervalRef.current];

        const loadInitialChartData = async () => {
            setLoading(true);
            setError(null);
            try {
                const map = activeIndicatorsMapRef.current;
                const jsonData = await fetchChartData(searchQuery, {
                    interval,
                    range,
                    indicators: formatIndicatorsParam(map),
                });
                currentDataRef.current = jsonData.candles;

                if (jsonData.candles.length > 0) {
                    oldestTimeRef.current = jsonData.candles[0].time;
                    setChartData(jsonData.candles);
                    setRawIndicators(jsonData);
                } else {
                    oldestTimeRef.current = null;
                    setChartData([]);
                    setRawIndicators({});
                }
            } catch (err: any) {
                setError(err.message || 'Something went wrong');
                currentDataRef.current = [];
                oldestTimeRef.current = null;
                setChartData([]);
                setRawIndicators({});
            } finally {
                setLoading(false);
            }
        };

        loadInitialChartData();
    }, [searchQuery, interval]);

    useEffect(() => {
        const updateIndicators = async () => {
            if (currentDataRef.current.length === 0) return;
            const map = activeIndicatorsMapRef.current;
            if (Object.keys(map).length === 0) {
                setRawIndicators({});
                return;
            }
            try {
                const calculated = await fetchIndicators(currentDataRef.current, map);
                setRawIndicators(calculated);
            } catch (err) {
                console.error("Error calculating indicators", err);
            }
        };
        updateIndicators();
    }, [activeIndicatorsJson]);

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

    const addMovingAverage = (type: "sma" | "ema") => {
        setMaItems((prev) => {
            const sameTypeCount = prev.filter((i) => i.type === type).length;
            const palette = type === "sma" ? SMA_COLORS : EMA_COLORS;
            const color = palette[sameTypeCount % palette.length];
            const period = DEFAULT_MA_PERIODS[sameTypeCount % DEFAULT_MA_PERIODS.length];
            const id = `${type}-${nextMaIdRef.current++}`;
            return [...prev, { id, type, period, enabled: true, color }];
        });
    };

    const toggleMovingAverage = (id: string) => {
        setMaItems((prev) =>
            prev.map((item) => (item.id === id ? { ...item, enabled: !item.enabled } : item))
        );
    };

    const updateMovingAveragePeriod = (id: string, rawValue: string) => {
        const parsed = Math.max(1, parseInt(rawValue, 10) || 1);
        setMaItems((prev) =>
            prev.map((item) => (item.id === id ? { ...item, period: parsed } : item))
        );
    };

    const updateMovingAverageColor = (id: string, color: string) => {
        setMaItems((prev) =>
            prev.map((item) => (item.id === id ? { ...item, color } : item))
        );
    };

    const removeMovingAverage = (id: string) => {
        setMaItems((prev) => prev.filter((item) => item.id !== id));
    };

    const displayedCandle: HoveredCandle | null =
        hoveredCandle ?? (chartData.length > 0 ? chartData[chartData.length - 1] : null);

    const candleColor = displayedCandle
        ? displayedCandle.close >= displayedCandle.open
            ? "text-emerald-400"
            : "text-rose-400"
        : "";

    const latestRsi =
        indicatorData.rsi && indicatorData.rsi.length > 0
            ? indicatorData.rsi[indicatorData.rsi.length - 1].value
            : undefined;

    const displayedRsi = hoveredCandle ? hoveredCandle.rsi : latestRsi;
    const activeCount =
        maItems.filter((i) => i.enabled).length +
        (bbEnabled ? 1 : 0) +
        (rsiEnabled ? 1 : 0);

    const smaList = maItems.filter((i) => i.type === "sma");
    const emaList = maItems.filter((i) => i.type === "ema");

    return (
        <div className="flex flex-col lg:flex-row gap-4 p-4 sm:p-6 min-h-dvh lg:h-dvh">
            <div className="flex-1 flex flex-col gap-3 min-h-96">
                <div className="flex flex-wrap items-center gap-3 px-3 py-2 bg-slate-900 border border-slate-800 rounded-xl">
                    <div className="relative w-full sm:w-60">
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
                            className="w-full px-3 py-1.5 rounded-lg bg-slate-800 border border-slate-700 text-sm text-slate-100 uppercase font-medium placeholder:normal-case placeholder:text-slate-500 focus:border-slate-500 outline-none"
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

                    <div className="relative" ref={indicatorMenuRef}>
                        <button
                            type="button"
                            onClick={() => setIsIndicatorMenuOpen((prev) => !prev)}
                            className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-slate-800 border border-slate-700 hover:border-slate-600 text-sm text-slate-200 font-medium transition-colors cursor-pointer"
                        >
                            <span>Indicators</span>
                            {activeCount > 0 && (
                                <span className="px-1.5 py-0.5 text-xs rounded bg-slate-700 text-slate-100">
                                    {activeCount}
                                </span>
                            )}
                            <ChevronDown size={15} className="text-slate-400" />
                        </button>

                        {isIndicatorMenuOpen && (
                            <div className="absolute top-full left-0 z-50 mt-1 w-72 p-2.5 bg-slate-800 border border-slate-700 rounded-lg shadow-xl flex flex-col gap-2">
                                <div className="flex flex-col gap-1">
                                    <div className="flex items-center justify-between px-1">
                                        <span className="text-[11px] font-semibold uppercase tracking-wider text-slate-400">
                                            SMA
                                        </span>
                                        <button
                                            type="button"
                                            onClick={() => addMovingAverage("sma")}
                                            className="flex items-center gap-1 px-1.5 py-0.5 text-xs text-sky-400 hover:bg-slate-700 rounded transition-colors cursor-pointer"
                                        >
                                            <Plus size={12} />
                                            <span>Add</span>
                                        </button>
                                    </div>
                                    {smaList.map((item) => (
                                        <div
                                            key={item.id}
                                            className="flex items-center justify-between gap-2 px-2 py-1 rounded-md hover:bg-slate-700/50"
                                        >
                                            <div className="flex items-center gap-2 text-xs text-slate-200 flex-1">
                                                <input
                                                    type="checkbox"
                                                    checked={item.enabled}
                                                    onChange={() => toggleMovingAverage(item.id)}
                                                    className="rounded border-slate-600 bg-slate-900 accent-slate-500 cursor-pointer"
                                                />
                                                <label
                                                    title="Change line color"
                                                    className="relative w-3.5 h-3.5 rounded-full shrink-0 cursor-pointer ring-1 ring-white/20 hover:scale-110 transition-transform"
                                                    style={{ backgroundColor: item.color }}
                                                >
                                                    <input
                                                        type="color"
                                                        value={item.color}
                                                        onChange={(e) => updateMovingAverageColor(item.id, e.target.value)}
                                                        className="sr-only"
                                                    />
                                                </label>
                                                <span
                                                    onClick={() => toggleMovingAverage(item.id)}
                                                    className="font-medium cursor-pointer select-none"
                                                >
                                                    SMA
                                                </span>
                                            </div>
                                            <input
                                                type="number"
                                                min={1}
                                                max={500}
                                                value={item.period}
                                                onChange={(e) => updateMovingAveragePeriod(item.id, e.target.value)}
                                                className="w-14 px-1.5 py-0.5 text-xs text-center font-mono rounded bg-slate-900 border border-slate-700 text-slate-200 outline-none focus:border-slate-500"
                                            />
                                            {smaList.length > 1 && (
                                                <button
                                                    type="button"
                                                    onClick={() => removeMovingAverage(item.id)}
                                                    className="text-slate-500 hover:text-rose-400 p-0.5 cursor-pointer"
                                                >
                                                    <X size={13} />
                                                </button>
                                            )}
                                        </div>
                                    ))}
                                </div>

                                <div className="border-t border-slate-700/60 pt-2 flex flex-col gap-1">
                                    <div className="flex items-center justify-between px-1">
                                        <span className="text-[11px] font-semibold uppercase tracking-wider text-slate-400">
                                            EMA
                                        </span>
                                        <button
                                            type="button"
                                            onClick={() => addMovingAverage("ema")}
                                            className="flex items-center gap-1 px-1.5 py-0.5 text-xs text-amber-400 hover:bg-slate-700 rounded transition-colors cursor-pointer"
                                        >
                                            <Plus size={12} />
                                            <span>Add</span>
                                        </button>
                                    </div>
                                    {emaList.map((item) => (
                                        <div
                                            key={item.id}
                                            className="flex items-center justify-between gap-2 px-2 py-1 rounded-md hover:bg-slate-700/50"
                                        >
                                            <div className="flex items-center gap-2 text-xs text-slate-200 flex-1">
                                                <input
                                                    type="checkbox"
                                                    checked={item.enabled}
                                                    onChange={() => toggleMovingAverage(item.id)}
                                                    className="rounded border-slate-600 bg-slate-900 accent-slate-500 cursor-pointer"
                                                />
                                                <label
                                                    title="Change line color"
                                                    className="relative w-3.5 h-3.5 rounded-full shrink-0 cursor-pointer ring-1 ring-white/20 hover:scale-110 transition-transform"
                                                    style={{ backgroundColor: item.color }}
                                                >
                                                    <input
                                                        type="color"
                                                        value={item.color}
                                                        onChange={(e) => updateMovingAverageColor(item.id, e.target.value)}
                                                        className="sr-only"
                                                    />
                                                </label>
                                                <span
                                                    onClick={() => toggleMovingAverage(item.id)}
                                                    className="font-medium cursor-pointer select-none"
                                                >
                                                    EMA
                                                </span>
                                            </div>
                                            <input
                                                type="number"
                                                min={1}
                                                max={500}
                                                value={item.period}
                                                onChange={(e) => updateMovingAveragePeriod(item.id, e.target.value)}
                                                className="w-14 px-1.5 py-0.5 text-xs text-center font-mono rounded bg-slate-900 border border-slate-700 text-slate-200 outline-none focus:border-slate-500"
                                            />
                                            {emaList.length > 1 && (
                                                <button
                                                    type="button"
                                                    onClick={() => removeMovingAverage(item.id)}
                                                    className="text-slate-500 hover:text-rose-400 p-0.5 cursor-pointer"
                                                >
                                                    <X size={13} />
                                                </button>
                                            )}
                                        </div>
                                    ))}
                                </div>

                                <div className="border-t border-slate-700/60 pt-2 flex flex-col gap-1">
                                    <div className="flex items-center justify-between gap-2 px-2 py-1 rounded-md hover:bg-slate-700/50">
                                        <div className="flex items-center gap-2 text-xs text-slate-200 flex-1">
                                            <input
                                                type="checkbox"
                                                checked={bbEnabled}
                                                onChange={() => setBbEnabled((v) => !v)}
                                                className="rounded border-slate-600 bg-slate-900 accent-slate-500 cursor-pointer"
                                            />
                                            <label
                                                title="Change line color"
                                                className="relative w-3.5 h-3.5 rounded-full shrink-0 cursor-pointer ring-1 ring-white/20 hover:scale-110 transition-transform"
                                                style={{ backgroundColor: bbColor }}
                                            >
                                                <input
                                                    type="color"
                                                    value={bbColor}
                                                    onChange={(e) => setBbColor(e.target.value)}
                                                    className="sr-only"
                                                />
                                            </label>
                                            <span
                                                onClick={() => setBbEnabled((v) => !v)}
                                                className="font-medium cursor-pointer select-none"
                                            >
                                                Bollinger Bands
                                            </span>
                                        </div>
                                        <input
                                            type="number"
                                            min={1}
                                            max={500}
                                            value={bbPeriod}
                                            onChange={(e) => setBbPeriod(Math.max(1, parseInt(e.target.value, 10) || 1))}
                                            className="w-14 px-1.5 py-0.5 text-xs text-center font-mono rounded bg-slate-900 border border-slate-700 text-slate-200 outline-none focus:border-slate-500"
                                        />
                                    </div>

                                    <div className="flex items-center justify-between gap-2 px-2 py-1 rounded-md hover:bg-slate-700/50">
                                        <div className="flex items-center gap-2 text-xs text-slate-200 flex-1">
                                            <input
                                                type="checkbox"
                                                checked={rsiEnabled}
                                                onChange={() => setRsiEnabled((v) => !v)}
                                                className="rounded border-slate-600 bg-slate-900 accent-slate-500 cursor-pointer"
                                            />
                                            <label
                                                title="Change line color"
                                                className="relative w-3.5 h-3.5 rounded-full shrink-0 cursor-pointer ring-1 ring-white/20 hover:scale-110 transition-transform"
                                                style={{ backgroundColor: rsiColor }}
                                            >
                                                <input
                                                    type="color"
                                                    value={rsiColor}
                                                    onChange={(e) => setRsiColor(e.target.value)}
                                                    className="sr-only"
                                                />
                                            </label>
                                            <span
                                                onClick={() => setRsiEnabled((v) => !v)}
                                                className="font-medium cursor-pointer select-none"
                                            >
                                                RSI
                                            </span>
                                        </div>
                                        <input
                                            type="number"
                                            min={1}
                                            max={500}
                                            value={rsiPeriod}
                                            onChange={(e) => setRsiPeriod(Math.max(1, parseInt(e.target.value, 10) || 1))}
                                            className="w-14 px-1.5 py-0.5 text-xs text-center font-mono rounded bg-slate-900 border border-slate-700 text-slate-200 outline-none focus:border-slate-500"
                                        />
                                    </div>
                                </div>
                            </div>
                        )}
                    </div>

                    {displayedCandle && (
                        <div className="flex flex-wrap items-center gap-4 text-xs font-mono ml-auto sm:ml-2">
                            <div>
                                <span className="text-slate-500 mr-1.5">Open</span>
                                <span className={candleColor}>{displayedCandle.open.toFixed(2)}</span>
                            </div>
                            <div>
                                <span className="text-slate-500 mr-1.5">High</span>
                                <span className={candleColor}>{displayedCandle.high.toFixed(2)}</span>
                            </div>
                            <div>
                                <span className="text-slate-500 mr-1.5">Low</span>
                                <span className={candleColor}>{displayedCandle.low.toFixed(2)}</span>
                            </div>
                            <div>
                                <span className="text-slate-500 mr-1.5">Close</span>
                                <span className={candleColor}>{displayedCandle.close.toFixed(2)}</span>
                            </div>
                            {displayedRsi !== undefined && (
                                <div>
                                    <span className="text-slate-500 mr-1.5">RSI</span>
                                    <span style={{ color: rsiColor }}>{displayedRsi.toFixed(2)}</span>
                                </div>
                            )}
                        </div>
                    )}
                </div>

                <div className="flex-1 min-h-80 bg-slate-900 border border-slate-800 rounded-xl overflow-hidden">
                    <CandleStickChart
                        data={chartData}
                        indicators={indicatorData}
                        onNeedMoreData={loadMoreData}
                        onCrosshairMove={setHoveredCandle}
                    />
                </div>

                <div className="flex items-center gap-1.5 px-3 py-2 bg-slate-900 border border-slate-800 rounded-xl overflow-x-auto">
                    {INTERVALS.map(({ value, label }) => (
                        <button
                            key={value}
                            type="button"
                            onClick={() => setInterval(value)}
                            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-colors cursor-pointer ${
                                interval === value
                                    ? 'bg-slate-700 text-white'
                                    : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800'
                            }`}
                        >
                            {label}
                        </button>
                    ))}
                </div>
            </div>

            <aside className="w-full lg:w-80 shrink-0 bg-slate-900 border border-slate-800 p-4 rounded-xl flex flex-col gap-3">
                <h3 className="text-slate-400 text-xs font-semibold uppercase tracking-wider">
                    Watchlist
                </h3>
            </aside>
        </div>
    );
}

export default ChartsPage
