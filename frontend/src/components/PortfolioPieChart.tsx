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
      <div className="w-full h-full text-black">
        <ResponsivePie
            data={data}
            margin={{top: 25, right: 25, bottom: 25, left: 25}}
            innerRadius={0.6}
            activeOuterRadiusOffset={8}
            padAngle={1}
            cornerRadius={3}
            colors={{scheme: 'nivo'}}
            enableArcLinkLabels={false}
        />
    </div>
    );
}

export default PortfolioPieChart;