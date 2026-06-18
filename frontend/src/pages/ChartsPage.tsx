import {useState, useEffect, useMemo} from 'react'
import { motion } from 'framer-motion';
import Chart from 'react-apexcharts';
import {getBaseCandleOptions, getVolumeOptions, getRSIOptions, xAxisFormatter} from '../utils/stockUtils/chartConfig';
import {calculateSMA, calcBollingerBands, calculateEMA, calculateRSI} from '../utils/stockUtils/indicators';

function ChartsPage() {
  const [symbol, setSymbol] = useState<string>('');
  const [timeInterval, setTimeInterval] = useState<'1m'|'2m'|'3m'|'5m'|'15m'|'30m'|'1h'|'4h'|'1d'|'1wk'|'1mo'|'3mo'>('1d');
  const [inputValue, setInputValue] = useState<string>('')
  const [data, setData] = useState<HistoricalPrice[]>([]);
  const [loadingState, setLoadingState] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [timeRange, setTimeRange] = useState<'1d'|'5d'|'1mo' | '3mo' | '6mo' | '1y' | '2y' | '5y' | '10y' | 'ytd' | 'max'>('1mo');
  const [showVolume, setShowVolume] = useState<boolean>(false);
  const [isMenuOpen, setIsMenuOpen] = useState<boolean>(false);
  const [showSMA, setShowSMA] = useState<boolean>(false);
  const [showEMA, setShowEMA] = useState<boolean>(false);
  const [showBollingerBands, setShowBollingerBands] = useState<boolean>(false);
  const [showRSI, setShowRSI] = useState<boolean>(false);
  const [showMACD, setShowMACD] = useState<boolean>(false);

  useEffect(() => {
        const fetchHistoryPrices = async (symbol: string) => {
            try{
                setLoadingState(true);
                const response = await fetch(`http://localhost:8080/api/market/history/${symbol}?interval=${timeInterval}&range=${timeRange}`);
                if(!response.ok){throw new Error(`Error occurred during fetching symbol: ${symbol}`);}
                const result = await response.json();
                setData(result.sort((a: any, b:any)=> new Date(a.date).getTime() - new Date(b.date).getTime()));
            }catch (err) {setError(err instanceof Error ? err.message : 'Unexpected error occurred');} finally{setLoadingState(false)}
        };
        if(symbol){fetchHistoryPrices(symbol);}
  },[symbol, timeInterval, timeRange]);

  const volumeSeries = useMemo(() => [{
      name: 'Volume',
      type: 'bar',
      data: data.map(d => ({
          x: xAxisFormatter(d.date),
          y: d.volume
          }))
      }], [data]);

  const bbData = useMemo(() => {
      if (!showBollingerBands || data.length === 0) return [];
      return calcBollingerBands(data, 20);
      }, [data, showBollingerBands]);

  const smaData = useMemo(() => {
      if (!showSMA || data.length === 0) return [];
      return calculateSMA(data, 200);
      }, [data, showSMA]);

  const emaData = useMemo(() => {
      if (!showEMA || data.length === 0) return [];
      return calculateEMA(data, 20);
      }, [data, showEMA]);

  const rsiData = useMemo(() => {
      if (!showRSI || data.length === 0) return [];
      return calculateRSI(data, 14);
      }, [data, showRSI]);

  const rsiSeries = useMemo(() => [{
      name: 'RSI',
      type: 'line',
      data: data.map((d, idx)=>({
        x: xAxisFormatter(d.date),
        y: rsiData[idx] ?? null
      }))
      }],[data, rsiData]);

  const mainChartSeries = useMemo(() => {
      const series: any[] = [{
          name: 'Cena',
          type: 'candlestick',
          data: data.map(d => ({
              x: xAxisFormatter(d.date),
              y: [d.open, d.high, d.low, d.close]
              }))
          }];
          if (showBollingerBands && bbData.length > 0){
              ['upper', 'middle', 'lower'].forEach((key) => {
                  series.push({
                      name: `BB ${key.toUpperCase()}`,
                      type: 'line',
                      data: data.map((d, idx) => ({
                          x: xAxisFormatter(d.date),
                          y: bbData[idx]?.[key as keyof typeof bbData[0]] ?? null
                          }))
                      });
                  });
              }
          if(showSMA && smaData.length > 0){
                  series.push({
                      name: 'SMA 200',
                      type: 'line',
                      data: data.map((d, idx) => ({
                          x: xAxisFormatter(d.date),
                          y: smaData[idx] ?? null
                          }))
                      });
              }
          if(showEMA && emaData.length > 0){
                  series.push({
                      name: 'EMA 20',
                      type: 'line',
                      data: data.map((d,idx) => ({
                          x: xAxisFormatter(d.date),
                          y: emaData[idx] ?? null
                          }))
                      })
              }
          return series;
      }, [data, bbData, showBollingerBands, smaData, showSMA, emaData, showEMA]);

  const chartOptions = useMemo(() => {
      const base = getBaseCandleOptions();
      return {
        ...base,
        chart: {
            ...base.chart,
            id: `main-chart-${symbol}-${timeInterval}-${timeRange}-${data.length}`
        }
      };
      }, [symbol, timeInterval, timeRange, data.length]);
  const volumeOptions = useMemo(() => {
      const base = getVolumeOptions();
      return {
        ...base,
        chart: {
            ...base.chart,
            id: `volume-chart-${symbol}-${timeInterval}-${timeRange}-${data.length}`
        }
      };
      }, [symbol, timeInterval, timeRange, data.length]);
  const rsiOptions = useMemo(() => {
      const base = getRSIOptions();
      return {
        ...base,
        chart: {
            ...base.chart,
            id: `rsi-chart-${symbol}-${timeInterval}-${timeRange}-${data.length}`
        }
      };
      }, [symbol, timeInterval, timeRange, data.length]);

  const indicatorConfigs = [
      {label: 'Show Volume', state: showVolume, setter: setShowVolume},
      {label: 'Show BB', state: showBollingerBands, setter: setShowBollingerBands},
      {label: 'Show SMA', state: showSMA, setter: setShowSMA},
      {label: 'Show EMA', state: showEMA, setter: setShowEMA},
      {label: 'Show RSI', state: showRSI, setter: setShowRSI},
      ];
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
            <div className="flex flex-col justify-between gap-4">
                <div className="flex items-center gap-1 mb-6 bg-slate-100 p-1 rounded-xl w-fit">
                    {(['1d','5d','1mo','3mo','6mo','1y', '2y', '5y', '10y', 'ytd', 'max'] as const).map(range => (
                        <button key={range} onClick={()=>setTimeRange(range)}
                            className={`relative px-4 py-1.5 rounded-lg text-sm font-semibold transition-all
                            ${timeRange === range ? ' text-blue-800 hover:text-blue-900 shadow-sm' : 'text-slate-500 hover:text-slate-700'}`}>
                        <span className="relative z-20">{range}</span>
                        {timeRange === range && (
                            <motion.div
                                layoutId="rangeTab"
                                className="absolute inset-0 bg-white rounded-lg shadow-sm z-10"
                                transition={{type: "spring", bounce: 0.2, duration: 0.6}}
                            />
                            )}
                        </button>
                    ))}
                </div>
                <div className="flex items-center gap-1 mb-6 bg-slate-100 p-1 rounded-xl w-fit">
                    {(['1m','2m','3m','5m','15m','30m','1h','4h','1d','1wk','1mo','3mo'] as const).map(interval => (
                        <button key={interval} onClick={()=>setTimeInterval(interval)}
                            className={`relative px-4 py-1.5 rounded-lg text-sm font-semibold transition-all
                            ${timeInterval === interval ? ' text-blue-800 hover:text-blue-900 shadow-sm' : 'text-slate-500 hover:text-slate-700'}`}>
                        <span className="relative z-20">{interval}</span>
                        {timeInterval === interval && (
                            <motion.div
                                layoutId="intervalTab"
                                className="absolute inset-0 bg-white rounded-lg shadow-sm z-10"
                                transition={{type: "spring", bounce: 0.2, duration: 0.6}}
                            />
                        )}
                        </button>
                    ))}
                </div>
            </div>
            <div className="relative">
                <button onClick={()=> setIsMenuOpen(!isMenuOpen)} className="flex items-center gap-2 px-4 py-1.5 bg-white border border-slate rounded-lg text-sm font-semibold text-slate-700 hover:bg-slate-50 transition-all shadow-sm">
                    <span>Indicators</span>
                    <svg className={`w-4 h-4 transition-transform ${isMenuOpen ? 'rotate-180' : ''}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7"/>
                    </svg>
                </button>
                {isMenuOpen && (<>
                    <div className="fixed inset-0 z-30" onClick={()=> setIsMenuOpen(false)}/>
                    <div className="absolute right-0 mt-2 w-48 bg-white border border-slate rounded-xl shadow-xl z-40 p-2">
                        {indicatorConfigs.map((config) => (
                            <label key={config.label} className="flex justify-between px-3 py-2 hover:bg-slate-50 rounded-lg cursor-pointer transition-colors">
                                <span className="ml-3 text-sm font-medium text-gray-900">{config.label}</span>
                                <div className="relative inline-flex items-center cursor-pointer">
                                    <input
                                        type="checkbox"
                                        className="sr-only peer"
                                        checked={config.state}
                                        onChange={() => config.setter(!config.state)}
                                    />
                                    <div className="w-11 h-6 bg-gray-200 peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                                </div>
                                </label>
                            ))}
                        </div>
                </>)}
            </div>
        </div>
            <div>{data.length} punktów danych</div>
            <div className="w-full p-4 bg-gray-50 rounded-t-xl border border-gray-200">
                <Chart key={`${symbol}-${timeInterval}-${timeRange}-${data.length}`}  options={chartOptions} series={mainChartSeries} type="candlestick" height={350} />
            </div>
            <div className="w-full flex flex-col bg-gray-50 rounded-b-xl border-x border-b border-gray-200 p-4 pt-0 gap-2">
                {showVolume && (
                    <div className="w-full relative z-0 border-b border-gray-100 pb-2">
                        <Chart key={`vol-${symbol}-${timeInterval}`} options={volumeOptions} series={volumeSeries} type="bar" height={80}/>
                    </div>
                )}

                {showRSI && (
                    <div className="w-full relative z-0">
                        <Chart key={`rsi-${symbol}-${timeInterval}`} options={rsiOptions} series={rsiSeries} type="line" height={150}/>
                    </div>
                )}
            </div>
    </div>
  )
}

export default ChartsPage