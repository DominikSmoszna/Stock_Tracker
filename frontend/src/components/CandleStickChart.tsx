import {useEffect, useRef} from "react";
import {CandlestickSeries, LineSeries, createChart, TickMarkType} from "lightweight-charts";
import {CandlestickChartProps} from "../types/market.ts"

function CandleStickChart({data, indicators, onNeedMoreData, onCrosshairMove}: CandlestickChartProps) {

    const chartContainerRef = useRef<HTMLDivElement>(null)
    const chartRef = useRef<any>(null)
    const candleSeriesRef = useRef<any>(null)
    const maSeriesMapRef = useRef<Map<string, any>>(new Map())
    const bbUpperSeriesRef = useRef<any>(null)
    const bbMiddleSeriesRef = useRef<any>(null)
    const bbLowerSeriesRef = useRef<any>(null)
    const rsiSeriesRef = useRef<any>(null)
    const onNeedMoreDataRef = useRef(onNeedMoreData)
    const onCrosshairMoveRef = useRef(onCrosshairMove)
    const prevDataLengthRef = useRef<number>(0)
    const prevFirstTimeRef = useRef<number | string>(0)

    useEffect(() => {
        onNeedMoreDataRef.current = onNeedMoreData
    }, [onNeedMoreData]);

    useEffect(() => {
        onCrosshairMoveRef.current = onCrosshairMove
    }, [onCrosshairMove]);

    useEffect(() => {
        if (!chartContainerRef.current) return;

        const chart = createChart(chartContainerRef.current, {
            width: chartContainerRef.current.clientWidth,
            height: chartContainerRef.current.clientHeight || 400,
            layout: {
                background: { color: '#0f172a' },
                textColor: '#94a3b8',
                panes: {
                    separatorColor: '#1e293b',
                    separatorHoverColor: '#334155',
                    enableResize: true,
                },
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

        const bbUpperSeries = chart.addSeries(LineSeries, {
            color: '#a855f7',
            lineWidth: 1,
            priceLineVisible: false,
            lastValueVisible: false,
        });

        const bbMiddleSeries = chart.addSeries(LineSeries, {
            color: '#c084fc',
            lineWidth: 1,
            priceLineVisible: false,
            lastValueVisible: false,
        });

        const bbLowerSeries = chart.addSeries(LineSeries, {
            color: '#a855f7',
            lineWidth: 1,
            priceLineVisible: false,
            lastValueVisible: false,
        });

        chart.timeScale().subscribeVisibleLogicalRangeChange((range) => {
            if (range && range.from < 2) {
                onNeedMoreDataRef.current();
            }
        });

        chart.subscribeCrosshairMove((param) => {
            if (!param.time) {
                onCrosshairMoveRef.current?.(null);
                return;
            }
            const bar = param.seriesData.get(candleSeries) as
                | { open?: number; high?: number; low?: number; close?: number }
                | undefined;
            const rsiPoint = rsiSeriesRef.current
                ? (param.seriesData.get(rsiSeriesRef.current) as { value?: number } | undefined)
                : undefined;

            if (
                bar &&
                bar.open !== undefined &&
                bar.high !== undefined &&
                bar.low !== undefined &&
                bar.close !== undefined
            ) {
                onCrosshairMoveRef.current?.({
                    open: bar.open,
                    high: bar.high,
                    low: bar.low,
                    close: bar.close,
                    rsi: rsiPoint?.value,
                });
            } else {
                onCrosshairMoveRef.current?.(null);
            }
        });

        chartRef.current = chart;
        candleSeriesRef.current = candleSeries;
        bbUpperSeriesRef.current = bbUpperSeries;
        bbMiddleSeriesRef.current = bbMiddleSeries;
        bbLowerSeriesRef.current = bbLowerSeries;

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
            maSeriesMapRef.current.clear();
            bbUpperSeriesRef.current = null;
            bbMiddleSeriesRef.current = null;
            bbLowerSeriesRef.current = null;
            rsiSeriesRef.current = null;
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

    useEffect(() => {
        if (!chartRef.current) return;

        const activeMAs = indicators?.movingAverages ?? [];
        const activeKeys = new Set(activeMAs.map((m) => m.key));

        for (const [key, series] of maSeriesMapRef.current.entries()) {
            if (!activeKeys.has(key)) {
                chartRef.current.removeSeries(series);
                maSeriesMapRef.current.delete(key);
            }
        }

        for (const ma of activeMAs) {
            let series = maSeriesMapRef.current.get(ma.key);
            if (!series) {
                series = chartRef.current.addSeries(LineSeries, {
                    color: ma.color,
                    lineWidth: 2,
                    priceLineVisible: false,
                    lastValueVisible: false,
                });
                maSeriesMapRef.current.set(ma.key, series);
            } else {
                series.applyOptions({ color: ma.color });
            }
            series.setData(ma.data ?? []);
        }

        const bbColor = indicators?.bbColor ?? '#a855f7';
        bbUpperSeriesRef.current?.applyOptions({ color: bbColor });
        bbMiddleSeriesRef.current?.applyOptions({ color: bbColor });
        bbLowerSeriesRef.current?.applyOptions({ color: bbColor });

        if (indicators?.bb && indicators.bb.length > 0) {
            bbUpperSeriesRef.current?.setData(
                indicators.bb.map((p) => ({ time: p.time, value: p.upper }))
            );
            bbMiddleSeriesRef.current?.setData(
                indicators.bb.map((p) => ({ time: p.time, value: p.middle }))
            );
            bbLowerSeriesRef.current?.setData(
                indicators.bb.map((p) => ({ time: p.time, value: p.lower }))
            );
        } else {
            bbUpperSeriesRef.current?.setData([]);
            bbMiddleSeriesRef.current?.setData([]);
            bbLowerSeriesRef.current?.setData([]);
        }

        const hasRsi = Boolean(indicators?.rsi && indicators.rsi.length > 0);
        const rsiColor = indicators?.rsiColor ?? '#ec4899';

        if (hasRsi && indicators?.rsi) {
            if (!rsiSeriesRef.current) {
                rsiSeriesRef.current = chartRef.current.addSeries(
                    LineSeries,
                    {
                        color: rsiColor,
                        lineWidth: 2,
                        priceLineVisible: true,
                        lastValueVisible: true,
                        autoscaleInfoProvider: () => ({
                            priceRange: {
                                minValue: 0,
                                maxValue: 100,
                            },
                        }),
                    },
                    1
                );
                chartRef.current.panes()[1]?.setHeight(130);
            } else {
                rsiSeriesRef.current.applyOptions({ color: rsiColor });
            }
            rsiSeriesRef.current.setData(indicators.rsi);
        } else if (rsiSeriesRef.current) {
            chartRef.current.removeSeries(rsiSeriesRef.current);
            rsiSeriesRef.current = null;
        }
    }, [indicators]);

    return (
        <div
            className="w-full h-full"
            ref={chartContainerRef}
        />
    )
}

export default CandleStickChart
