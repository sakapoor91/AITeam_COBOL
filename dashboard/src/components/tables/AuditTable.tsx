'use client';

import { auditEntries } from '@/lib/sample-data';

export default function AuditTable() {
  return (
    <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div className="px-6 py-4 border-b border-slate-100">
        <h3 className="text-sm font-semibold text-slate-700">Audit Trail</h3>
      </div>
      <div className="overflow-x-auto max-h-[480px] overflow-y-auto">
        <table className="w-full text-sm">
          <thead className="sticky top-0 bg-slate-50">
            <tr className="border-b border-slate-100">
              <th className="text-left px-6 py-3 text-xs font-medium text-slate-500 uppercase tracking-wider">Timestamp</th>
              <th className="text-left px-6 py-3 text-xs font-medium text-slate-500 uppercase tracking-wider">Agent</th>
              <th className="text-left px-6 py-3 text-xs font-medium text-slate-500 uppercase tracking-wider">Action</th>
              <th className="text-left px-6 py-3 text-xs font-medium text-slate-500 uppercase tracking-wider">Module</th>
              <th className="text-left px-6 py-3 text-xs font-medium text-slate-500 uppercase tracking-wider">Result</th>
              <th className="text-left px-6 py-3 text-xs font-medium text-slate-500 uppercase tracking-wider">Details</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {auditEntries.map((entry) => (
              <tr key={entry.id} className="hover:bg-slate-50 transition-colors">
                <td className="px-6 py-3 text-slate-500 whitespace-nowrap">{entry.timestamp}</td>
                <td className="px-6 py-3 font-medium text-slate-700">{entry.agent}</td>
                <td className="px-6 py-3 text-slate-600">{entry.action}</td>
                <td className="px-6 py-3 text-slate-600 font-mono text-xs">{entry.module}</td>
                <td className="px-6 py-3">
                  <span
                    className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${
                      entry.result === 'PASS'
                        ? 'bg-green-100 text-green-700'
                        : 'bg-red-100 text-red-700'
                    }`}
                  >
                    {entry.result}
                  </span>
                </td>
                <td className="px-6 py-3 text-slate-500 text-xs">{entry.details}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
