import { Link } from 'react-router-dom'
import { useState } from 'react'
import { Home, LineChart, GitCompare, Briefcase, PieChart} from 'lucide-react'

function Navbar() {
  const [isExpanded, setIsExpanded] = useState(false);
  return (
    <div
    className={`fixed left-0 top-0 h-full bg-gray-900 transition-all duration-300 ${isExpanded ? 'w-48' : 'w-16'}`}
    onMouseEnter={()=> setIsExpanded(true)}
    onMouseLeave={()=> setIsExpanded(false)}
    >
    <nav>
        <Link to="/" className="flex items-center gap-3 p-3 text-white hover:bg-gray-700 rounded-lg">
            <Home size={20}/>
            {isExpanded && <span>Home</span>}
        </Link>
        <Link to="/charts" className="flex items-center gap-3 p-3 text-white hover:bg-gray-700 rounded-lg">
            <LineChart size={20}/>
            {isExpanded && <span>Charts</span>}
        </Link>
        <Link to="/compare" className="flex items-center gap-3 p-3 text-white hover:bg-gray-700 rounded-lg">
            <GitCompare size={20}/>
            {isExpanded && <span>Compare</span>}
        </Link>
        <Link to="/portfolio" className="flex items-center gap-3 p-3 text-white hover:bg-gray-700 rounded-lg">
            <Briefcase size={20}/>
            {isExpanded && <span>Portfolio</span>}
        </Link>
        <Link to="/etf" className="flex items-center gap-3 p-3 text-white hover:bg-gray-700 rounded-lg">
            <PieChart size={20}/>
            {isExpanded && <span>ETFs</span>}
        </Link>
    </nav>
    </div>
  )
}

export default Navbar