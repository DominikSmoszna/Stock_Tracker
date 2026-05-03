import {useState, useEffect} from 'react'
import { motion } from 'framer-motion';
import Chart from 'react-apexcharts';

function ChartsPage() {

  const [symbol, setSymbol] = useState<string>('');
  const [inputValue, setInputValue] = useState<string>('')
  const [data, setData] = useState<HistoricalPrice[]>([]);
  const [loadingState, setLoadingState] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [timeRange, setTimeRange] = useState<'1M' | '3M' | '6M' | '1Y'>('1M');
  const [showVolume, setShowVolume] = useState<boolean>(false);

  useEffect(() => {
        const fetchHistoryPrices = async (symbol: string) => {
            try{
                setLoadingState(true);
                const response = await fetch(`http://localhost:8080/api/market/history/${symbol}`);
                if(!response.ok){throw new Error(`Error occurred during fetching symbol: ${symbol}`);}
                const result = await response.json();
                setData(result.sort((a: any, b:any)=> new Date(a.data).getTime() - new Date(b.data).getTime()));
            }catch (err) {setError(err instanceof Error ? err.message : 'Unexpected error occurred');} finally{setLoadingState(false)}
        };
        if(symbol){fetchHistoryPrices(symbol);}
  },[symbol]);

const filterData = (data: HistoricalPrice[], range: '1M' | '3M' | '6M' | '1Y') => {
      const now = new Date();
      const months = range === '1M' ? 1 : range === '3M' ? 3 : range === '6M' ? 6 : 12;
      const cutoff = new Date(now.setMonth(now.getMonth()-months));
      return data.filter(d => new Date(d.date) >= cutoff);
  }

  const filteredData = filterData(data, timeRange);

  const candleSeries = [{
      data: [...filteredData].reverse().map(d => ({
          x: d.date,
          y: [d.open, d.high, d.low, d.close]
          }))
      }];

  const volumeSeries = [{
      name: 'Volume',
      data: [...filteredData].reverse().map(d => ({
          x: d.date,
          y: d.volume
          }))
      }];

  const chartOptions: any = {
        chart: {id: 'basic-bar', type: 'candlestick', toolbar: {show: false}, animations: {enabled: false}  },
        xaxis: {type: 'category', tickAmount: 10,
            labels: {
                formatter: function(val:string) {
                if(!val) return '';
                const d = new Date(val);
                return d.toLocaleDateString('pl-PL', {day: '2-digit', month: 'short'});
                }
            }
        },
        yaxis: {tooltip: {enabled: true} },
        plotOptions: {
            candlestick: {
                colors: {upward: '#22c55e', downward: '#ef4444'}
                }
        },
        axisTicks: {show: false}
      };

  const volumeOptions: any = {
        chart: {type: 'bar', toolbar: {show: false}, sparkline: {enabled: true} },
        plotOptions: {bar: {columnWidth: '80%', colors: {ranges: [{from:0, to: 999999999, color: '94a3b8'}] }}},
        xaxis: {type: 'category', tickAmount: 10,
            labels: {
                formatter: function(val:string) {
                    if(!val) return '';
                    const d = new Date(val);
                    return d.toLocaleDateString('pl-PL', {day: '2-digit', month: 'short'});
            }}
      }
  }


  return (
    <div className="max-w-4xl mx-auto p-6 rounded-xl shadow-lg border border-slate-100">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
            <h2 className="text-xl font-bold text-slate-800">Analiza Instrumentu: <span className="text-blue-600">{symbol}</span></h2>
            <div className="flex gap-2">
                <input type="text" value={inputValue}
                onChange={(e)=> setInputValue(e.target.value.toUpperCase())}
                placeholder="Symbol..."
                className="px-4 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all w-32"/>
                <button onClick ={()=>setSymbol(inputValue)} className="bg-blue-800 hover:bg-blue-900 text-white px-5 py-2 rounded-lg font-medium transition-colors shadow-sm">Search</button>
            </div>
        </div>
        <div className="flex flex-col sm:flex-row justify-between gap-4 mb-8">
        <div className="flex items-center gap-1 mb-6 bg-slate-100 p-1 rounded-xl w-fit">
            {(['1M','3M','6M','1Y'] as const).map(range => (
                <button key={range} onClick={()=>setTimeRange(range)}
                    className={`relative px-4 py-1.5 rounded-lg text-sm font-semibold transition-all
                        ${timeRange === range ? ' text-blue-800 hover:text-blue-900 shadow-sm' : 'text-slate-500 hover:text-slate-700'}`}
                >
                <span className="relative z-20">{range}</span>
                {timeRange === range && (
                    <motion.div
                        layoutId="activeTab"
                        className="absolute inset-0 bg-white rounded-lg shadow-sm z-10"
                        transition={{type: "spring", bounce: 0.2, duration: 0.6}}
                    />
                    )}
                </button>
            ))}
        </div>
        <div className="flex items-center gap-1 mb-6">
            <span className="ml-3 text-sm font-medium text-gray-900">Show Volume </span>
            <label className="relative inline-flex flex items-center gap-3 cursor-pointer">
                <input type="checkbox" className="sr-only peer" checked={showVolume} onChange={()=> setShowVolume(!showVolume)}/>
                <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                <span className="ml-3 text-sm font-medium text-gray-900 mic-w-[30px]">{showVolume ? 'ON' : 'OFF'}</span>
            </label>
        </div>
        </div>
        <div className="w-full p-4 bg-gray-50 rounded-xl border border-gray-200">
            <Chart options={chartOptions} series={candleSeries} type="candlestick" height={300} />
        </div>
        { showVolume &&
        <div className="w-full p-4 bg-gray-50 rounded-b-xl border border-gray-200 -mt-4 relative z-0">
            <Chart options={volumeOptions} series={volumeSeries} type="bar" height={80}/>
        </div>
        }
    </div>
  )
}

export default ChartsPage