import { ModuleStatus } from '@/lib/types';
import { STATUS_COLORS, STAGE_LABELS } from '@/lib/constants';

interface StatusBadgeProps {
  status: ModuleStatus;
}

export default function StatusBadge({ status }: StatusBadgeProps) {
  const colors = STATUS_COLORS[status];
  const label = STAGE_LABELS[status];

  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium ${colors.bg} ${colors.text}`}
    >
      <span className={`w-1.5 h-1.5 rounded-full ${colors.dot}`} />
      {label}
    </span>
  );
}
