import { clearSession, getSession } from '../auth/session';

// The one place the backend URL lives
export const API_URL = (import.meta.env.VITE_API_URL || 'http://localhost:8080/api').replace(/\/$/, '');

// Error carrying the backend's ErrorResponse ({ status, message, timestamp })
export class ApiError extends Error {
  constructor(status, message) {
    super(message);
    this.status = status;
  }
}

let onSessionExpired = () => {
  window.location.assign('/login?expired=1');
};

// App registers a router-aware handler so we navigate without a full reload
export function setSessionExpiredHandler(handler) {
  onSessionExpired = handler;
}

export async function request(path, { method = 'GET', body, auth = true } = {}) {
  const headers = {};
  const session = auth ? getSession() : null;
  if (session?.token) headers.Authorization = `Bearer ${session.token}`;

  let payload = body;
  if (body && !(body instanceof FormData)) {
    headers['Content-Type'] = 'application/json';
    payload = JSON.stringify(body);
  }

  let res;
  try {
    res = await fetch(`${API_URL}${path}`, { method, headers, body: payload });
  } catch {
    throw new ApiError(0, "Can't reach the CourseLens server");
  }

  // 401 on an authenticated call = token missing/expired/invalid -> back to login.
  // (Login itself also returns 401 for wrong passwords; it's sent with auth:false so it's not caught here.)
  if (res.status === 401 && auth) {
    clearSession();
    onSessionExpired();
    throw new ApiError(401, 'Your session expired — please log in again');
  }

  const text = await res.text();
  const data = text ? safeJson(text) : null;

  if (!res.ok) {
    throw new ApiError(res.status, data?.message || 'Something went wrong, please try again');
  }
  return data;
}

function safeJson(text) {
  try {
    return JSON.parse(text);
  } catch {
    return null;
  }
}
