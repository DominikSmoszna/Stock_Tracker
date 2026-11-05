import {useEffect, useRef} from "react";
import {CandlestickSeries, createChart, TickMarkType} from "lightweight-charts";
import {CandlestickChartProps} from "../types/market.ts"

function CandleStickChart({data, onNeedMoreData}: CandlestickChartProps) {

    const chartContainerRef = useRef<HTMLDivElement>(null)
    const chartRef = useRef<any>(null)
    const candleSeriesRef = useRef<any>(null)
    const onNeedMoreDataRef = useRef(onNeedMoreData)
    const prevDataLengthRef = useRef<number>(0)
    const prevFirstTimeRef = useRef<number | string>(0)

    useEffect(() => {
        onNeedMoreDataRef.current = onNeedMoreData
    }, [onNeedMoreData]);

    useEffect(() => {
        if (!chartContainerRef.current) return;

        const chart = createChart(chartContainerRef.current, {
            width: chartContainerRef.current.clientWidth,
            height: chartContainerRef.current.clientHeight || 400,
            layout: {
                background: { color: '#0f172a' },
                textColor: '#94a3b8',
            },
            grid: {
                vertLines: { color: '#1e293b' },
                horzLines: { color: '#1e293b' },
            },
            timeScale: {
                borderColor: '#1e293b',
                timeVisible: true,
                tickMarkFormatter: (time: number | string, tickMarkType: TickMarkType, locale: string) => {
                    const date = typeof time === 'number'
                        ? new Date(time * 1000)
                        : new Date(time)

                    switch (tickMarkType) {
                        case TickMarkType.Year:
                            return date.toLocaleDateString(locale, {year: 'numeric'});
                        case TickMarkType.Month:
                            return date.toLocaleDateString(locale, {month: 'short'});
                        case TickMarkType.DayOfMonth:
                            return date.toLocaleDateString(locale, {day: 'numeric'});
                        case TickMarkType.Time:
                            return date.toLocaleTimeString(locale, {hour: '2-digit', minute: '2-digit'});
                        default:
                            return date.toLocaleDateString(locale)
                    }
                }
            },
            rightPriceScale: {
                borderColor: '#1e293b',
            },
        });

        const candleSeries = chart.addSeries(CandlestickSeries, {
            upColor: '#10b981',
            downColor: '#f43f5e',
            borderVisible: false,
            wickUpColor: '#10b981',
            wickDownColor: '#f43f5e',
        });

        chart.timeScale().subscribeVisibleLogicalRangeChange((range) => {
            if (range && range.from < 2) {
                onNeedMoreDataRef.current();
            }
        });

        chartRef.current = chart;
        candleSeriesRef.current = candleSeries;

        const resizeObserver = new ResizeObserver((entries) => {
            if (entries.length === 0 || !chartRef.current) return;
            const {width, height} = entries[0].contentRect;
            chartRef.current.resize(width, height);
        });

        resizeObserver.observe(chartContainerRef.current);

        return () => {
            resizeObserver.disconnect();
            chart.remove();
            chartRef.current = null;
            candleSeriesRef.current = null;
        };
    }, []);

    useEffect(() => {
        if (!candleSeriesRef.current || !chartRef.current || data.length === 0) return;
        const timeScale = chartRef.current.timeScale();
        const logicalRange = timeScale.getVisibleLogicalRange();
        candleSeriesRef.current.setData(data)
        if (data.length > prevDataLengthRef.current && data[0].time < prevFirstTimeRef.current) {
            if (logicalRange) {
                const addedBarsCount = data.length - prevDataLengthRef.current;
                timeScale.setVisibleLogicalRange({
                    from: logicalRange.from + addedBarsCount,
                    to: logicalRange.to + addedBarsCount
                });
            }
        } else {
            candleSeriesRef.current.priceScale().applyOptions({autoScale: true});
            timeScale.fitContent();
        }
        prevDataLengthRef.current = data.length;
        prevFirstTimeRef.current = data[0].time;
    }, [data]);

    return (
        <div
            className="w-full h-full"
            ref={chartContainerRef}
        />
    )
}

export default CandleStickChart
