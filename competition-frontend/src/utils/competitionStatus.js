export const STATUS_STYLE = {
  open:    { bg: '#f6ffed', color: '#52c41a' },
  urgent:  { bg: '#fff1f0', color: '#ff4d4f' },
  expired: { bg: '#f5f5f5', color: '#999' },
};

export function computeStatus(deadlineStr) {
  const now = new Date();
  now.setHours(0, 0, 0, 0);
  const deadline = new Date(deadlineStr);
  deadline.setHours(23, 59, 59, 999);
  const diff = (deadline - now) / (1000 * 60 * 60 * 24);

  if (diff < 0) return { label: '已截止', style: 'expired', isExpired: true };
  if (diff <= 7) return { label: '即将截止', style: 'urgent', isExpired: false };
  return { label: '报名中', style: 'open', isExpired: false };
}

export const CD_STYLES = {
  backBtn: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: 4,
    background: 'none',
    border: 'none',
    color: '#555',
    fontSize: 14,
    fontWeight: 500,
    cursor: 'pointer',
    padding: '8px 4px',
    marginTop: 20,
    marginBottom: 16,
    borderRadius: 6,
    transition: 'color 0.2s, background 0.2s',
  },
  infoCellContent: {
    display: 'flex',
    alignItems: 'center',
    gap: 10,
    padding: '10px 0',
  },
  postCard: {
    padding: '16px 20px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 12,
    transition: 'transform 0.2s ease, box-shadow 0.2s ease',
  },
  bottomBar: {
    position: 'fixed',
    bottom: 0,
    left: 0,
    right: 0,
    background: '#fff',
    borderTop: '1px solid #f0f0f0',
    padding: '12px 0',
    paddingBottom: 'max(12px, env(safe-area-inset-bottom))',
    boxShadow: '0 -2px 12px rgba(0,0,0,0.04)',
    zIndex: 50,
  },
  favBtn: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: 6,
    background: '#fafafa',
    border: '1px solid #e5e4e7',
    borderRadius: 10,
    padding: '10px 18px',
    cursor: 'pointer',
    color: '#555',
    transition: 'all 0.2s ease',
    flexShrink: 0,
  },
};
