export type TimeRange = '1M' | '3M' | '6M' | '1Y';

export const filterDataByRange = (data: any[], range: TimeRange) => {
    const now = new Date();
    const months = range === '1M' ? 1 : range === '3M' ? 3 : range === '6M' ? 6 : 12;
    const cutoff = new Date(now.setMonth(now.getMonth()-months));
    return data.filter(d => new Date(d.date) >= cutoff);
    };

export const xAxisFormatter = (val: string) => {
    if (!val) return '';
    const d = new Date(val);
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
    xaxis: {
        type: 'datetime',
        tickAmount: 10,
        labels: {
            formatter: xAxisFormatter,
            style: {colors: '#64748b'}
            },
        axisTicks: {show: false},
            axisBorder: {show: false}
        },
    yaxis: {
        opposite: true,
        tooltip: {enabled: true}
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
    dataLabels: {enabled: false}
    });