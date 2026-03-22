'use client';

import { modules } from '@/lib/sample-data';
import StatusBadge from '@/components/cards/StatusBadge';

export default function ModuleTable() {
  return (
    <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div className="px-6 py-4 border-b border-slate-100">
        <h3 className="text-sm font-semibold text-slate-700">Module Status</h3>
      </div>
      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-slate-100 bg-slate-50">
              <th className="text-left px-6 py-3 text-xs font-medium text-slate-500 uppercase tracking-wider">Name</th>
              <th className="text-left px-6 py-3 text-xs font-medium text-slate-500 uppercase tracking-wider">Type</th>
              <th className="text-left px-6 py-3 text-xs font-medium text-slate-500 uppercase tracking-wider">Status</th>
              <th className="text-left px-6 py-3 text-xs font-medium text-slate-500 uppercase tracking-wider">Stage</th>
              <th className="text-right px-6 py-3 text-xs font-medium text-slate-500 uppercase tracking-wider">LOC</th>
              <th className="text-right px-6 py-3 text-xs font-medium text-slate-500 uppercase tracking-wider">Coverage</th>
              <th className="text-left px-6 py-3 text-xs font-medium text-slate-500 uppercase tracking-wider">Last Updated</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {modules.map((mod) => (
              <tr key={mod.id} className="hover:bg-slate-50 transition-colors">
                <td className="px-6 py-3 font-medium text-slate-900">{mod.name}</td>
                <td className="px-6 py-3 text-slate-600">{mod.type}</td>
                <td className="px-6 py-3">
                  <StatusBadge status={mod.status} />
                </td>
                <td className="px-6 py-3 text-slate-600">{mod.stage}</td>
                <td className="px-6 py-3 text-right text-slate-600">{mod.linesOfCode.toLocaleString()}</td>
                <td className="px-6 py-3 text-right text-slate-600">
                  {mod.coveragePercent > 0 ? `${mod.coveragePercent}%` : '-'}
                </td>
                <td className="px-6 py-3 text-slate-500">{mod.lastUpdated}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
