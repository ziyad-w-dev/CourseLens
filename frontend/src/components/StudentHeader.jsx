import { Link, useNavigate } from 'react-router-dom';
import { clearSession, getSession } from '../auth/session';
import { programName, trackOf } from '../lib/domain';
import { initials } from '../lib/format';
import { LogoMark } from './Logo';
import { LogoutIcon, TrackIcon } from './Icons';
import styles from './Header.module.css';

export function useLogout() {
  const navigate = useNavigate();
  return () => {
    clearSession();
    navigate('/login');
  };
}

export function TrackPill({ track, short = false }) {
  const t = trackOf(track);
  return (
    <span
      className={short ? styles.trackPillSm : styles.trackPill}
      style={{ background: t.tint, color: t.color }}
    >
      <span className={styles.trackDot} style={{ background: t.color }}>
        <TrackIcon track={track} size={short ? 12 : 14} color="#FFFFFF" sw={short ? 2.4 : 2.2} />
      </span>
      {short ? t.short : t.name}
    </span>
  );
}

// Desktop header for all student pages (My courses, Course, Topic)
export default function StudentHeader({ current = false }) {
  const session = getSession();
  const logout = useLogout();
  return (
    <header className={styles.header}>
      <div className={styles.left}>
        <Link to="/courses" className={styles.brand}>
          <LogoMark size={30} />
          <span className={styles.brandName}>CourseLens</span>
        </Link>
        <nav className={styles.nav}>
          <Link to="/courses" aria-current={current ? 'page' : undefined} className={styles.navLink}>
            My courses
          </Link>
        </nav>
      </div>
      <div className={styles.right}>
        <TrackPill track={session?.track} />
        <div className={styles.user}>
          <div className={styles.userText}>
            <span className={styles.userName}>{session?.fullName}</span>
            <span className={styles.userSub}>{programName(session?.program)}</span>
          </div>
          <span className={styles.avatar}>{initials(session?.fullName)}</span>
        </div>
        <span className={styles.divider} />
        <button type="button" onClick={logout} className={styles.logout}>
          <LogoutIcon size={16} color="#172033" />
          Log out
        </button>
      </div>
    </header>
  );
}
