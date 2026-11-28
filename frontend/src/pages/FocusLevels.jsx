import { Link } from 'react-router-dom';
import FocusBadge from '../components/FocusBadge';
import { TrackIcon } from '../components/Icons';
import { LogoMark } from '../components/Logo';
import { TRACKS, TRACK_ORDER } from '../lib/domain';
import styles from './FocusLevels.module.css';

// "How focus levels work" (linked from the empty My courses screen) = the Foundations board
const TRACK_TEXT = {
  BACKEND_DEVELOPER: 'APIs, databases and the server logic behind apps',
  FRONTEND_DEVELOPER: 'The interfaces people see, click and use',
  DATA_ANALYST: 'Turning raw data into decisions and dashboards',
};

const LEVEL_TEXT = [
  { level: 'MASTER_IT', title: 'Core for your track — study deeply', note: 'Solid fill, three bars. Always listed first.' },
  { level: 'APPLY_IT', title: "You'll use it in practice", note: 'Soft tint, two bars.' },
  { level: 'KNOW_IT', title: "Understand the concept, don't over-invest", note: 'Outline only, one bar.' },
];

export default function FocusLevels() {
  return (
    <div className={styles.page}>
      <div className={styles.top}>
        <div className={styles.titleCol}>
          <Link to="/" className={styles.brand}>
            <LogoMark size={36} />
            <span className={styles.brandName}>CourseLens</span>
          </Link>
          <h1 className={styles.h1}>Foundations</h1>
        </div>
        <p className={styles.lead}>
          Two systems carry the product. Tracks are identity — each has its own color and icon. Focus levels are
          priority — they get heavier as they matter more, so they read even in grayscale.
        </p>
      </div>

      <section className={styles.section}>
        <h2 className={styles.h2}>CAREER TRACKS</h2>
        <div className={styles.grid}>
          {TRACK_ORDER.map((id) => {
            const t = TRACKS[id];
            return (
              <div key={id} className={styles.card}>
                <div className={styles.trackIcon} style={{ background: t.tint }}>
                  <TrackIcon track={id} size={24} color={t.color} />
                </div>
                <div className={styles.trackName} style={{ color: t.color }}>{t.name}</div>
                <div className={styles.text}>{TRACK_TEXT[id]}</div>
              </div>
            );
          })}
        </div>
      </section>

      <section className={styles.section}>
        <h2 className={styles.h2}>FOCUS LEVELS</h2>
        <div className={styles.grid}>
          {LEVEL_TEXT.map(({ level, title, note }) => (
            <div key={level} className={`${styles.card} ${styles.levelCard}`}>
              <FocusBadge level={level} size="lg" />
              <div className={styles.levelTitle}>{title}</div>
              <div className={styles.text}>{note}</div>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}
