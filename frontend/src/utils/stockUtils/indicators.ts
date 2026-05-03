export const calculateSMA = (data: any[], period: number) => {
      return data.map((_, index) => {
          if (index < period-1) return {x: data[index].date, y: null};
          const slice = data.slice(index-period+1, index+1);
          const sum = slice.reduce((acc, curr)=> acc + curr.close, 0);
          return {x: data[index].date, y: Number((sum/period).toFixed(2))};
      });
  };