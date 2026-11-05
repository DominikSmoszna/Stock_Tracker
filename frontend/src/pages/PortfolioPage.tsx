import PortfolioPieChart from "../components/PortfolioPieChart.tsx"

function PortfolioPage() {
  return (
      <div className="max-w-7xl mx-auto p-4 sm:p-6 flex flex-col gap-6">
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
              <div className="lg:col-span-7 h-80 sm:h-96 bg-slate-900 border border-slate-800 rounded-xl p-4">
                  <PortfolioPieChart/>
              </div>
              <div className="lg:col-span-5 h-80 sm:h-96 bg-slate-900 border border-slate-800 rounded-xl p-4" />
          </div>

          <div className="h-80 sm:h-96 w-full bg-slate-900 border border-slate-800 rounded-xl p-4" />

          <div className="h-80 sm:h-96 w-full bg-slate-900 border border-slate-800 rounded-xl p-4" />
      </div>
  );
}

export default PortfolioPage
