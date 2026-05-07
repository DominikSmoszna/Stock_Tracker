import {HistoricalPrice} from '../types/market';

export const calculateSMA = (data: HistoricalPrice[], period: number) => {
      return data.map((_, idx) => {
          if (idx < period-1) return {x: data[idx].date, y: null};
          const slice = data.slice(idx - period + 1, idx+1);
          const sum = slice.reduce((acc, curr)=> acc + curr.close, 0);
          return {x: data[idx].date, y: Number((sum/period).toFixed(2))};
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
        const prices = slice.map(d => d.close);
        const middle = prices.reduce((a, b) => a + b, 0) / period;
        const stdDev = calcStandardDeviation(prices, middle);
        return {
            middle: middle,
            upper: middle + (stdDev * 2),
            lower: middle - (stdDev * 2)
            };
        })
    };