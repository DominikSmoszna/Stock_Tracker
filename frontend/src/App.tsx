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
          <Navbar />
          <div className={`transition-all duration-300 ml-16`}>
          <Routes>
              <Route path="/" element = {<HomePage/>} />
              <Route path="/charts" element = {<ChartsPage/>} />
              <Route path="/compare" element = {<ComparePage/>} />
              <Route path="/portfolio" element = {<PortfolioPage/>} />
              <Route path="/etf" element = {<EtfPage/>} />
          </Routes>
          </div>
      </BrowserRouter>
  )
}

export default App