import { ModuleStatus } from './types';

export const STATUS_COLORS: Record<ModuleStatus, { bg: string; text: string; dot: string }> = {
  [ModuleStatus.NOT_STARTED]: { bg: 'bg-gray-100', text: 'text-gray-700', dot: 'bg-gray-400' },
  [ModuleStatus.IN_ANALYSIS]: { bg: 'bg-blue-100', text: 'text-blue-700', dot: 'bg-blue-500' },
  [ModuleStatus.IN_DESIGN]: { bg: 'bg-purple-100', text: 'text-purple-700', dot: 'bg-purple-500' },
  [ModuleStatus.IN_TRANSLATION]: { bg: 'bg-amber-100', text: 'text-amber-700', dot: 'bg-amber-500' },
  [ModuleStatus.VALIDATED]: { bg: 'bg-green-100', text: 'text-green-700', dot: 'bg-green-500' },
  [ModuleStatus.DEPLOYED]: { bg: 'bg-emerald-100', text: 'text-emerald-700', dot: 'bg-emerald-500' },
};

export const STAGE_LABELS: Record<ModuleStatus, string> = {
  [ModuleStatus.NOT_STARTED]: 'Not Started',
  [ModuleStatus.IN_ANALYSIS]: 'Analysis',
  [ModuleStatus.IN_DESIGN]: 'Design',
  [ModuleStatus.IN_TRANSLATION]: 'Translation',
  [ModuleStatus.VALIDATED]: 'Validated',
  [ModuleStatus.DEPLOYED]: 'Deployed',
};

export const STAGE_CHART_COLORS: Record<string, string> = {
  'Not Started': '#9ca3af',
  'Analysis': '#3b82f6',
  'Design': '#a855f7',
  'Translation': '#f59e0b',
  'Validated': '#22c55e',
  'Deployed': '#10b981',
};

export const KPI_TARGETS = {
  modulesCompleted: 7,
  costPerLine: 2.50,
  equivalencePassRate: 99.5,
  projectedCompletionWeek: 20,
};

export const NAV_ITEMS = [
  { href: '/executive', label: 'Executive', icon: 'chart' },
  { href: '/architecture', label: 'Architecture', icon: 'blocks' },
  { href: '/operations', label: 'Operations', icon: 'activity' },
  { href: '/compliance', label: 'Compliance', icon: 'shield' },
];
