import {useState, useEffect} from 'react'
import {Client} from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import MarketHours from '../components/MarketHours'

function HomePage() {
    const [prices, setPrices] = useState({})

    useEffect(()=> {
        const client = new Client({
            webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
            onConnect: () => {
                client.subscribe('/topic/prices', (message) => {
                    const price = JSON.parse(message.body)
                    setPrices(prev => ({
                        ...prev,
                        [price.ticker]:price
                        }))
                    })
                }
            })
            client.activate()
            return () => client.deactivate()
        }, [])

  return (
      <div>
          <MarketHours/>
          {Object.values(prices).map(price=>(
              <div key={price.ticker}>
                  {price.ticker}: {price.price}
              </div>
          ))}
      </div>
  )
}

export default HomePage