import {useEffect, useRef} from "react";
import {CandlestickSeries, createChart} from "lightweight-charts";
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

        chart.timeScale().subscribeVisibleLogicalRangeChange((range) => {
            if (range && range.from < 2) {
                onNeedMoreDataRef.current();
            }
        });

        chartRef.current = chart;
        candleSeriesRef.current = candleSeries;

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

    useEffect(() => {
        if (!candleSeriesRef.current || !chartRef.current || data.length === 0) return;
        if (!candleSeriesRef.current || !chartRef.current) return;
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
        }else {
            timeScale.fitContent();
        }
        prevDataLengthRef.current = data.length;
        prevFirstTimeRef.current = data[0].time;
    }, [data]);

    return (<div
        ref={chartContainerRef}
        className="relative border border-gray-200 rounded-xl mt-4 bg-white p-2 shadow-md min-h-[400px]"
    />)
}

export default CandleStickChart