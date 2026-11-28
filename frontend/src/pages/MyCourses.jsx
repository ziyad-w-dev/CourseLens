import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getMyCoursesAsStudent } from '../api/endpoints';
import { getSession } from '../auth/session';
import { ArrowRight, RefreshIcon } from '../components/Icons';
import StudentHeader from '../components/StudentHeader';
import { trackOf } from '../lib/domain';
import { firstName, greeting, plural } from '../lib/format';
import styles from './MyCourses.module.css';

function CourseCard({ course, track }) {
  const m = course.masterCount ?? 0;
  const a = course.applyCount ?? 0;
  const k = course.knowCount ?? 0;
  const n = m + a + k;
  const total = n || 1;
  // title of the first MASTER IT topic in syllabus order, or null when there is none
  const start = course.startWith;

  return (
    <article className={styles.card}>
      <div className={styles.cardHead}>
        <span className={styles.code}>{course.code}</span>
        <span className={styles.levelChip}>Level {course.level}</span>
      </div>
      <h2 className={styles.cardTitle}>{course.title}</h2>
      <div className={styles.mix}>
        <div role="img" aria-label={`${m} master it, ${a} apply it, ${k} know it topics`} className={styles.bar}>
          <span className={styles.barMaster} style={{ width: `${(m / total) * 100}%` }} />
          <span className={styles.barApply} style={{ width: `${(a / total) * 100}%` }} />
        </div>
        <span className={styles.mixText}>{m} Master · {a} Apply · {k} Know</span>
      </div>

      {start && (
        <div className={styles.callout} style={{ background: track.tint }}>
          <span className={styles.calloutLabel} style={{ color: track.color }}>START WITH</span>
          <span className={styles.calloutText}>{start}</span>
        </div>
      )}
      {/* a course with no topics yet shows neither box */}
      {m === 0 && n > 0 && (
        <div className={styles.callout} style={{ background: track.tint }}>
          <span className={styles.calloutLabel} style={{ color: track.color }}>GOOD NEWS</span>
          <span className={styles.calloutText}>Mostly KNOW IT for your track, so a lighter load</span>
        </div>
      )}

      <div className={styles.cardFoot}>
        <span className={styles.topicCount}>{plural(n, 'topic')}</span>
        <Link to={`/courses/${course.id}`} className={styles.open}>
          Open
          <ArrowRight size={16} color="#172033" />
        </Link>
      </div>
    </article>
  );
}

function EmptyIllustration() {
  return (
    <svg width="168" height="120" viewBox="0 0 168 120" fill="none" aria-hidden="true">
      <rect x="30" y="22" width="92" height="74" rx="12" fill="#FFFFFF" stroke="#D5D3CB" strokeWidth="1.5" transform="rotate(-8 76 59)" />
      <rect x="44" y="18" width="92" height="74" rx="12" fill="#FFFFFF" stroke="#C5CAD3" strokeWidth="1.5" />
      <rect x="56" y="32" width="36" height="8" rx="4" fill="#E9E7E0" />
      <rect x="56" y="48" width="64" height="6" rx="3" fill="#ECEEF9" />
      <rect x="56" y="60" width="48" height="6" rx="3" fill="#ECEEF9" />
      <circle cx="122" cy="84" r="20" fill="#172033" />
      <circle cx="117" cy="84" r="8" stroke="#F5F4EF" strokeWidth="2" />
      <circle cx="127" cy="84" r="8" stroke="#E08A5E" strokeWidth="2" />
    </svg>
  );
}

export default function MyCourses() {
  const session = getSession();
  const track = trackOf(session?.track);
  const name = firstName(session?.fullName);

  const [courses, setCourses] = useState(null);
  const [error, setError] = useState(null);
  const [level, setLevel] = useState(null);

  const load = useCallback(() => {
    setError(null);
    getMyCoursesAsStudent()
      .then(setCourses)
      .catch((err) => {
        if (err.status !== 401) setError(err.message);
      });
  }, []);

  useEffect(load, [load]);

  if (courses && courses.length === 0) {
    return (
      <div className={styles.page}>
        <StudentHeader current />
        <main className={`${styles.main} ${styles.mainEmpty}`}>
          <div className={styles.intro}>
            <h1 className={styles.h1}>Welcome, {name}</h1>
            <p className={styles.lead}>
              You're all set up as a <strong style={{ color: track.color }}>{track.name}</strong>.
            </p>
          </div>
          <div className={styles.empty}>
            <EmptyIllustration />
            <div className={styles.emptyText}>
              <h2 className={styles.emptyTitle}>No courses yet</h2>
              <p className={styles.emptyBody}>
                When your professor uploads a course specification, it shows up here — already ranked for your{' '}
                {track.name} track. Nothing to do until then.
              </p>
            </div>
            <div className={styles.emptyActions}>
              <button type="button" className={styles.btnDark} onClick={load}>
                <RefreshIcon size={16} color="#FFFFFF" />
                Check again
              </button>
              <Link to="/focus-levels" className={styles.btnLight}>How focus levels work</Link>
            </div>
          </div>
        </main>
      </div>
    );
  }

  const all = courses || [];
  const levels = [...new Set(all.map((c) => c.level))].sort((x, y) => x - y);
  const shown = all.filter((c) => level === null || c.level === level);
  const tabs = [{ label: `All · ${all.length}`, value: null }].concat(
    levels.map((l) => ({ label: `Level ${l} · ${all.filter((c) => c.level === l).length}`, value: l })),
  );

  return (
    <div className={styles.page}>
      <StudentHeader current />
      <main className={styles.main}>
        <div className={styles.top}>
          <div className={styles.intro}>
            <h1 className={styles.h1}>{greeting()}, {name}</h1>
            <p className={styles.lead}>
              {plural(all.length, 'course')}, each ranked for your{' '}
              <strong style={{ color: track.color }}>{track.name}</strong> track.
            </p>
          </div>
          <div aria-label="Bar colours" className={styles.key}>
            <span className={styles.keyItem}><span className={styles.swatch} style={{ background: 'var(--master)' }} />Master it</span>
            <span className={styles.keyItem}><span className={styles.swatch} style={{ background: 'var(--apply-bar)' }} />Apply it</span>
            <span className={styles.keyItem}><span className={styles.swatch} style={{ background: 'var(--know-bar)' }} />Know it</span>
          </div>
        </div>

        {error && <p className={styles.status} role="alert">{error}</p>}

        {courses && (
          <>
            <div role="group" aria-label="Filter by level" className={styles.tabs}>
              {tabs.map((tb) => (
                <button
                  key={tb.label}
                  type="button"
                  aria-pressed={level === tb.value}
                  onClick={() => setLevel(tb.value)}
                  className={`${styles.tab} ${level === tb.value ? styles.tabOn : ''}`}
                >
                  {tb.label}
                </button>
              ))}
            </div>

            <div className={styles.grid}>
              {shown.map((c) => (
                <CourseCard key={c.id} course={c} track={track} />
              ))}
            </div>
          </>
        )}
      </main>
    </div>
  );
}
