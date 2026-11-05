import { NavLink } from 'react-router-dom'
import { useState } from 'react'
import { Home, LineChart, GitCompare, Briefcase, PieChart } from 'lucide-react'

const NAV_ITEMS = [
  { to: '/', label: 'Home', icon: Home },
  { to: '/charts', label: 'Charts', icon: LineChart },
  { to: '/compare', label: 'Compare', icon: GitCompare },
  { to: '/portfolio', label: 'Portfolio', icon: Briefcase },
  { to: '/etf', label: 'ETFs', icon: PieChart },
]

function Navbar() {
  const [isExpanded, setIsExpanded] = useState(false)

  return (
    <aside
      className={`fixed left-0 top-0 h-full bg-slate-900/95 backdrop-blur border-r border-slate-800 z-50 transition-all duration-200 ${
        isExpanded ? 'w-48' : 'w-16'
      }`}
      onMouseEnter={() => setIsExpanded(true)}
      onMouseLeave={() => setIsExpanded(false)}
    >
      <nav className="flex flex-col gap-1 p-2">
        {NAV_ITEMS.map(({ to, label, icon: Icon }) => (
          <NavLink
            key={to}
            to={to}
            end={to === '/'}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors whitespace-nowrap overflow-hidden ${
                isActive
                  ? 'bg-slate-800 text-white'
                  : 'text-slate-400 hover:text-slate-100 hover:bg-slate-800/60'
              }`
            }
          >
            <Icon size={20} className="shrink-0" />
            {isExpanded && <span>{label}</span>}
          </NavLink>
        ))}
      </nav>
    </aside>
  )
}

export default Navbar
