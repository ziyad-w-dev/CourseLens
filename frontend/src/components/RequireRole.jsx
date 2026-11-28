import { Navigate } from 'react-router-dom';
import { getSession, homePathFor } from '../auth/session';

// Route guard: no session -> login, wrong role -> that user's own home page
export default function RequireRole({ role, children }) {
  const session = getSession();
  if (!session?.token) return <Navigate to="/login" replace />;
  if (session.role !== role) return <Navigate to={homePathFor(session)} replace />;
  return children;
}
