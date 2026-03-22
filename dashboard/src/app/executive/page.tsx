'use client';

import KpiCard from '@/components/cards/KpiCard';
import ProgressChart from '@/components/charts/ProgressChart';
import CostChart from '@/components/charts/CostChart';
import ModuleTable from '@/components/tables/ModuleTable';
import { kpis, roiData } from '@/lib/sample-data';

export default function ExecutivePage() {
  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-slate-900">Executive Dashboard</h2>
        <p className="text-sm text-slate-500 mt-1">CardDemo COBOL modernization overview</p>
      </div>

      {/* KPI Row */}
      <div className="grid grid-cols-4 gap-4">
        {kpis.map((kpi) => (
          <KpiCard key={kpi.title} {...kpi} />
        ))}
      </div>

      {/* Progress + ROI Row */}
      <div className="grid grid-cols-4 gap-4">
        <div className="col-span-3">
          <ProgressChart />
        </div>
        <div className="col-span-1">
          <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6 h-full">
            <h3 className="text-sm font-semibold text-slate-700 mb-4">ROI Comparison</h3>
            <div className="space-y-4">
              {roiData.map((item) => (
                <div key={item.category}>
                  <p className="text-xs font-medium text-slate-500 mb-2">{item.category}</p>
                  <div className="flex items-end gap-3">
                    <div className="flex-1">
                      <p className="text-xs text-slate-400 mb-1">Traditional</p>
                      <div className="bg-red-100 rounded px-2 py-1">
                        <p className="text-sm font-semibold text-red-700">
                          {item.category === 'Total Cost'
                            ? `$${(item.traditional / 1000).toFixed(0)}K`
                            : item.category === 'Defect Rate (%)'
                            ? `${item.traditional}%`
                            : item.traditional}
                        </p>
                      </div>
                    </div>
                    <div className="flex-1">
                      <p className="text-xs text-slate-400 mb-1">AI-Driven</p>
                      <div className="bg-green-100 rounded px-2 py-1">
                        <p className="text-sm font-semibold text-green-700">
                          {item.category === 'Total Cost'
                            ? `$${(item.aiDriven / 1000).toFixed(0)}K`
                            : item.category === 'Defect Rate (%)'
                            ? `${item.aiDriven}%`
                            : item.aiDriven}
                        </p>
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Cost Chart */}
      <CostChart />

      {/* Module Table */}
      <ModuleTable />
    </div>
  );
}
