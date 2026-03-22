'use client';

import { modules, agents } from '@/lib/sample-data';
import { STATUS_COLORS, STAGE_LABELS } from '@/lib/constants';
import AgentTable from '@/components/tables/AgentTable';

export default function ArchitecturePage() {
  const completedModules = modules.filter(
    (m) => m.coveragePercent > 0
  );
  const avgCoverage =
    completedModules.length > 0
      ? completedModules.reduce((sum, m) => sum + m.coveragePercent, 0) / completedModules.length
      : 0;

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-slate-900">Architecture Dashboard</h2>
        <p className="text-sm text-slate-500 mt-1">Module structure and agent assignments</p>
      </div>

      {/* Module Status Grid */}
      <div>
        <h3 className="text-sm font-semibold text-slate-700 mb-3">Module Status</h3>
        <div className="grid grid-cols-4 gap-4">
          {modules.map((mod) => {
            const colors = STATUS_COLORS[mod.status];
            const label = STAGE_LABELS[mod.status];
            return (
              <div
                key={mod.id}
                className={`rounded-xl border-2 p-4 ${colors.bg} border-opacity-50`}
                style={{ borderColor: 'transparent' }}
              >
                <div className="flex items-center justify-between mb-2">
                  <span className="font-mono text-sm font-bold text-slate-900">{mod.name}</span>
                  <span
                    className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium ${colors.bg} ${colors.text}`}
                  >
                    <span className={`w-1.5 h-1.5 rounded-full ${colors.dot}`} />
                    {label}
                  </span>
                </div>
                <p className="text-xs text-slate-600 mb-1">{mod.description}</p>
                <p className="text-xs text-slate-500">{mod.type} -- {mod.linesOfCode.toLocaleString()} LOC</p>
              </div>
            );
          })}
        </div>
      </div>

      {/* Agent Assignments */}
      <AgentTable />

      {/* Test Coverage Summary */}
      <div>
        <h3 className="text-sm font-semibold text-slate-700 mb-3">Test Coverage Summary</h3>
        <div className="grid grid-cols-3 gap-4">
          <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
            <p className="text-sm text-slate-500 mb-1">Average Coverage</p>
            <p className="text-3xl font-bold text-slate-900">{avgCoverage.toFixed(1)}%</p>
            <p className="text-xs text-slate-400 mt-1">Across modules with tests</p>
          </div>
          <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
            <p className="text-sm text-slate-500 mb-1">Modules with Tests</p>
            <p className="text-3xl font-bold text-slate-900">{completedModules.length}</p>
            <p className="text-xs text-slate-400 mt-1">of {modules.length} total modules</p>
          </div>
          <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
            <p className="text-sm text-slate-500 mb-1">Target Coverage</p>
            <p className="text-3xl font-bold text-slate-900">90%</p>
            <p className="text-xs text-slate-400 mt-1">Minimum line coverage required</p>
          </div>
        </div>
      </div>
    </div>
  );
}
