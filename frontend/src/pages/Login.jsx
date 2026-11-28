import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { login } from '../api/endpoints';
import { homePathFor, saveSession } from '../auth/session';
import { AlertIcon, ClockIcon } from '../components/Icons';
import { LoginAside } from './AuthAside';
import styles from './Auth.module.css';

export default function Login() {
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const sessionExpired = params.get('expired') === '1';

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  async function onSubmit(e) {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const session = await login({ email, password });
      saveSession(session);
      navigate(homePathFor(session), { replace: true });
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className={styles.page}>
      <LoginAside />

      <main className={`${styles.main} ${styles.loginMain}`}>
        <form className={styles.loginCol} onSubmit={onSubmit}>
          <div className={styles.tabs}>
            <Link to="/signup" className={styles.tab}>Sign up</Link>
            <span className={styles.tabActive}>Log in</span>
          </div>

          <div className={styles.heading}>
            <h2 className={styles.title}>Welcome back</h2>
            <p className={styles.subtitle}>Log in with your university email.</p>
          </div>

          {sessionExpired && (
            <div role="status" className={styles.expired}>
              <ClockIcon size={20} color="#8A3310" />
              <span className={styles.expiredText}>Your session expired — please log in again</span>
            </div>
          )}

          <div className={styles.field}>
            <label htmlFor="li-email" className={styles.label}>University email</label>
            <input
              id="li-email"
              type="email"
              required
              autoComplete="email"
              placeholder="name@uoh.edu.sa"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className={styles.input}
            />
          </div>

          <div className={styles.field}>
            <label htmlFor="li-pass" className={styles.label}>Password</label>
            <input
              id="li-pass"
              type="password"
              required
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className={styles.input}
            />
            {error && (
              <span role="alert" className={styles.errorText}>
                <AlertIcon size={15} color="#A4281B" />
                {error}
              </span>
            )}
          </div>

          <button type="submit" className={styles.submit} disabled={submitting}>Log in</button>

          <div className={styles.switch}>
            New to CourseLens? <Link to="/signup">Create an account</Link>
          </div>
        </form>
      </main>
    </div>
  );
}
