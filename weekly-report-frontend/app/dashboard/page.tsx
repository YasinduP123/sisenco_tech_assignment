'use client';

import { useEffect, useState } from 'react';
import AppShell from '@/components/AppShell';
import {
  getDashboardSummary,
  getTasksCompletedTrend,
  getStatusByMember,
  getTimeByTaskType,
  getRecentActivity,
} from '@/lib/api';
import { currentWeekRange, toISODate } from '@/lib/utils';
import StatusBadge from '@/components/StatusBadge';
import type {
  DashboardSummaryDto, TasksTrendDto, MemberStatusDto, TaskTypeTimeDto, ActivityFeedItemDto,
} from '@/lib/types';

// Returns a range spanning the last N weeks up to (and including) the current week.
// Used for trend/time-by-type charts, which need history — not just "this week".
function lastNWeeksRange(n: number) {
  const week = currentWeekRange();
  const from = new Date(week.start);
  from.setDate(from.getDate() - n * 7);
  return { start: toISODate(from), end: week.end };
}

export default function DashboardPage() {
  const week = currentWeekRange();
  const trendRange = lastNWeeksRange(8); // last 8 weeks of history

  const [summary, setSummary] = useState<DashboardSummaryDto | null>(null);
  const [trend, setTrend] = useState<TasksTrendDto | null>(null);
  const [statusByMember, setStatusByMember] = useState<MemberStatusDto[]>([]);
  const [timeByType, setTimeByType] = useState<TaskTypeTimeDto[]>([]);
  const [activity, setActivity] = useState<ActivityFeedItemDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    async function load() {
      try {
        const [s, t, m, tt, a] = await Promise.all([
          getDashboardSummary(week.start),
          getTasksCompletedTrend(undefined, trendRange.start, trendRange.end),
          getStatusByMember(week.start),
          getTimeByTaskType(trendRange.start, trendRange.end),
          getRecentActivity(8),
        ]);
        setSummary(s); setTrend(t); setStatusByMember(m); setTimeByType(tt); setActivity(a);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load dashboard.');
      } finally {
        setLoading(false);
      }
    }
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const maxHours = Math.max(1, ...timeByType.map((t) => t.totalHours));
  const maxTrend = Math.max(1, ...(trend?.completedCounts || [1]));

  return (
    <AppShell>
      <div className="page-heading">
        <div>
          <h1>Dashboard</h1>
          <p>Team activity for the week of {week.start}.</p>
        </div>
      </div>

      {error && <p className="error-text">{error}</p>}
      {loading ? (
        <p className="help-text">Loading…</p>
      ) : (
        <>
          <div className="metric-grid">
            <div className="metric-card">
              <div className="label">Submitted this week</div>
              <div className="value">{summary?.totalSubmittedThisWeek ?? 0}</div>
            </div>
            <div className="metric-card">
              <div className="label">Compliance rate</div>
              <div className="value">{summary?.complianceRatePct?.toFixed(0) ?? 0}%</div>
            </div>
            <div className="metric-card">
              <div className="label">Needs correction</div>
              <div className="value">{summary?.needsCorrectionCount ?? 0}</div>
            </div>
            <div className="metric-card">
              <div className="label">Open blockers</div>
              <div className="value">{summary?.openBlockersCount ?? 0}</div>
            </div>
          </div>

          <div className="panel">
            <strong>Tasks completed trend</strong>
            <p className="help-text" style={{ marginTop: 4 }}>Last 8 weeks</p>
            {!trend || trend.weekLabels.length === 0 ? (
              <p className="help-text" style={{ marginTop: 16 }}>No completed tasks in this range yet.</p>
            ) : (
              <div style={{ display: 'flex', alignItems: 'flex-end', gap: 10, height: 140, marginTop: 16 }}>
                {trend.weekLabels.map((label, i) => (
                  <div key={`${label}-${i}`} style={{ flex: 1, textAlign: 'center' }}>
                    <div style={{
                      height: `${(trend.completedCounts[i] / maxTrend) * 100}px`,
                      background: 'var(--accent)', borderRadius: 3, marginBottom: 6,
                    }} />
                    <div className="help-text" style={{ fontSize: 11 }}>{label}</div>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="panel">
            <strong>Status by team member — this week</strong>
            <table className="data-table" style={{ marginTop: 10 }}>
              <thead><tr><th>Team member</th><th>Status</th></tr></thead>
              <tbody>
                {statusByMember.map((m) => (
                  <tr key={m.userId}>
                    <td>{m.userName}</td>
                    <td><StatusBadge status={m.status} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="panel">
            <strong>Time spent by task type</strong>
            <p className="help-text" style={{ marginTop: 4 }}>Last 8 weeks</p>
            {timeByType.length === 0 ? (
              <p className="help-text" style={{ marginTop: 12 }}>No time logged yet.</p>
            ) : (
              <div style={{ marginTop: 12 }}>
                {timeByType.map((t) => (
                  <div key={t.taskType} style={{ marginBottom: 10 }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, marginBottom: 4 }}>
                      <span>{t.taskType}</span>
                      <span className="help-text">{t.totalHours}h</span>
                    </div>
                    <div style={{ background: 'var(--bg)', borderRadius: 3, height: 8 }}>
                      <div style={{
                        width: `${(t.totalHours / maxHours) * 100}%`,
                        background: 'var(--accent)', height: 8, borderRadius: 3,
                      }} />
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="panel">
            <strong>Recent activity</strong>
            {activity.length === 0 ? (
              <p className="help-text">No recent activity.</p>
            ) : (
              activity.map((a, i) => (
                <div key={i} style={{ padding: '8px 0', borderTop: i > 0 ? '1px solid var(--border)' : 'none' }}>
                  <div style={{ fontWeight: 500 }}>{a.summary}</div>
                  <div className="help-text">{new Date(a.timestamp).toLocaleString()}</div>
                </div>
              ))
            )}
          </div>
        </>
      )}
    </AppShell>
  );
}