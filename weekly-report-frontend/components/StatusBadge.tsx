import { ReportStatus } from '@/lib/types';
import { STATUS_LABELS, STATUS_COLORS } from '@/lib/utils';

export default function StatusBadge({ status }: { status: ReportStatus | null | undefined }) {
  if (!status) {
    return (
      <span className="status">
        <span className="status-dot" style={{ background: '#CBD5E1' }} />
        Not started
      </span>
    );
  }

  return (
    <span className="status">
      <span className="status-dot" style={{ background: STATUS_COLORS[status] }} />
      {STATUS_LABELS[status]}
    </span>
  );
}
