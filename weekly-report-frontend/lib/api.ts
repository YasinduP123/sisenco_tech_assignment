import { getSession } from 'next-auth/react';
import type {
  UserDto,
  ProjectDto,
  ReportRequestDto,
  ReportResponseDto,
  ReviewRequestDto,
  ReviewCommentDto,
  ReportVersionSummaryDto,
  PagedResponse,
  ReportStatus,
  DashboardSummaryDto,
  TasksTrendDto,
  MemberStatusDto,
  ProjectWorkloadDto,
  TaskTypeTimeDto,
  ActivityFeedItemDto,
} from './types';

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8082/api';

async function request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const session = await getSession();

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(session?.accessToken && { Authorization: `Bearer ${session.accessToken}` }),
      ...options.headers,
    },
  });

  if (!response.ok) {
    let errorMessage = `HTTP ${response.status}`;
    try {
      const errorBody = await response.json();
      errorMessage = errorBody.message || errorBody.error || errorMessage;
    } catch {
      // response wasn't JSON
    }
    throw new Error(errorMessage);
  }

  if (response.status === 204) return undefined as T;

  const contentType = response.headers.get('content-type');
  if (!contentType?.includes('application/json')) return undefined as T;

  return response.json();
}

// ---------- Current user ----------
export const getCurrentUserProfile = () => request<UserDto>('/users/me');

// ---------- User management (manager only) ----------
export const getAllUsers = () => request<UserDto[]>('/users');
export const getAllTeamMembers = () => request<UserDto[]>('/users/team-members');
export const getUserById = (id: number) => request<UserDto>(`/users/${id}`);
export const updateUserRole = (id: number, role: UserDto['role']) =>
  request<UserDto>(`/users/${id}/role`, { method: 'PATCH', body: JSON.stringify({ role }) });
export const deleteUser = (id: number) => request<void>(`/users/${id}`, { method: 'DELETE' });

// ---------- Projects ----------
export const getAllProjects = () => request<ProjectDto[]>('/projects');
export const createProject = (dto: Omit<ProjectDto, 'id' | 'active'>) =>
  request<ProjectDto>('/projects', { method: 'POST', body: JSON.stringify(dto) });
export const updateProject = (id: number, dto: Partial<ProjectDto>) =>
  request<ProjectDto>(`/projects/${id}`, { method: 'PUT', body: JSON.stringify(dto) });
export const deleteProject = (id: number) =>
  request<void>(`/projects/${id}`, { method: 'DELETE' });

// ---------- Reports: team member ----------
export const createDraftReport = (dto: ReportRequestDto) =>
  request<ReportResponseDto>('/reports', { method: 'POST', body: JSON.stringify(dto) });
export const updateReport = (id: number, dto: ReportRequestDto) =>
  request<ReportResponseDto>(`/reports/${id}`, { method: 'PUT', body: JSON.stringify(dto) });
export const submitReport = (id: number) =>
  request<ReportResponseDto>(`/reports/${id}/submit`, { method: 'POST' });
export const getMyReports = (page = 0, size = 10) =>
  request<PagedResponse<ReportResponseDto>>(`/reports/my?page=${page}&size=${size}`);

// ---------- Reports: shared ----------
export const getReportById = (id: number) => request<ReportResponseDto>(`/reports/${id}`);
export const getReportVersionHistory = (id: number) =>
  request<ReportVersionSummaryDto[]>(`/reports/${id}/versions`);

// ---------- Reports: manager search ----------
export const searchReports = (filters?: {
  userId?: number;
  projectId?: number;
  status?: ReportStatus;
  weekStart?: string;
  weekEnd?: string;
  page?: number;
  size?: number;
}) => {
  const params = new URLSearchParams();
  if (filters?.userId) params.append('userId', String(filters.userId));
  if (filters?.projectId) params.append('projectId', String(filters.projectId));
  if (filters?.status) params.append('status', filters.status);
  if (filters?.weekStart) params.append('weekStart', filters.weekStart);
  if (filters?.weekEnd) params.append('weekEnd', filters.weekEnd);
  params.append('page', String(filters?.page ?? 0));
  params.append('size', String(filters?.size ?? 10));
  return request<PagedResponse<ReportResponseDto>>(`/reports?${params.toString()}`);
};

// ---------- Review (manager only) ----------
export const reviewReport = (reportId: number, dto: ReviewRequestDto) =>
  request<ReportResponseDto>(`/reports/${reportId}/review`, {
    method: 'POST',
    body: JSON.stringify(dto),
  });
export const getCommentHistory = (reportId: number) =>
  request<ReviewCommentDto[]>(`/reports/${reportId}/comments`);

// ---------- Dashboard (manager only) ----------
export const getDashboardSummary = (weekStart: string) =>
  request<DashboardSummaryDto>(`/dashboard/summary?weekStart=${weekStart}`);
export const getTasksCompletedTrend = (userId?: number, from?: string, to?: string) => {
  const params = new URLSearchParams();
  if (userId) params.append('userId', String(userId));
  if (from) params.append('from', from);
  if (to) params.append('to', to);
  return request<TasksTrendDto>(`/dashboard/tasks-trend?${params.toString()}`);
};
export const getStatusByMember = (weekStart: string) =>
  request<MemberStatusDto[]>(`/dashboard/status-by-member?weekStart=${weekStart}`);
export const getWorkloadByProject = (from: string, to: string) =>
  request<ProjectWorkloadDto[]>(`/dashboard/workload-by-project?from=${from}&to=${to}`);
export const getTimeByTaskType = (from: string, to: string) =>
  request<TaskTypeTimeDto[]>(`/dashboard/time-by-task-type?from=${from}&to=${to}`);
export const getRecentActivity = (limit = 10) =>
  request<ActivityFeedItemDto[]>(`/dashboard/recent-activity?limit=${limit}`);
