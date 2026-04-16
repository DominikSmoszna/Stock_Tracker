import { useState, useEffect } from 'react'

const MARKET_DATA = [
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


function MarketHours() {
    const [currentTime, setCurrentTime] = useState(new Date())

    useEffect(() => {
            const timer = setInterval(()=> setCurrentTime(new Date()),30000);
            return () => clearInterval(timer);
        },[]);

    const getMarketStatus = (market) =>  {

            const formatter = Intl.DateTimeFormat("en-US", {
                timeZone: market.timezone,
                hour: "numeric",
                minute: "numeric",
                hour12: false,
                weekday: "short",
                });

            const parts = formatter.formatToParts(currentTime);
            const getPart = (type) => parts.find((p) => p.type === type).value

            const localHour = parseInt(getPart("hour"));
            const localMinute = parseInt(getPart("minute"));
            const localDay = getPart("weekday");

            if (localDay === "Sat" || localDay === "Sun") return false;

            const currentTotalMinutes = localHour * 60 + localMinute;
            const openTotalMinutes = market.openHour * 60 + market.openMinute;
            const closeTotalMinutes = market.closeHour * 60 + market.closeMinute;

            return currentTotalMinutes >= openTotalMinutes && currentTotalMinutes <= closeTotalMinutes;
            }

    return (
        <div className="p-4 bg-slate-900 rounded-xl border border-slate-800 shadow-lg">
            <h3 className="text-slate-400 text-xs font-semibold uppercase tracking-wider mb-4">Market Status</h3>
            <div className="space-y-3">
                {MARKET_DATA.map((market) => {
                    const isOpen= getMarketStatus(market);
                    const localTimeString = currentTime.toLocaleTimeString("en-GB",{
                        timeZone: market.timezone,
                        hour: '2-digit',
                        minute: '2-digit'
                        });
                    return (
                        <div key={market.id} className="flex items-center justify-between">
                            <div className="flex flex-col">
                            <span className="text-slate-200 font-medium">{market.name}</span>
                            <span className="text-slate-500 text-[10px] front-mono">{localTimeString}</span>
                            </div>
                            <div>
                                <span className={`relative flex h-2 w-2`}>
                                    {isOpen && (<span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>)}
                                    <span className={`relative inline-flex rounded-full h-2 w-2 ${isOpen? 'bg-emerald-500' : 'bg-rose-500'}`}></span>
                                </span>
                                <span className={`text-xs font-bold uppercase ${isOpen ? 'text-emerald-500' : 'text-rose-500'}`}>
                                    {isOpen ? 'Open' : 'Closed'}
                                </span>
                            </div>
                        </div>
                    )
                }
            )}
            </div>
        </div>
    );
}

export default MarketHours