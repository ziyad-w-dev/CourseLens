import { useEffect } from 'react';
import { Navigate, Route, Routes, useNavigate } from 'react-router-dom';
import { setSessionExpiredHandler } from './api/client';
import { getSession, homePathFor } from './auth/session';
import RequireRole from './components/RequireRole';
import SignUp from './pages/SignUp';
import Login from './pages/Login';
import MyCourses from './pages/MyCourses';
import CourseDetail from './pages/CourseDetail';
import TopicDetail from './pages/TopicDetail';
import ProfDashboard from './pages/ProfDashboard';
import FocusLevels from './pages/FocusLevels';

function Home() {
  const session = getSession();
  return <Navigate to={session?.token ? homePathFor(session) : '/signup'} replace />;
}

export default function App() {
  const navigate = useNavigate();

  useEffect(() => {
    setSessionExpiredHandler(() => navigate('/login?expired=1', { replace: true }));
  }, [navigate]);

  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/signup" element={<SignUp />} />
      <Route path="/login" element={<Login />} />
      <Route path="/focus-levels" element={<FocusLevels />} />

      <Route path="/courses" element={<RequireRole role="STUDENT"><MyCourses /></RequireRole>} />
      <Route path="/courses/:courseId" element={<RequireRole role="STUDENT"><CourseDetail /></RequireRole>} />
      <Route
        path="/courses/:courseId/topics/:topicId"
        element={<RequireRole role="STUDENT"><TopicDetail /></RequireRole>}
      />

      <Route path="/professor" element={<RequireRole role="DOCTOR"><ProfDashboard /></RequireRole>} />

      <Route path="*" element={<Home />} />
    </Routes>
  );
}
