'use client';

import { useEffect, useState } from 'react';
import { useSession } from 'next-auth/react';
import Link from 'next/link';
import AppShell from '@/components/AppShell';
import StatusBadge from '@/components/StatusBadge';
import { getMyReports, searchReports, getAllProjects, getAllTeamMembers } from '@/lib/api';
import { formatDateRange } from '@/lib/utils';
import type { ReportResponseDto, ProjectDto, UserDto, ReportStatus } from '@/lib/types';

export default function MyReportsPage() {
  const { data: session } = useSession();
  const isManager = session?.isManager;

  const [reports, setReports] = useState<ReportResponseDto[]>([]);
  const [projects, setProjects] = useState<ProjectDto[]>([]);
  const [members, setMembers] = useState<UserDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [filterUser, setFilterUser] = useState('');
  const [filterProject, setFilterProject] = useState('');
  const [filterStatus, setFilterStatus] = useState('');

  async function load() {
    setLoading(true);
    setError('');
    try {
      if (isManager) {
        const [reportPage, projectList, memberList] = await Promise.all([
          searchReports({
            userId: filterUser ? Number(filterUser) : undefined,
            projectId: filterProject ? Number(filterProject) : undefined,
            status: (filterStatus as ReportStatus) || undefined,
            page: 0,
            size: 50,
          }),
          getAllProjects(),
          getAllTeamMembers(),
        ]);
        setReports(reportPage.content);
        setProjects(projectList);
        setMembers(memberList);
      } else {
        const reportPage = await getMyReports(0, 50);
        setReports(reportPage.content);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load reports.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (session) load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [session, filterUser, filterProject, filterStatus]);

  return (
    <AppShell>
      <div className="page-heading">
        <div>
          <h1>{isManager ? 'Team reports' : 'My reports'}</h1>
          <p>{isManager ? 'Review reports submitted across the team.' : 'Create, edit, and track your weekly updates.'}</p>
        </div>
        {!isManager && (
          <Link href="/my-reports/new" className="btn btn-primary">
            + New report
          </Link>
        )}
      </div>

      {isManager && (
        <div className="panel" style={{ marginBottom: 16, display: 'flex', gap: 12 }}>
          <select value={filterUser} onChange={(e) => setFilterUser(e.target.value)}>
            <option value="">All team members</option>
            {members.map((m) => (
              <option key={m.id} value={m.id}>{m.name}</option>
            ))}
          </select>
          <select value={filterProject} onChange={(e) => setFilterProject(e.target.value)}>
            <option value="">All projects</option>
            {projects.map((p) => (
              <option key={p.id} value={p.id}>{p.name}</option>
            ))}
          </select>
          <select value={filterStatus} onChange={(e) => setFilterStatus(e.target.value)}>
            <option value="">All statuses</option>
            <option value="DRAFT">Draft</option>
            <option value="SUBMITTED">Submitted</option>
            <option value="NEEDS_CORRECTION">Needs correction</option>
            <option value="APPROVED">Approved</option>
          </select>
        </div>
      )}

      {error && <p className="error-text">{error}</p>}

      <div className="panel">
        {loading ? (
          <p className="help-text">Loading…</p>
        ) : reports.length === 0 ? (
          <div className="empty-state">
            <strong>No reports yet</strong>
            <p>{isManager ? 'No reports match these filters.' : 'Create your first weekly report to get started.'}</p>
          </div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                {isManager && <th>Team member</th>}
                <th>Week</th>
                <th>Project</th>
                <th>Status</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {reports.map((r) => (
                <tr key={r.id}>
                  {isManager && <td>{r.userName}</td>}
                  <td>{formatDateRange(r.weekStart, r.weekEnd)}</td>
                  <td>{r.projectName || '—'}</td>
                  <td><StatusBadge status={r.status} /></td>
                  <td style={{ textAlign: 'right' }}>
                    <Link
                      href={isManager ? `/review/${r.id}` : `/my-reports/${r.id}`}
                      className="btn btn-sm"
                    >
                      {isManager ? 'Review' : 'Open'}
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </AppShell>
  );
}
