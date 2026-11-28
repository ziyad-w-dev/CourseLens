// The logged-in user as returned by POST /api/auth/login|register:
// { token, role, track, fullName, program }
const KEY = 'courselens.session';

export function getSession() {
  try {
    const raw = localStorage.getItem(KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function saveSession(authResponse) {
  localStorage.setItem(KEY, JSON.stringify(authResponse));
}

export function clearSession() {
  localStorage.removeItem(KEY);
}

export function homePathFor(session) {
  if (!session) return '/login';
  return session.role === 'DOCTOR' ? '/professor' : '/courses';
}
