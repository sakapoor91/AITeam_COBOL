'use client';

import AuditTable from '@/components/tables/AuditTable';
import { modules } from '@/lib/sample-data';
import { ModuleStatus } from '@/lib/types';

const complianceChecks = [
  {
    title: 'Financial Accuracy',
    status: 'PASS',
    description: 'BigDecimal used for all monetary calculations',
    lastChecked: '2026-03-22',
  },
  {
    title: 'Security Review',
    status: 'PASS',
    description: 'No PII exposure, auth flows validated',
    lastChecked: '2026-03-21',
  },
  {
    title: 'Data Integrity',
    status: 'PASS',
    description: 'All VSAM-to-PostgreSQL mappings verified',
    lastChecked: '2026-03-20',
  },
  {
    title: 'FINOS CDM',
    status: 'PASS',
    description: 'Common Domain Model compliance validated',
    lastChecked: '2026-03-20',
  },
];

const approvalStages = ['Analysis', 'Design', 'Translation', 'Review', 'Approval', 'Deploy'];

export default function CompliancePage() {
  const deployedModules = modules.filter(
    (m) => m.status === ModuleStatus.DEPLOYED || m.status === ModuleStatus.VALIDATED
  );

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-slate-900">Compliance Dashboard</h2>
        <p className="text-sm text-slate-500 mt-1">Regulatory compliance and audit trail</p>
      </div>

      {/* Compliance Status Cards */}
      <div className="grid grid-cols-4 gap-4">
        {complianceChecks.map((check) => (
          <div
            key={check.title}
            className="bg-white rounded-xl border border-slate-200 shadow-sm p-6"
          >
            <div className="flex items-center justify-between mb-2">
              <p className="text-sm font-semibold text-slate-700">{check.title}</p>
              <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold bg-green-100 text-green-700">
                {check.status}
              </span>
            </div>
            <p className="text-xs text-slate-500 mb-2">{check.description}</p>
            <p className="text-xs text-slate-400">Last checked: {check.lastChecked}</p>
          </div>
        ))}
      </div>

      {/* Audit Trail */}
      <AuditTable />

      {/* Approval Chain */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
        <h3 className="text-sm font-semibold text-slate-700 mb-4">Approval Chain</h3>
        <div className="space-y-4">
          {deployedModules.map((mod) => (
            <div key={mod.id}>
              <p className="text-sm font-medium text-slate-700 mb-2">
                <span className="font-mono">{mod.name}</span>
                <span className="text-slate-400 ml-2">-- {mod.description}</span>
              </p>
              <div className="flex items-center gap-1">
                {approvalStages.map((stage, idx) => {
                  const isCompleted =
                    mod.status === ModuleStatus.DEPLOYED ||
                    (mod.status === ModuleStatus.VALIDATED && idx < 5);
                  return (
                    <div key={stage} className="flex items-center">
                      <div
                        className={`flex items-center justify-center w-24 py-1.5 rounded text-xs font-medium ${
                          isCompleted
                            ? 'bg-green-100 text-green-700'
                            : 'bg-slate-100 text-slate-400'
                        }`}
                      >
                        {stage}
                      </div>
                      {idx < approvalStages.length - 1 && (
                        <svg
                          className={`w-4 h-4 mx-0.5 flex-shrink-0 ${
                            isCompleted ? 'text-green-400' : 'text-slate-300'
                          }`}
                          fill="none"
                          viewBox="0 0 24 24"
                          strokeWidth={2}
                          stroke="currentColor"
                        >
                          <path strokeLinecap="round" strokeLinejoin="round" d="M8.25 4.5l7.5 7.5-7.5 7.5" />
                        </svg>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
