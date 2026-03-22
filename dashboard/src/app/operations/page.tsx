'use client';

import KpiCard from '@/components/cards/KpiCard';
import AgentTable from '@/components/tables/AgentTable';
import TokenChart from '@/components/charts/TokenChart';
import VelocityChart from '@/components/charts/VelocityChart';
import { agents } from '@/lib/sample-data';

export default function OperationsPage() {
  const activeAgents = agents.filter((a) => a.status === 'active').length;
  const totalCostPerHour = agents.reduce((sum, a) => sum + a.costUsd, 0) / 14; // avg over 14 days, rough hourly
  const errorsLast24h = 1; // from audit entries with FAIL

  const opsKpis = [
    {
      title: 'Active Agents',
      value: String(activeAgents),
      target: `${agents.length} total`,
      unit: 'agents',
      trend: 'neutral' as const,
    },
    {
      title: 'Token Burn Rate',
      value: `$${totalCostPerHour.toFixed(2)}`,
      target: 'per hour avg',
      unit: '$/hr',
      trend: 'down' as const,
    },
    {
      title: 'Errors (24h)',
      value: String(errorsLast24h),
      target: '0 target',
      unit: 'errors',
      trend: 'down' as const,
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-slate-900">Operations Dashboard</h2>
        <p className="text-sm text-slate-500 mt-1">Agent performance and resource consumption</p>
      </div>

      {/* KPI Row */}
      <div className="grid grid-cols-3 gap-4">
        {opsKpis.map((kpi) => (
          <KpiCard key={kpi.title} {...kpi} />
        ))}
      </div>

      {/* Agent Table */}
      <AgentTable />

      {/* Charts Side by Side */}
      <div className="grid grid-cols-2 gap-4">
        <TokenChart />
        <VelocityChart />
      </div>
    </div>
  );
}
