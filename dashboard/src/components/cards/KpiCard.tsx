'use client';

interface KpiCardProps {
  title: string;
  value: string;
  target: string;
  unit: string;
  trend: 'up' | 'down' | 'neutral';
}

export default function KpiCard({ title, value, target, trend }: KpiCardProps) {
  const trendIcon = trend === 'up' ? (
    <svg className="w-4 h-4 text-green-500" fill="none" viewBox="0 0 24 24" strokeWidth={2} stroke="currentColor">
      <path strokeLinecap="round" strokeLinejoin="round" d="M4.5 19.5l15-15m0 0H8.25m11.25 0v11.25" />
    </svg>
  ) : trend === 'down' ? (
    <svg className="w-4 h-4 text-green-500" fill="none" viewBox="0 0 24 24" strokeWidth={2} stroke="currentColor">
      <path strokeLinecap="round" strokeLinejoin="round" d="M4.5 4.5l15 15m0 0V8.25m0 11.25H8.25" />
    </svg>
  ) : (
    <svg className="w-4 h-4 text-slate-400" fill="none" viewBox="0 0 24 24" strokeWidth={2} stroke="currentColor">
      <path strokeLinecap="round" strokeLinejoin="round" d="M17.25 8.25L21 12m0 0l-3.75 3.75M21 12H3" />
    </svg>
  );

  return (
    <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
      <div className="flex items-center justify-between mb-1">
        <p className="text-sm font-medium text-slate-500">{title}</p>
        {trendIcon}
      </div>
      <p className="text-3xl font-bold text-slate-900 mt-2">{value}</p>
      <p className="text-sm text-slate-400 mt-1">{target}</p>
    </div>
  );
}
