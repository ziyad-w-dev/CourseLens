import FocusBadge from '../components/FocusBadge';
import { TrackIcon } from '../components/Icons';
import { LogoMark } from '../components/Logo';
import { TRACKS } from '../lib/domain';
import styles from './Auth.module.css';

function Shell({ title, text, children }) {
  return (
    <aside className={styles.aside}>
      <div className={styles.asideBrand}>
        <LogoMark size={32} dark />
        <span className={styles.asideBrandName}>CourseLens</span>
      </div>
      <div className={styles.asideBody}>
        <h1 className={styles.asideTitle}>{title}</h1>
        <p className={styles.asideText}>{text}</p>
        {children}
      </div>
      <div className={styles.asideFoot}>
        For software engineering and computer science students — and the professors who teach them.
      </div>
    </aside>
  );
}

// Static marketing example from the design ("Same topic. Three tracks.")
const EXAMPLE = [
  { track: 'DATA_ANALYST', level: 'MASTER_IT' },
  { track: 'BACKEND_DEVELOPER', level: 'APPLY_IT' },
  { track: 'FRONTEND_DEVELOPER', level: 'KNOW_IT' },
];

export function SignUpAside() {
  return (
    <Shell
      title="Every course, read through the lens of your career."
      text="Your professor uploads the course once. CourseLens tells you which topics to master, which to apply, and which you only need to know — for the job you actually want."
    >
      <div className={styles.example}>
        <div className={styles.exampleHead}>
          <span className={styles.exampleTitle}>Normalization for Relational Databases</span>
          <span className={styles.exampleCode}>SENG 351</span>
        </div>
        <div className={styles.exampleRows}>
          {EXAMPLE.map(({ track, level }) => (
            <div key={track} className={styles.exampleRow}>
              <span className={styles.exampleTrack} style={{ color: TRACKS[track].color }}>
                <TrackIcon track={track} size={18} color={TRACKS[track].color} />
                {TRACKS[track].name}
              </span>
              <FocusBadge level={level} size="sm" />
            </div>
          ))}
        </div>
        <div className={styles.exampleNote}>Same topic. Three tracks. Three different priorities.</div>
      </div>
    </Shell>
  );
}

const LEGEND = [
  { level: 'MASTER_IT', text: 'Core for your track — study deeply' },
  { level: 'APPLY_IT', text: "You'll use it in practice" },
  { level: 'KNOW_IT', text: "Understand the concept, don't over-invest" },
];

export function LoginAside() {
  return (
    <Shell
      title="Know what to study first. Stop guessing."
      text="Pick up where you left off — your courses are already ranked for your career track."
    >
      <div className={styles.legend}>
        {LEGEND.map(({ level, text }) => (
          <div key={level} className={styles.legendRow}>
            <FocusBadge level={level} size="sm" />
            <span className={styles.legendText}>{text}</span>
          </div>
        ))}
      </div>
    </Shell>
  );
}
