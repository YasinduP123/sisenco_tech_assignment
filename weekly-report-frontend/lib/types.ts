// =====================================================
// Enums (must match backend exactly)
// =====================================================

export type UserRole = 'TEAM_MEMBER' | 'MANAGER';

export type ReportStatus = 'DRAFT' | 'SUBMITTED' | 'NEEDS_CORRECTION' | 'APPROVED';

export type ReviewAction = 'APPROVED' | 'CHANGES_REQUESTED';

// =====================================================
// Core DTOs
// =====================================================

export interface UserDto {
  id: number;
  name: string;
  email: string;
  role: UserRole;
}

export interface ProjectDto {
  id: number;
  name: string;
  description: string;
  active: boolean;
}

export interface TaskEntryDto {
  id?: number;
  taskName: string;
  priority?: string;
  plannedPct?: number;
  actualPct?: number;
  status?: string;
  timePlanned?: number;
  timeSpent?: number;
  deliverable?: string;
  taskType?: string;
}

export interface ReportRequestDto {
  projectId?: number;
  weekStart: string; // ISO date "2026-09-01"
  weekEnd: string;
  nextWeekPlan?: string;
  keyBlocker?: string;
  keyAchievement?: string;
  notes?: string;
  taskEntries: TaskEntryDto[];
}

export interface ReportResponseDto {
  id: number;
  userId: number;
  userName: string;
  projectId?: number;
  projectName?: string;
  weekStart: string;
  weekEnd: string;
  status: ReportStatus;
  nextWeekPlan?: string;
  keyBlocker?: string;
  keyAchievement?: string;
  notes?: string;
  submittedAt?: string;
  updatedAt?: string;
  taskEntries: TaskEntryDto[];
  latestComment?: string;
}

export interface ReportVersionSummaryDto {
  id: number;
  versionNumber: number;
  createdAt: string;
  contentSnapshot: string;
}

export interface ReviewRequestDto {
  action: ReviewAction;
  comment?: string;
}

export interface ReviewCommentDto {
  id: number;
  managerName: string;
  action: ReviewAction;
  comment?: string;
  createdAt: string;
  reportVersionNumber?: number;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

// =====================================================
// Dashboard DTOs
// =====================================================

export interface DashboardSummaryDto {
  totalSubmittedThisWeek: number;
  complianceRatePct: number;
  needsCorrectionCount: number;
  openBlockersCount: number;
}

export interface TasksTrendDto {
  weekLabels: string[];
  completedCounts: number[];
}

export interface MemberStatusDto {
  userId: number;
  userName: string;
  status: ReportStatus | null;
}

export interface ProjectWorkloadDto {
  projectId: number;
  projectName: string;
  taskCount: number;
}

export interface TaskTypeTimeDto {
  taskType: string;
  totalHours: number;
}

export interface ActivityFeedItemDto {
  type: 'SUBMITTED' | 'APPROVED' | 'CHANGES_REQUESTED';
  userName: string;
  reportId: number;
  timestamp: string;
  summary: string;
}
