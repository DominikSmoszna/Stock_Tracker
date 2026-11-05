import {useState, useEffect} from 'react'
import {Client} from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import MarketHours from '../components/MarketHours'
import MarketIndices from '../components/MarketIndices'
import {StockPrice} from "../types/market.ts";
import {WS_URL} from "../api/client.ts";

function HomePage() {
    const [prices, setPrices] = useState<Record<string, StockPrice>>({})

    useEffect(() => {
        const client = new Client({
            webSocketFactory: () => new SockJS(WS_URL),
            onConnect: () => {
                client.subscribe('/topic/prices', (message) => {
                    const price = JSON.parse(message.body)
                    setPrices(prev => ({
                        ...prev,
                        [price.ticker]: price
                    }))
                })
            }
        })
        client.activate()
        return () => { client.deactivate() }
    }, [])

    const livePrices = Object.values(prices)

    return (
        <div className="max-w-7xl mx-auto p-4 sm:p-6 flex flex-col gap-6">
            <MarketHours/>
            <MarketIndices/>
            {livePrices.length > 0 && (
                <section className="bg-slate-900 border border-slate-800 rounded-xl p-4 sm:p-5">
                    <h3 className="text-slate-400 text-xs font-semibold uppercase tracking-wider mb-3">
                        Live Feed
                    </h3>
                    <div className="grid grid-cols-2 sm:grid-cols-4 md:grid-cols-6 gap-3">
                        {livePrices.map(price => (
                            <div
                                key={price.ticker}
                                className="flex items-center justify-between px-3 py-2 bg-slate-950/60 border border-slate-800 rounded-lg text-sm"
                            >
                                <span className="font-semibold text-slate-300">{price.ticker}</span>
                                <span className="font-mono text-slate-100">{price.price.toFixed(2)}</span>
                            </div>
                        ))}
                    </div>
                </section>
            )}
        </div>
    )
}

export default HomePage
