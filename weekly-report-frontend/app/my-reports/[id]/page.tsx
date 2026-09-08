'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import AppShell from '@/components/AppShell';
import StatusBadge from '@/components/StatusBadge';
import {
  getReportById,
  createDraftReport,
  updateReport,
  submitReport,
  getAllProjects,
  getReportVersionHistory,
} from '@/lib/api';
import { currentWeekRange } from '@/lib/utils';
import type { ReportRequestDto, ReportResponseDto, ProjectDto, TaskEntryDto, ReportVersionSummaryDto } from '@/lib/types';

const EMPTY_TASK: TaskEntryDto = {
  taskName: '', priority: 'Medium', plannedPct: 100, actualPct: 0,
  status: 'Not started', timePlanned: 0, timeSpent: 0, deliverable: '', taskType: 'Development',
};

export default function ReportEditorPage() {
  const params = useParams();
  const router = useRouter();
  const idParam = params.id as string;
  const isNew = idParam === 'new';

  const [projects, setProjects] = useState<ProjectDto[]>([]);
  const [report, setReport] = useState<ReportResponseDto | null>(null);
  const [versions, setVersions] = useState<ReportVersionSummaryDto[]>([]);
  const [showVersions, setShowVersions] = useState(false);

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const week = currentWeekRange();
  const [form, setForm] = useState<ReportRequestDto>({
    projectId: undefined,
    weekStart: week.start,
    weekEnd: week.end,
    nextWeekPlan: '',
    keyBlocker: '',
    keyAchievement: '',
    notes: '',
    taskEntries: [{ ...EMPTY_TASK }],
  });

  useEffect(() => {
    async function load() {
      try {
        const projectList = await getAllProjects();
        setProjects(projectList);

        if (!isNew) {
          const existing = await getReportById(Number(idParam));
          setReport(existing);
          setForm({
            projectId: existing.projectId,
            weekStart: existing.weekStart,
            weekEnd: existing.weekEnd,
            nextWeekPlan: existing.nextWeekPlan || '',
            keyBlocker: existing.keyBlocker || '',
            keyAchievement: existing.keyAchievement || '',
            notes: existing.notes || '',
            taskEntries: existing.taskEntries.length ? existing.taskEntries : [{ ...EMPTY_TASK }],
          });
        }
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load report.');
      } finally {
        setLoading(false);
      }
    }
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [idParam]);

  const editable = isNew || report?.status === 'DRAFT' || report?.status === 'NEEDS_CORRECTION';

  function updateTask(index: number, patch: Partial<TaskEntryDto>) {
    setForm((f) => {
      const tasks = [...f.taskEntries];
      tasks[index] = { ...tasks[index], ...patch };
      return { ...f, taskEntries: tasks };
    });
  }

  function addTask() {
    setForm((f) => ({ ...f, taskEntries: [...f.taskEntries, { ...EMPTY_TASK }] }));
  }

  function removeTask(index: number) {
    setForm((f) => ({ ...f, taskEntries: f.taskEntries.filter((_, i) => i !== index) }));
  }

  async function handleSaveDraft() {
    setSaving(true);
    setError('');
    try {
      if (isNew) {
        const created = await createDraftReport(form);
        router.replace(`/my-reports/${created.id}`);
      } else {
        const updated = await updateReport(Number(idParam), form);
        setReport(updated);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save report.');
    } finally {
      setSaving(false);
    }
  }

  async function handleSubmit() {
    setSaving(true);
    setError('');
    try {
      let targetId = report?.id;
      if (isNew) {
        const created = await createDraftReport(form);
        targetId = created.id;
      } else {
        await updateReport(Number(idParam), form);
      }
      const submitted = await submitReport(targetId as number);
      router.replace(`/my-reports`);
      void submitted;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to submit report.');
    } finally {
      setSaving(false);
    }
  }

  async function loadVersions() {
    if (!report) return;
    const v = await getReportVersionHistory(report.id);
    setVersions(v);
    setShowVersions(true);
  }

  if (loading) {
    return <AppShell><p className="help-text">Loading…</p></AppShell>;
  }

  return (
    <AppShell>
      <div className="page-heading">
        <div>
          <h1>{isNew ? 'New weekly report' : `Week report`}</h1>
          <p>{report ? <StatusBadge status={report.status} /> : 'Fill in your weekly update below.'}</p>
        </div>
        {report && report.status === 'NEEDS_CORRECTION' && (
          <button className="btn btn-sm" onClick={loadVersions}>View version history</button>
        )}
      </div>

      {report?.latestComment && report.status === 'NEEDS_CORRECTION' && (
        <div className="panel" style={{ borderColor: 'var(--status-correction)', marginBottom: 16 }}>
          <strong style={{ color: 'var(--status-correction)' }}>Manager feedback</strong>
          <p style={{ margin: '6px 0 0' }}>{report.latestComment}</p>
        </div>
      )}

      {showVersions && (
        <div className="panel" style={{ marginBottom: 16 }}>
          <strong>Past versions</strong>
          {versions.length === 0 ? (
            <p className="help-text">No previous versions yet.</p>
          ) : (
            versions.map((v) => (
              <div key={v.id} style={{ padding: '8px 0', borderTop: '1px solid var(--border)' }}>
                <div style={{ fontWeight: 500 }}>Version {v.versionNumber}</div>
                <div className="help-text">{new Date(v.createdAt).toLocaleString()}</div>
              </div>
            ))
          )}
        </div>
      )}

      {error && <p className="error-text" style={{ marginBottom: 12 }}>{error}</p>}

      <div className="panel">
        <div className="field-row">
          <div className="field">
            <label>Week start</label>
            <input type="date" disabled={!editable} value={form.weekStart}
              onChange={(e) => setForm({ ...form, weekStart: e.target.value })} />
          </div>
          <div className="field">
            <label>Week end</label>
            <input type="date" disabled={!editable} value={form.weekEnd}
              onChange={(e) => setForm({ ...form, weekEnd: e.target.value })} />
          </div>
          <div className="field">
            <label>Project / category</label>
            <select disabled={!editable} value={form.projectId ?? ''}
              onChange={(e) => setForm({ ...form, projectId: e.target.value ? Number(e.target.value) : undefined })}>
              <option value="">— None —</option>
              {projects.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
            </select>
          </div>
        </div>
      </div>

      <div className="panel">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
          <strong>Tasks completed</strong>
          {editable && <button className="btn btn-sm" onClick={addTask}>+ Add task</button>}
        </div>

        <table className="data-table">
          <thead>
            <tr>
              <th>Task</th>
              <th>Type</th>
              <th>Priority</th>
              <th>Status</th>
              <th>Planned %</th>
              <th>Actual %</th>
              <th>Time planned (h)</th>
              <th>Time spent (h)</th>
              {editable && <th></th>}
            </tr>
          </thead>
          <tbody>
            {form.taskEntries.map((t, i) => (
              <tr key={i}>
                <td><input disabled={!editable} value={t.taskName} placeholder="Task name"
                  onChange={(e) => updateTask(i, { taskName: e.target.value })} /></td>
                <td>
                  <select disabled={!editable} value={t.taskType} onChange={(e) => updateTask(i, { taskType: e.target.value })}>
                    <option>Development</option>
                    <option>Testing</option>
                    <option>Meetings</option>
                    <option>Documentation</option>
                  </select>
                </td>
                <td>
                  <select disabled={!editable} value={t.priority} onChange={(e) => updateTask(i, { priority: e.target.value })}>
                    <option>Low</option>
                    <option>Medium</option>
                    <option>High</option>
                  </select>
                </td>
                <td>
                  <select disabled={!editable} value={t.status} onChange={(e) => updateTask(i, { status: e.target.value })}>
                    <option>Not started</option>
                    <option>In progress</option>
                    <option>Done</option>
                  </select>
                </td>
                <td><input disabled={!editable} type="number" min={0} max={100} value={t.plannedPct}
                  onChange={(e) => updateTask(i, { plannedPct: Number(e.target.value) })} style={{ width: 70 }} /></td>
                <td><input disabled={!editable} type="number" min={0} max={100} value={t.actualPct}
                  onChange={(e) => updateTask(i, { actualPct: Number(e.target.value) })} style={{ width: 70 }} /></td>
                <td><input disabled={!editable} type="number" step={0.5} value={t.timePlanned}
                  onChange={(e) => updateTask(i, { timePlanned: Number(e.target.value) })} style={{ width: 70 }} /></td>
                <td><input disabled={!editable} type="number" step={0.5} value={t.timeSpent}
                  onChange={(e) => updateTask(i, { timeSpent: Number(e.target.value) })} style={{ width: 70 }} /></td>
                {editable && (
                  <td>
                    <button className="btn btn-sm" onClick={() => removeTask(i)}>Remove</button>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="panel">
        <div className="field">
          <label>Blockers / challenges</label>
          <textarea disabled={!editable} value={form.keyBlocker}
            onChange={(e) => setForm({ ...form, keyBlocker: e.target.value })}
            placeholder="What's slowing you down this week? Flag the key one." />
        </div>
        <div className="field">
          <label>Achievements / highlights</label>
          <textarea disabled={!editable} value={form.keyAchievement}
            onChange={(e) => setForm({ ...form, keyAchievement: e.target.value })}
            placeholder="What are you proud of this week? Flag the key one." />
        </div>
        <div className="field">
          <label>Plan for next week</label>
          <textarea disabled={!editable} value={form.nextWeekPlan}
            onChange={(e) => setForm({ ...form, nextWeekPlan: e.target.value })} />
        </div>
        <div className="field">
          <label>Notes / links (optional)</label>
          <textarea disabled={!editable} value={form.notes}
            onChange={(e) => setForm({ ...form, notes: e.target.value })} />
        </div>
      </div>

      {editable && (
        <div style={{ display: 'flex', gap: 10 }}>
          <button className="btn" disabled={saving} onClick={handleSaveDraft}>Save draft</button>
          <button className="btn btn-primary" disabled={saving} onClick={handleSubmit}>
            {saving ? 'Submitting…' : 'Submit for review'}
          </button>
        </div>
      )}
    </AppShell>
  );
}