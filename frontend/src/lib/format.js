const MONTHS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

// "2026-09-21T10:15:00" (LocalDateTime) -> "21 Sep 2026"
export function formatDate(isoLocal) {
  if (!isoLocal) return '';
  const [y, m, d] = isoLocal.slice(0, 10).split('-').map(Number);
  if (!y || !m || !d) return '';
  return `${d} ${MONTHS[m - 1]} ${y}`;
}

export const plural = (n, one, many = one + 's') => `${n} ${n === 1 ? one : many}`;

export const firstName = (fullName = '') => fullName.trim().split(/\s+/)[0] || '';

export function initials(fullName = '') {
  const parts = fullName
    .replace(/^(dr|prof)\.?\s+/i, '')
    .trim()
    .split(/\s+/)
    .filter(Boolean);
  if (parts.length === 0) return '';
  const first = parts[0][0];
  const last = parts.length > 1 ? parts[parts.length - 1][0] : '';
  return (first + last).toUpperCase();
}

export function greeting(date = new Date()) {
  const h = date.getHours();
  if (h < 12) return 'Good morning';
  if (h < 18) return 'Good afternoon';
  return 'Good evening';
}

export function formatFileSize(bytes) {
  if (bytes >= 1024 * 1024) return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  return `${Math.max(1, Math.round(bytes / 1024))} KB`;
}

// syllabusNumber 5 -> "05"
export const pad2 = (n) => String(n).padStart(2, '0');
