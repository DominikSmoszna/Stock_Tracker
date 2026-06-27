import {useState, useEffect} from 'react'
import {MarketQuote} from "../types/market.ts";

const INDICES = ['SPY', 'QQQ', 'DIA', 'IWM','ACWI', 'GLD', 'SLV', 'USO']

function MarketIndices() {

    const [quoteList, setQuoteList] = useState<MarketQuote[]>([]);
    const [loadingState, setLoadingState] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(()=> {
        const fetchIndices = async () => {
            try{
                setLoadingState(true);

                const request = INDICES.map(symbol=>
                    fetch(`http://localhost:8080/api/market/quote/${symbol}`).then(res =>{
                        if(!res.ok) throw new Error(`Error occurred during fetching symbol: ${symbol}`);
                        return res.json();
                    })
                );

                const results = await Promise.all(request);
                setQuoteList(results);
            }catch (err) {
                setError(err instanceof Error ? err.message : 'Unexpected error occurred');
            } finally{
                setLoadingState(false);
            }
        };
        fetchIndices();
    },[]);

    if (loadingState) {
        return <div className="p-4 text-gray-500">Loading indices...</div>;
        }
    if (error){
        return <div className="p-4 text-red-500 font-semibold">Error: {error}</div>;
        }

    return(
        <div className="flex flex-wrap gap-4 p-4 bg-gray-50 rounded-lg shadow-sm w-[90%] mx-auto justify-between">
            {quoteList.map((quote)=>(
                <div key={quote.symbol} className=" flex flex-col p-3 bg-slate-300 border border-gray-200 rounded-md min-w-[160px] transition-transform duration-300 ease-in-out hover:scale-[1.1] cursor-pointer">
                    <span className="text-sm font-bold text-gray-700">{quote.symbol}</span>
                    <div className="gap-2">
                    <span className="text-l font-semibold text-gray-900">
                    {quote.currentPrice.toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}USD
                    </span>
                    <span className={`text-sm font-medium ${quote.percentChange >=0 ? 'text-green-500' : 'text-red-500'}`}>
                    {quote.percentChange > 0 ?  ' +' : ' '}{(quote.currentPrice-quote.previousClose).toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}
                    </span>
                    </div>
                    <span className={`text-sm font-medium ${quote.percentChange >=0 ? 'text-green-500' : 'text-red-500'}`}>
                    {quote.percentChange > 0 ? '+' : ''}{quote.percentChange.toFixed(2)}%
                    </span>
                </div>
                ))}
            </div>
        );
}

export default MarketIndices