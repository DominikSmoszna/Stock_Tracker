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
                    const formatter = new Intl.DateTimeFormat("en-US", {
                        timeZone: market.timezone,
                        hour: "numeric",
                        minute: "numeric",
                        hour12: false,
                        weekday: "short"
                        });
                    const parts = formatter.formatToParts(currentTime);
                    const getPart = (type) => parts.find((p) => p.type === type).value
                    const h = parseInt(getPart("hour"));
                    const m = parseInt(getPart("minute"));
                    const localDay = getPart("weekday");
                    const currentPos = ((h*60+m)/1440)*100;
                    const openPos = ((market.openHour*60 + market.openMinute)/1440)*100;
                    const closePos = ((market.closeHour*60)/1440)*100;
                    const marketWidth = closePos - openPos;
                    const isWeekend = localDay === "Sat" || localDay === "Sun";
                    return (
                        <div key={market.id} className="flex items-center justify-between hover:bg-slate-800 border-b border-slate-800 last:border-0">
                            <div className="flex flex-col w-[80px]">
                            <span className="text-slate-200 font-medium">{market.name}</span>
                            <span className="text-slate-500 text-[10px] front-mono">{localTimeString}</span>
                            </div>
                            <div className="relative h-4 w-full bg-slate-700 rounded-fill overflow-hidden mt-1 m-2">
                                {!isWeekend && (
                                <div className="absolute h-full bg-slate-300 opacity-40" style={{left: `${openPos}%`, width: `${marketWidth}%`}}></div>
                                )}
                                <div className="absolute h-full w-0.5 bg-yellow-400 z-10 shadow-[0_0_10px_rgba(250,204,21,0.8)]" style={{left: `${currentPos}%`}}></div>
                            </div>
                            <div className="flex items-center justify-start gap-2 w-[70px] shrink-0">
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