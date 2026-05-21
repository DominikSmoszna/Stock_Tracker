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

const COMMON_AXIS_STYLES = {
    labels: {
        style: {colors: '#64748b', fontSize: '12px'},
        formatter: (val: string | number) => xAxisFormatter(val)
        },
        axisTicks: {show: false},
        axisBorder: {show: false}
    }

export const getBaseCandleOptions = (showVolume: boolean): any => ({
    chart: {
        id: 'main-chart',
        group: 'stock-sync',
        type: 'candlestick',
        toolbar: {show: false},
        animations: {enabled: true}
        },
    legend: {show: false},
    xaxis: {
        type: 'category',
        tickAmount: 10,
        ...COMMON_AXIS_STYLES
        },
    yaxis: {
        opposite: true,
        tooltip: {enabled: true},
        labels: {
            formatter: (val: number) => val.toFixed(2),
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
        show: true,
        borderColor: '#f1f5f9',
        padding: {left: 30, right: 30}
        },
    tooltip: {
        enabled: true,
        shared: true,
        intersect: false,
        fixed: {
            enabled: true,
            position: 'topLeft',
            offsetX: -10,
            offsetY: 0,
            },
        custom: function({series, seriesIndex, dataPointIndex, w}: any){

            const hasCandleData = w.globals.seriesCandleO && w.globals.seriesCandleO[0] && w.globals.seriesCandleO[0][dataPointIndex] !== undefined;
            const date = w.globals.categoryLabels[dataPointIndex];

            if (!hasCandleData){
                let val = series[seriesIndex][dataPointIndex];
                return `
                    <div class="p-2 shadow-lg">
                        <div class="text-xs font-bold text-gray-500">${date}</div>
                        <div class="text-sm font-bold">${val !== null ? val.toFixed(2) : 'N/A'}</div>
                    </div>
                `;
                }


            const o = w.globals.seriesCandleO[0][dataPointIndex];
            const h = w.globals.seriesCandleH[0][dataPointIndex];
            const l = w.globals.seriesCandleL[0][dataPointIndex];
            const c = w.globals.seriesCandleC[0][dataPointIndex];

            let html = `
                <div class="p-2 shadow-lg border-0" style="font-family: inherit;">
                <div class="text-xs font-bold text-gray-500 mb-1">${date}</div>
                <div class="grid grid-cols-2 gap-x-4 gap-y-1 mb-2">
                    <div><span class="text-gray-400 text-[10px] uppercase">O:</span> <span class="font-mono font-bold">${o?.toFixed(2)}</span></div>
                    <div><span class="text-gray-400 text-[10px] uppercase">H:</span> <span class="font-mono font-bold">${h?.toFixed(2)}</span></div>
                    <div><span class="text-gray-400 text-[10px] uppercase">L:</span> <span class="font-mono font-bold">${l?.toFixed(2)}</span></div>
                    <div><span class="text-gray-400 text-[10px] uppercase">C:</span> <span class="font-mono font-bold ${c >= o ? 'text-green-500' : 'text-red-500'}">${c?.toFixed(2)}</span></div>
                </div>`;

            w.config.series.forEach((s: any, i: number) => {
                if(s.type === 'line' && series[i][dataPointIndex] !== null) {
                        html += `<div class="text-[10px] text-gray-600">${s.name}: <b>${series[i][dataPointIndex]?.toFixed(2)}</b></div>`;
                    }
                })

            html += `</div>`
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
        padding: {left: 30, right: 30}
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

export const getRSIOptions = (): any => ({
    chart: {
        id: 'rsi-chart',
        group: 'stock-sync',
        type: 'line',
        height: 150,
        toolbar: {show: false},
        brush: {enabled: false},
        selection: {enabled: false},
        sparkline: {enabled: false},
        padding: {left: 30, right: 30}
        },
    colors: ['#8b5cf6'],
    stroke: {width: 2},
    annotations: {
        yaxis: [
            {y:30, borderColor: '#ef4444', strokeDashArray:4, label: {text: 'Oversold', position: 'right', offsetX: -10, style:{color: '#ef4444', background: 'transparent'}}},
            {y:70, borderColor: '#ef4444', strokeDashArray:4, label: {text: 'Overbought', position: 'right', offsetX: -10, style:{color: '#ef4444', background: 'transparent'}}}
            ]
        },
    yaxis: {
        min: 0,
        max: 100,
        tickAmount: 4,
        opposite: true,
        forceNiceScale: false,
        labels: {
            show: true,
            formatter: (val: number) => val.toFixed(0),
            style: {colors: '#64748b'}
            }
        },
    xaxis: {
        type: 'category',
        tickAmount: 8,
        ...COMMON_AXIS_STYLES
        },
    grid: {
        show: true,
        borderColor: '#f1f5f9'
        }
    });