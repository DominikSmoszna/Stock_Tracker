import PortfolioPieChart from "../components/PortfolioPieChart.tsx"

function PortfolioPage() {
  return (
      <div className="w-[calc(100%-4rem)] max-w-full font-sans flex flex-col gap-4 p-8 lg:pl-36 lg:pr-36 pl-16 pr-16 min-h-dvh text-white ml-16">
          <div className="rounded-xl w-full h-[40%] flex flex-col lg:flex-row gap-8  p-8">
              <div className="aspect-square lg:w-[60%] w-full bg-slate-600 rounded-xl">
                  <PortfolioPieChart/>
              </div>
              <div className="lg:h-auto lg:w-[40%] w-full h-80 bg-slate-600 rounded-xl">

              </div>
          </div>
          <div className="rounded-xl h-100 w-full bg-amber-100 p-8">
              <div className="bg-amber-700 rounded-xl w-full h-full">

              </div>
          </div>
          <div className="rounded-xl h-100 w-full bg-amber-100 p-8">
              <div className="bg-amber-700 rounded-xl w-full h-full">

              </div>
          </div>
      </div>
  );
}

export default PortfolioPage