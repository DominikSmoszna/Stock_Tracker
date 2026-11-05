import {ResponsivePie} from "@nivo/pie";

interface PieDataItem {
    id: string;
    label: string;
    value: number;
}

const data: PieDataItem[] = [
    {id: 'e1', label: 'e1', value: 11},
    {id: 'e2', label: 'e2', value: 3},
    {id: 'e3', label: 'e3', value: 5},
    {id: 'e4', label: 'e4', value: 1},
];

function PortfolioPieChart() {
    return (
      <div className="w-full h-full text-slate-900">
        <ResponsivePie
            data={data}
            margin={{top: 20, right: 20, bottom: 20, left: 20}}
            innerRadius={0.65}
            activeOuterRadiusOffset={6}
            padAngle={1.5}
            cornerRadius={4}
            colors={{scheme: 'nivo'}}
            enableArcLinkLabels={false}
        />
      </div>
    );
}

export default PortfolioPieChart;
