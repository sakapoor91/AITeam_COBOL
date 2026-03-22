export enum ModuleStatus {
  NOT_STARTED = 'NOT_STARTED',
  IN_ANALYSIS = 'IN_ANALYSIS',
  IN_DESIGN = 'IN_DESIGN',
  IN_TRANSLATION = 'IN_TRANSLATION',
  VALIDATED = 'VALIDATED',
  DEPLOYED = 'DEPLOYED',
}

export interface ModuleData {
  id: string;
  name: string;
  type: string;
  status: ModuleStatus;
  stage: string;
  linesOfCode: number;
  coveragePercent: number;
  lastUpdated: string;
  description: string;
}

export interface AgentData {
  id: string;
  name: string;
  role: string;
  status: 'active' | 'idle';
  currentTask: string;
  tokensUsed: number;
  tasksCompleted: number;
  costUsd: number;
}

export interface KpiData {
  title: string;
  value: string;
  target: string;
  unit: string;
  trend: 'up' | 'down' | 'neutral';
}

export interface AuditEntry {
  id: string;
  timestamp: string;
  agent: string;
  action: string;
  module: string;
  result: 'PASS' | 'FAIL';
  details: string;
}

export interface TimeSeriesPoint {
  date: string;
  value: number;
  label?: string;
}

export interface RoiData {
  category: string;
  traditional: number;
  aiDriven: number;
}
