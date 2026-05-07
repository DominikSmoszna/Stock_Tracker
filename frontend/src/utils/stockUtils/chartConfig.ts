export type TimeRange = '1M' | '3M' | '6M' | '1Y';

export const filterDataByRange = (data: any[], range: TimeRange) => {
    const now = new Date();
    const months = range === '1M' ? 1 : range === '3M' ? 3 : range === '6M' ? 6 : 12;
    const cutoff = new Date(now.setMonth(now.getMonth()-months));
    return data.filter(d => new Date(d.date) >= cutoff);
    };

export const xAxisFormatter = (val: number | string) => {
    if (!val) return '';
    const d = new Date(val);
    if (isNaN(d.getTime())) return '';
    return d.toLocaleDateString('pl-PL', {day: '2-digit', month: 'short'});
    };

export const getBaseCandleOptions = (showVolume: boolean): any => ({
    chart: {
        id: 'main-chart',
        group: 'stock-sync',
        type: 'candlestick',
        toolbar: {show: false},
        animations: {enabled: false}
        },
    legend: {show: false},
    xaxis: {
        type: 'category',
        tickAmount: 10,
        labels: {
            formatter: (val: string) => {
                if(!val) return '';
                const d = new Date(val);
                return d.toLocaleDateString('pl-PL', { day: '2-digit', month: 'short' });
                },
            style: {colors: '#64748b'}
            },
        axisTicks: {show: false},
            axisBorder: {show: false}
        },
    yaxis: {
        opposite: true,
        tooltip: {enabled: true},
        labels: {
            formatter: (val: number) => {
                return val.toFixed(2);
                },
            style: {colors: '#64748b'}
            }
        },
    plotOptions: {
        candlestick: {
            colors: {upward: '#22c55e', downward: '#ef4444'},
            wick: {useFillColor: true}
            }
        },
    grid: {
        borderColor: '#f1f5f9',
        padding: {bottom: showVolume ? -20 : 0}
        },
    tooltip: {
        enabled: true,
        shared: true,
        intersect: false,
        followCursor: true,
        custom: function({series, seriesIndex, dataPointIndex, w}: any){
            const o = w.globals.seriesCandleO[0][dataPointIndex];
            const h = w.globals.seriesCandleH[0][dataPointIndex];
            const l = w.globals.seriesCandleL[0][dataPointIndex];
            const c = w.globals.seriesCandleC[0][dataPointIndex];
            const date = w.globals.categoryLabels[dataPointIndex];

            let html = `
                <div class="p-2 shadow-lg border-0" style="font-family: inherit;">
                <div class="text-xs font-bold text-gray-500 mb-1">${date}</div>
                <div class="grid grid-cols-2 gap-x-4 gap-y-1 mb-2">
                    <div><span class="text-gray-400 text-[10px] uppercase">O:</span> <span class="font-mono font-bold">${o?.toFixed(2)}</span></div>
                    <div><span class="text-gray-400 text-[10px] uppercase">H:</span> <span class="font-mono font-bold">${h?.toFixed(2)}</span></div>
                    <div><span class="text-gray-400 text-[10px] uppercase">L:</span> <span class="font-mono font-bold">${l?.toFixed(2)}</span></div>
                    <div><span class="text-gray-400 text-[10px] uppercase">C:</span> <span class="font-mono font-bold ${c >= o ? 'text-green-500' : 'text-red-500'}">${c?.toFixed(2)}</span></div>
                </div></div>`;
            return html;
            },
        marker: {show: true},
        style: {fontSize: '12px'},
        theme: 'light',
        y: {
            formatter: (val: number) => val?.toFixed(2)
            }
        },
    colors: ['#22c55e', '#3b82f6', '#f59e0b', '#ef4444', '#8b5cf6'],
    stroke: {
        width: [1, 2, 2, 2, 2],
        curve: 'smooth'
        }
    });

export const getVolumeOptions = (): any => ({
    chart: {
        id: 'volume-chart',
        group: 'stock-sync',
        type: 'bar',
        toolbar: {show: false},
        sparkline: {enabled: false}
        },
    plotOptions: {
        bar: {
            columnWidth: '80%',
            colors: {
                ranges: [{from: 0, to: 999999999, color: '#94a3b8'}]
                }
            }
        },
    xaxis: {
        type: 'category',
        labels: {show: false},
        axisTicks: {show: false},
        axisBorder: {show: false},
        tooltip: {enabled: false}
        },
    yaxis: {
        labels: {show: false},
        axisTicks: {show: false},
        axisBorder: {show: false}
        },
    grid: {
        show: false,
        padding: {top: -20}
        },
    dataLabels: {enabled: false},
    tooltip: {
        y: {
            formatter: (val: number) => {
                if (val >= 1000000) return (val / 1000000).toFixed(1) + 'M';
                if (val >= 1000) return (val / 1000).toFixed(1) + 'K';
                return val.toString();
                }
            }
        }
    });