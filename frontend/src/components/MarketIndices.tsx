import {useState, useEffect} from 'react'
import {MarketQuote} from "../types/market.ts";
import {fetchMarketQuote} from "../api/client.ts";

const INDICES = ['SPY', 'QQQ', 'DIA', 'IWM', 'ACWI', 'GLD', 'SLV', 'USO']

function MarketIndices() {
    const [quoteList, setQuoteList] = useState<MarketQuote[]>([]);
    const [loadingState, setLoadingState] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchIndices = async () => {
            try {
                setLoadingState(true);
                const results = await Promise.all(INDICES.map(symbol => fetchMarketQuote(symbol)));
                setQuoteList(results);
            } catch (err) {
                setError(err instanceof Error ? err.message : 'Unexpected error occurred');
            } finally {
                setLoadingState(false);
            }
        };
        fetchIndices();
    }, []);

    if (loadingState) {
        return (
            <div className="p-4 bg-slate-900 border border-slate-800 rounded-xl text-sm text-slate-400">
                Loading indices...
            </div>
        );
    }

    if (error) {
        return (
            <div className="p-4 bg-slate-900 border border-rose-900/50 rounded-xl text-sm text-rose-400">
                Error: {error}
            </div>
        );
    }

    return (
        <section className="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-8 gap-3">
            {quoteList.map((quote) => {
                const isPositive = quote.percentChange >= 0;
                const priceDiff = quote.currentPrice - quote.previousClose;
                const changeColor = isPositive ? 'text-emerald-400' : 'text-rose-400';

                return (
                    <div
                        key={quote.symbol}
                        className="flex flex-col justify-between p-3.5 bg-slate-900 border border-slate-800 hover:border-slate-700 rounded-xl transition-colors"
                    >
                        <div className="flex items-center justify-between gap-2">
                            <span className="text-xs font-semibold text-slate-400 tracking-wide">
                                {quote.symbol}
                            </span>
                            <span className={`text-xs font-medium ${changeColor}`}>
                                {quote.percentChange > 0 ? '+' : ''}
                                {quote.percentChange.toFixed(2)}%
                            </span>
                        </div>

                        <div className="mt-2">
                            <div className="text-base font-semibold text-slate-100">
                                {quote.currentPrice.toLocaleString(undefined, {
                                    minimumFractionDigits: 2,
                                    maximumFractionDigits: 2,
                                })}
                                <span className="text-xs text-slate-500 ml-1 font-normal">USD</span>
                            </div>
                            <div className={`text-xs ${changeColor}`}>
                                {priceDiff > 0 ? '+' : ''}
                                {priceDiff.toLocaleString(undefined, {
                                    minimumFractionDigits: 2,
                                    maximumFractionDigits: 2,
                                })}
                            </div>
                        </div>
                    </div>
                );
            })}
        </section>
    );
}

export default MarketIndices
