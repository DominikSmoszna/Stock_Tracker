import {HistoricalPrice} from '../types/market';

export const calculateSMA = (data: HistoricalPrice[], period: number) => {
    let sum =0;
      return data.map((item, idx) => {
          sum += item.close;
          if (idx < period-1) return {x: data[idx].date, y: null};
          if (idx >= period) {
              sum -= data[idx-period].close;
              }
          return {x: data[idx].date, y: Number((sum/period).toFixed(2))};
      });
    };

export const calculateEMA = (data: HistoricalPrice[], period: number) => {
    const multiplier = 2/(period+1);
    let previousEMA: number | null = null;
    return data.map((item, idx) => {
            if (idx < period-1) return {x: data[idx].date, y: null};
            if (idx === period -1) {
            const slice = data.slice(idx - period + 1, idx+1);
            const SMA = slice.reduce((acc, curr) => acc + curr.close, 0)/period;
            previousEMA = SMA;
            return {x: item.date, y: Number(SMA.toFixed(2))}
            }
            const currentEMA = (item.close - (previousEMA as number)) * multiplier + (previousEMA as number);
            previousEMA = currentEMA;
            return {x: item.date, y: Number(currentEMA.toFixed(2))};
        });
    };

export const calcStandardDeviation = (values: number[], mean: number) => {
    const squareDiffs = values.map(v => Math.pow(v-mean, 2));
    const avgSquareDiss = squareDiffs.reduce((a, b) => a + b, 0) / values.length;
    return Math.sqrt(avgSquareDiss);
    };

export const calcBollingerBands = (data: HistoricalPrice[], period: number = 20) => {
    return data.map((_, idx, arr) => {
        if (idx < period - 1) return {middle: null, upper: null, lower: null};
        const slice = arr.slice(idx - period + 1, idx + 1);
        const sum = slice.reduce((a,b) => a +b.close, 0);
        const middle = sum / period;

        const variance = slice.reduce((acc, curr) => {
            return acc + Math.pow(curr.close - middle, 2);
            }, 0) / period;

        const stdDev = Math.sqrt(variance);

        return {
            middle: Number(middle.toFixed(2)),
            upper: Number((middle+stdDev*2).toFixed(2)),
            lower: Number((middle-stdDev*2).toFixed(2))
            };
        })
    };

export const calculateRSI = (data: HistoricalPrice[], period: number = 14) =>{
    let avgGain =0;
    let avgLoss =0;

    return data.map((item, idx) => {
        if(idx === 0) return {x: item.date, y: null};

        const change = item.close - data[idx-1].close;
        const gain = change > 0 ? change : 0;
        const loss = change < 0 ? -change : 0;

        if (idx < period) {
            avgGain += gain;
            avgLoss += loss;
            return {x: item.date, y: null};
            }

        if (idx === period) {
            avgGain /= period;
            avgLoss /= period;
            }else{
                avgGain = (avgGain * (period -1) + gain) / period;
                avgLoss = (avgLoss * (period -1) + loss) / period;
                }

        const rs = avgLoss === 0 ? 100 : avgGain / avgLoss;
        const rsi = 100 - 100 / (1+rs);
        return {x: item.date, y: Number(rsi.toFixed(2))};
        });
    };