'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import AppShell from '@/components/AppShell';
import StatusBadge from '@/components/StatusBadge';
import { getReportById, reviewReport, getCommentHistory } from '@/lib/api';
import { formatDateRange } from '@/lib/utils';
import type { ReportResponseDto, ReviewCommentDto } from '@/lib/types';

export default function ReviewPage() {
  const params = useParams();
  const router = useRouter();
  const id = Number(params.id);

  const [report, setReport] = useState<ReportResponseDto | null>(null);
  const [comments, setComments] = useState<ReviewCommentDto[]>([]);
  const [comment, setComment] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  async function load() {
    setLoading(true);
    try {
      const [r, c] = await Promise.all([getReportById(id), getCommentHistory(id)]);
      setReport(r);
      setComments(c);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load report.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); /* eslint-disable-next-line */ }, [id]);

  async function handleApprove() {
    setSaving(true);
    setError('');
    try {
      await reviewReport(id, { action: 'APPROVED' });
      router.push('/my-reports');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to approve.');
    } finally {
      setSaving(false);
    }
  }

  async function handleRequestChanges() {
    if (!comment.trim()) {
      setError('A comment is required when requesting changes.');
      return;
    }
    setSaving(true);
    setError('');
    try {
      await reviewReport(id, { action: 'CHANGES_REQUESTED', comment });
      router.push('/my-reports');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to send back for correction.');
    } finally {
      setSaving(false);
    }
  }

  if (loading) return <AppShell><p className="help-text">Loading…</p></AppShell>;
  if (!report) return <AppShell><p className="error-text">{error || 'Report not found.'}</p></AppShell>;

  const canReview = report.status === 'SUBMITTED';

  return (
    <AppShell>
      <div className="page-heading">
        <div>
          <h1>{report.userName}'s report</h1>
          <p>{formatDateRange(report.weekStart, report.weekEnd)} · {report.projectName || 'No project'}</p>
        </div>
        <StatusBadge status={report.status} />
      </div>

      <div className="panel">
        <strong>Tasks</strong>
        <table className="data-table" style={{ marginTop: 10 }}>
          <thead>
            <tr>
              <th>Task</th><th>Type</th><th>Priority</th><th>Status</th><th>Planned %</th><th>Actual %</th><th>Time (h)</th>
            </tr>
          </thead>
          <tbody>
            {report.taskEntries.map((t, i) => (
              <tr key={i}>
                <td>{t.taskName}</td>
                <td>{t.taskType}</td>
                <td>{t.priority}</td>
                <td>{t.status}</td>
                <td>{t.plannedPct}%</td>
                <td>{t.actualPct}%</td>
                <td>{t.timeSpent}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="panel">
        <strong>Blockers</strong>
        <p>{report.keyBlocker || '—'}</p>
        <strong>Achievements</strong>
        <p>{report.keyAchievement || '—'}</p>
        <strong>Next week plan</strong>
        <p>{report.nextWeekPlan || '—'}</p>
        {report.notes && (<><strong>Notes</strong><p>{report.notes}</p></>)}
      </div>

      {comments.length > 0 && (
        <div className="panel">
          <strong>Comment history</strong>
          {comments.map((c) => (
            <div key={c.id} style={{ padding: '8px 0', borderTop: '1px solid var(--border)' }}>
              <div style={{ fontWeight: 500 }}>
                {c.managerName} · {c.action === 'APPROVED' ? 'Approved' : 'Requested changes'}
                {c.reportVersionNumber ? ` (v${c.reportVersionNumber})` : ''}
              </div>
              {c.comment && <p style={{ margin: '4px 0 0' }}>{c.comment}</p>}
              <div className="help-text">{new Date(c.createdAt).toLocaleString()}</div>
            </div>
          ))}
        </div>
      )}

      {canReview && (
        <div className="panel">
          <strong>Review this report</strong>
          <div className="field" style={{ marginTop: 10 }}>
            <label>Comment (required if requesting changes)</label>
            <textarea value={comment} onChange={(e) => setComment(e.target.value)}
              placeholder="Explain what needs to change…" />
          </div>
          {error && <p className="error-text">{error}</p>}
          <div style={{ display: 'flex', gap: 10 }}>
            <button className="btn btn-primary" disabled={saving} onClick={handleApprove}>Approve</button>
            <button className="btn btn-danger" disabled={saving} onClick={handleRequestChanges}>Request changes</button>
          </div>
        </div>
      )}
    </AppShell>
  );
}