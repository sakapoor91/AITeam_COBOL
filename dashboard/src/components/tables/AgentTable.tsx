'use client';

import { agents } from '@/lib/sample-data';

export default function AgentTable() {
  return (
    <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div className="px-6 py-4 border-b border-slate-100">
        <h3 className="text-sm font-semibold text-slate-700">Agent Status</h3>
      </div>
      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-slate-100 bg-slate-50">
              <th className="text-left px-6 py-3 text-xs font-medium text-slate-500 uppercase tracking-wider">Name</th>
              <th className="text-left px-6 py-3 text-xs font-medium text-slate-500 uppercase tracking-wider">Role</th>
              <th className="text-left px-6 py-3 text-xs font-medium text-slate-500 uppercase tracking-wider">Status</th>
              <th className="text-left px-6 py-3 text-xs font-medium text-slate-500 uppercase tracking-wider">Current Task</th>
              <th className="text-right px-6 py-3 text-xs font-medium text-slate-500 uppercase tracking-wider">Tokens Used</th>
              <th className="text-right px-6 py-3 text-xs font-medium text-slate-500 uppercase tracking-wider">Cost</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {agents.map((agent) => (
              <tr key={agent.id} className="hover:bg-slate-50 transition-colors">
                <td className="px-6 py-3 font-medium text-slate-900">{agent.name}</td>
                <td className="px-6 py-3 text-slate-600">{agent.role}</td>
                <td className="px-6 py-3">
                  <span className="inline-flex items-center gap-1.5">
                    <span
                      className={`w-2 h-2 rounded-full ${
                        agent.status === 'active' ? 'bg-green-500' : 'bg-gray-400'
                      }`}
                    />
                    <span className="text-slate-600 capitalize">{agent.status}</span>
                  </span>
                </td>
                <td className="px-6 py-3 text-slate-600 text-xs">{agent.currentTask}</td>
                <td className="px-6 py-3 text-right text-slate-600">{agent.tokensUsed.toLocaleString()}</td>
                <td className="px-6 py-3 text-right text-slate-600">${agent.costUsd.toFixed(2)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
