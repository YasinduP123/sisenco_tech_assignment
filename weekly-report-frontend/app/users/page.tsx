'use client';

import { useEffect, useState } from 'react';
import AppShell from '@/components/AppShell';
import { getAllUsers, updateUserRole, deleteUser } from '@/lib/api';
import type { UserDto, UserRole } from '@/lib/types';

export default function UsersPage() {
  const [users, setUsers] = useState<UserDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  async function load() {
    setLoading(true);
    try {
      setUsers(await getAllUsers());
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load users.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, []);

  async function handleRoleChange(id: number, role: UserRole) {
    try {
      await updateUserRole(id, role);
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to update role.');
    }
  }

  async function handleDelete(id: number) {
    if (!confirm('Remove this user?')) return;
    try {
      await deleteUser(id);
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to remove user.');
    }
  }

  return (
    <AppShell>
      <div className="page-heading">
        <div>
          <h1>User management</h1>
          <p>Team members are auto-added on their first sign-in via Keycloak.</p>
        </div>
      </div>

      {error && <p className="error-text">{error}</p>}

      <div className="panel">
        {loading ? (
          <p className="help-text">Loading…</p>
        ) : (
          <table className="data-table">
            <thead><tr><th>Name</th><th>Email</th><th>Role</th><th></th></tr></thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id}>
                  <td>{u.name}</td>
                  <td>{u.email}</td>
                  <td>
                    <select value={u.role} onChange={(e) => handleRoleChange(u.id, e.target.value as UserRole)}>
                      <option value="TEAM_MEMBER">Team member</option>
                      <option value="MANAGER">Manager</option>
                    </select>
                  </td>
                  <td style={{ textAlign: 'right' }}>
                    <button className="btn btn-sm btn-danger" onClick={() => handleDelete(u.id)}>Remove</button>
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
