import { useState, useEffect } from 'react'
import { Market } from "../types/market.ts"

const MARKET_DATA: Market[] = [
    {
        id: "london",
        name: "London",
        timezone: "Europe/London",
        openHour: 8,
        openMinute: 0,
        closeHour: 16,
        closeMinute: 0,
    },
    {
        id: "newyork",
        name: "New York",
        timezone: "America/New_York",
        openHour: 9,
        openMinute: 30,
        closeHour: 16,
        closeMinute: 0,
    },
    {
        id: "tokyo",
        name: "Tokyo",
        timezone: "Asia/Tokyo",
        openHour: 9,
        openMinute: 0,
        closeHour: 15,
        closeMinute: 0,
    },
    {
        id: "sydney",
        name: "Sydney",
        timezone: "Australia/Sydney",
        openHour: 10,
        openMinute: 0,
        closeHour: 16,
        closeMinute: 0,
    }
]

function getMarketTimeInfo(market: Market, currentTime: Date) {
    const formatter = new Intl.DateTimeFormat("en-US", {
        timeZone: market.timezone,
        hour: "numeric",
        minute: "numeric",
        hour12: false,
        weekday: "short",
    })

    const parts = formatter.formatToParts(currentTime)
    const getPart = (type: string) => parts.find((p) => p.type === type)?.value ?? ''

    const hour = parseInt(getPart("hour"), 10)
    const minute = parseInt(getPart("minute"), 10)
    const weekday = getPart("weekday")
    const isWeekend = weekday === "Sat" || weekday === "Sun"

    const currentMinutes = hour * 60 + minute
    const openMinutes = market.openHour * 60 + market.openMinute
    const closeMinutes = market.closeHour * 60 + market.closeMinute
    const isOpen = !isWeekend && currentMinutes >= openMinutes && currentMinutes <= closeMinutes

    const localTimeString = currentTime.toLocaleTimeString("en-GB", {
        timeZone: market.timezone,
        hour: '2-digit',
        minute: '2-digit',
    })

    return {
        isOpen,
        isWeekend,
        localTimeString,
        currentPos: (currentMinutes / 1440) * 100,
        openPos: (openMinutes / 1440) * 100,
        marketWidth: ((closeMinutes - openMinutes) / 1440) * 100,
    }
}

function MarketHours() {
    const [currentTime, setCurrentTime] = useState(new Date())

    useEffect(() => {
        const timer = setInterval(() => setCurrentTime(new Date()), 30000)
        return () => clearInterval(timer)
    }, [])

    return (
        <section className="p-4 sm:p-5 bg-slate-900 rounded-xl border border-slate-800">
            <h3 className="text-slate-400 text-xs font-semibold uppercase tracking-wider mb-4">
                Market Status
            </h3>
            <div className="divide-y divide-slate-800/80">
                {MARKET_DATA.map((market) => {
                    const { isOpen, isWeekend, localTimeString, currentPos, openPos, marketWidth } =
                        getMarketTimeInfo(market, currentTime)

                    return (
                        <div
                            key={market.id}
                            className="flex items-center gap-3 py-2.5 first:pt-0 last:pb-0"
                        >
                            <div className="w-24 shrink-0">
                                <div className="text-sm text-slate-200 font-medium">{market.name}</div>
                                <div className="text-xs text-slate-500 font-mono">{localTimeString}</div>
                            </div>

                            <div className="relative h-2.5 flex-1 bg-slate-800 rounded-full overflow-hidden">
                                {!isWeekend && (
                                    <div
                                        className="absolute h-full bg-slate-600/60 rounded-full"
                                        style={{ left: `${openPos}%`, width: `${marketWidth}%` }}
                                    />
                                )}
                                <div
                                    className="absolute h-full w-0.5 bg-amber-400"
                                    style={{ left: `${currentPos}%` }}
                                />
                            </div>

                            <div className="flex items-center justify-end gap-2 w-20 shrink-0">
                                <span className="relative flex h-2 w-2">
                                    {isOpen && (
                                        <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75" />
                                    )}
                                    <span
                                        className={`relative inline-flex rounded-full h-2 w-2 ${
                                            isOpen ? 'bg-emerald-500' : 'bg-rose-500'
                                        }`}
                                    />
                                </span>
                                <span
                                    className={`text-xs font-medium uppercase ${
                                        isOpen ? 'text-emerald-400' : 'text-rose-400'
                                    }`}
                                >
                                    {isOpen ? 'Open' : 'Closed'}
                                </span>
                            </div>
                        </div>
                    )
                })}
            </div>
        </section>
    )
}

export default MarketHours
