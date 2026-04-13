import { Link } from 'react-router-dom'

function Navbar() {
  return (
    <nav>
        <Link to="/">Home</Link>
        <Link to="/charts">Charts</Link>
        <Link to="/compare">Compare</Link>
        <Link to="/portfolio">Portfolio</Link>
        <Link to="/etf">ETF</Link>
    </nav>
  )
}

export default Navbar