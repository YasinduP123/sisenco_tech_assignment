'use client';

import { useEffect, useState } from 'react';
import AppShell from '@/components/AppShell';
import { getAllProjects, createProject, updateProject, deleteProject } from '@/lib/api';
import type { ProjectDto } from '@/lib/types';

export default function ProjectsPage() {
  const [projects, setProjects] = useState<ProjectDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [showForm, setShowForm] = useState(false);
  const [editing, setEditing] = useState<ProjectDto | null>(null);
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');

  async function load() {
    setLoading(true);
    try {
      setProjects(await getAllProjects());
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load projects.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, []);

  function openCreate() {
    setEditing(null);
    setName('');
    setDescription('');
    setShowForm(true);
  }

  function openEdit(p: ProjectDto) {
    setEditing(p);
    setName(p.name);
    setDescription(p.description);
    setShowForm(true);
  }

  async function handleSave() {
    try {
      if (editing) {
        await updateProject(editing.id, { name, description });
      } else {
        await createProject({ name, description });
      }
      setShowForm(false);
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save project.');
    }
  }

  async function handleDelete(id: number) {
    if (!confirm('Delete this project?')) return;
    try {
      await deleteProject(id);
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete project.');
    }
  }

  return (
    <AppShell>
      <div className="page-heading">
        <div>
          <h1>Projects & categories</h1>
          <p>Manage the projects team members can attach to their reports.</p>
        </div>
        <button className="btn btn-primary" onClick={openCreate}>+ Add project</button>
      </div>

      {error && <p className="error-text">{error}</p>}

      {showForm && (
        <div className="panel" style={{ marginBottom: 16 }}>
          <div className="field">
            <label>Name</label>
            <input value={name} onChange={(e) => setName(e.target.value)} />
          </div>
          <div className="field">
            <label>Description</label>
            <textarea value={description} onChange={(e) => setDescription(e.target.value)} />
          </div>
          <div style={{ display: 'flex', gap: 10 }}>
            <button className="btn btn-primary" onClick={handleSave}>Save</button>
            <button className="btn" onClick={() => setShowForm(false)}>Cancel</button>
          </div>
        </div>
      )}

      <div className="panel">
        {loading ? (
          <p className="help-text">Loading…</p>
        ) : projects.length === 0 ? (
          <div className="empty-state">
            <strong>No projects yet</strong>
            <p>Add your first project to organize reports.</p>
          </div>
        ) : (
          <table className="data-table">
            <thead><tr><th>Name</th><th>Description</th><th>Status</th><th></th></tr></thead>
            <tbody>
              {projects.map((p) => (
                <tr key={p.id}>
                  <td>{p.name}</td>
                  <td>{p.description}</td>
                  <td>{p.active ? 'Active' : 'Inactive'}</td>
                  <td style={{ textAlign: 'right', display: 'flex', gap: 6, justifyContent: 'flex-end' }}>
                    <button className="btn btn-sm" onClick={() => openEdit(p)}>Edit</button>
                    <button className="btn btn-sm btn-danger" onClick={() => handleDelete(p.id)}>Delete</button>
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
