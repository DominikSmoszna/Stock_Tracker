import {BrowserRouter, Routes, Route} from 'react-router-dom'
import Navbar from './components/Navbar'
import HomePage from './pages/HomePage'
import ChartsPage from './pages/ChartsPage'
import ComparePage from './pages/ComparePage'
import PortfolioPage from './pages/PortfolioPage'
import EtfPage from './pages/EtfPage'

function App() {
  return (
      <BrowserRouter>
          <div className="min-h-dvh bg-slate-950 text-slate-100 font-sans pl-16">
              <Navbar />
              <main className="min-h-dvh">
                  <Routes>
                      <Route path="/" element={<HomePage/>} />
                      <Route path="/charts" element={<ChartsPage/>} />
                      <Route path="/compare" element={<ComparePage/>} />
                      <Route path="/portfolio" element={<PortfolioPage/>} />
                      <Route path="/etf" element={<EtfPage/>} />
                  </Routes>
              </main>
          </div>
      </BrowserRouter>
  )
}

export default App
