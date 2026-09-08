export function formatDate(iso?: string) {
  if (!iso) return '—';
  const d = new Date(iso);
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

export function formatDateRange(start: string, end: string) {
  const s = new Date(start);
  const e = new Date(end);
  const months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
  return `${months[s.getMonth()]} ${s.getDate()} – ${months[e.getMonth()]} ${e.getDate()}, ${e.getFullYear()}`;
}

// Returns the Monday of the current week, and the Sunday, as ISO date strings
export function currentWeekRange(): { start: string; end: string } {
  const now = new Date();
  const day = now.getDay();
  const diffToMonday = day === 0 ? -6 : 1 - day;
  const monday = new Date(now);
  monday.setDate(now.getDate() + diffToMonday);
  const sunday = new Date(monday);
  sunday.setDate(monday.getDate() + 6);
  return { start: toISODate(monday), end: toISODate(sunday) };
}

export function toISODate(d: Date) {
  return d.toISOString().slice(0, 10);
}

export const STATUS_LABELS: Record<string, string> = {
  DRAFT: 'Draft',
  SUBMITTED: 'Submitted',
  NEEDS_CORRECTION: 'Needs correction',
  APPROVED: 'Approved',
};

export const STATUS_COLORS: Record<string, string> = {
  DRAFT: '#6B6963',
  SUBMITTED: '#1D4ED8',
  NEEDS_CORRECTION: '#B45309',
  APPROVED: '#15803D',
};
