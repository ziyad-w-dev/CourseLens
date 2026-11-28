import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { getCourse } from '../api/endpoints';
import { getSession } from '../auth/session';
import FocusBadge from '../components/FocusBadge';
import {
  ArrowLeft,
  BookIcon,
  BulbIcon,
  CapIcon,
  ChevronRight,
  FlaskIcon,
  LogoutIcon,
  SparkleIcon,
  TrackIcon,
} from '../components/Icons';
import { LogoMark } from '../components/Logo';
import StudentHeader, { TrackPill, useLogout } from '../components/StudentHeader';
import headerStyles from '../components/Header.module.css';
import { LEVELS, LEVEL_ORDER, programName, trackOf } from '../lib/domain';
import { pad2, plural } from '../lib/format';
import { useIsMobile } from '../lib/useMediaQuery';
import styles from './CourseDetail.module.css';

const sum = (topics, key) => topics.reduce((acc, t) => acc + (t[key] || 0), 0);

// Topics grouped by focus level, syllabus order inside each group
function groupTopics(topics) {
  const sorted = [...topics].sort((a, b) => a.syllabusNumber - b.syllabusNumber);
  return LEVEL_ORDER.map((level) => ({ level, topics: sorted.filter((t) => t.focusLevel === level) })).filter(
    (g) => g.topics.length > 0,
  );
}

export function useCourse(courseId) {
  const [course, setCourse] = useState(null);
  const [error, setError] = useState(null);
  useEffect(() => {
    let alive = true;
    setCourse(null);
    setError(null);
    getCourse(courseId)
      .then((c) => alive && setCourse(c))
      .catch((err) => alive && err.status !== 401 && setError(err.message));
    return () => {
      alive = false;
    };
  }, [courseId]);
  return { course, error };
}

const TILE_CLASS = { MASTER_IT: styles.tileMaster, APPLY_IT: styles.tileApply, KNOW_IT: styles.tileKnow };
const COUNT_KEY = { MASTER_IT: 'masterCount', APPLY_IT: 'applyCount', KNOW_IT: 'knowCount' };

export default function CourseDetail() {
  const { courseId } = useParams();
  const { course, error } = useCourse(courseId);
  const isMobile = useIsMobile();
  const session = getSession();
  const track = trackOf(session?.track);

  const content = !course ? (
    <p className={styles.status} role={error ? 'alert' : undefined}>{error || ''}</p>
  ) : isMobile ? (
    <MobileBody course={course} track={track} />
  ) : (
    <DesktopBody course={course} track={track} />
  );

  return (
    <div className={styles.page}>
      {isMobile ? <MobileHeader track={session?.track} /> : <StudentHeader />}
      {isMobile ? (
        <main className={styles.mMain}>
          <Link to="/courses" className={styles.mBack}>
            <ArrowLeft size={16} color="#586174" />
            My courses
          </Link>
          {content}
        </main>
      ) : (
        <main className={styles.main}>
          <Link to="/courses" className={styles.back}>
            <ArrowLeft size={16} color="#586174" />
            My courses
          </Link>
          {content}
        </main>
      )}
    </div>
  );
}

function MobileHeader({ track }) {
  const logout = useLogout();
  return (
    <header className={headerStyles.mHeader}>
      <Link to="/courses" className={headerStyles.mBrand}>
        <LogoMark size={28} />
        <span className={headerStyles.mBrandName}>CourseLens</span>
      </Link>
      <div className={headerStyles.mRight}>
        <TrackPill track={track} short />
        <button type="button" onClick={logout} className={headerStyles.mLogout}>
          <LogoutIcon size={16} color="#172033" />
          Log out
        </button>
      </div>
    </header>
  );
}

function WhyCards({ course, track, mobile }) {
  const program = programName(course.program);
  const cards = [
    {
      key: 'program',
      show: !!course.description,
      icon: <CapIcon size={mobile ? 19 : 22} color="#172033" />,
      iconBg: 'var(--chip)',
      label: 'FOR EVERY STUDENT IN THE PROGRAM',
      title: `Why this course matters for you as a ${program} student`,
      text: course.description,
    },
    {
      key: 'track',
      show: !!course.focusReason,
      icon: <TrackIcon track={track.id} size={mobile ? 19 : 22} color="#FFFFFF" />,
      iconBg: track.color,
      label: `PERSONALIZED FOR ${track.name.toUpperCase()}`,
      labelColor: track.color,
      title: 'Why it matters for your track',
      titleColor: track.cardTitle,
      text: course.focusReason,
      textColor: track.cardText,
      style: { background: track.tint, borderColor: track.cardBorder },
    },
    {
      key: 'example',
      show: !!course.realWorldExample,
      icon: <BulbIcon size={mobile ? 19 : 22} color="#FFFFFF" />,
      iconBg: 'var(--master)',
      label: `IN A ${track.job} JOB`,
      labelColor: 'var(--apply-fg)',
      title: 'Real-world example',
      text: course.realWorldExample,
      textColor: 'var(--warm-text)',
      warm: true,
    },
  ].filter((c) => c.show);

  return cards.map((c) =>
    mobile ? (
      <section key={c.key} className={`${styles.mCard} ${c.warm ? styles.whyWarm : ''}`} style={c.style}>
        <div className={styles.mCardHead}>
          <span className={styles.mIcon} style={{ background: c.iconBg }}>{c.icon}</span>
          <span className={styles.mLabel} style={{ color: c.labelColor }}>{c.label}</span>
        </div>
        <h2 className={styles.mCardTitle} style={{ color: c.titleColor }}>{c.title}</h2>
        <p className={styles.mCardText} style={{ color: c.textColor }}>{c.text}</p>
      </section>
    ) : (
      <section key={c.key} className={`${styles.whyCard} ${c.warm ? styles.whyWarm : ''}`} style={c.style}>
        <span className={styles.whyIcon} style={{ background: c.iconBg }}>{c.icon}</span>
        <span className={styles.whyLabel} style={{ color: c.labelColor }}>{c.label}</span>
        <h2 className={styles.whyTitle} style={{ color: c.titleColor }}>{c.title}</h2>
        <p className={styles.whyText} style={{ color: c.textColor }}>{c.text}</p>
      </section>
    ),
  );
}

function groupSummary(level, topics) {
  const lec = sum(topics, 'lectureHours');
  const lab = sum(topics, 'labHours');
  let s = `${LEVELS[level].summary} · ${plural(topics.length, 'topic')} · ${lec} lecture hrs`;
  if (lab > 0) s += ` · ${lab} lab hrs`;
  return s;
}

function DesktopBody({ course, track }) {
  const topics = course.topics || [];
  const groups = groupTopics(topics);
  const lec = sum(topics, 'lectureHours');
  const lab = sum(topics, 'labHours');

  return (
    <>
      <div className={styles.titleBlock}>
        <span className={styles.code}>{course.code}</span>
        <h1 className={styles.h1}>{course.title}</h1>
        <p className={styles.meta}>
          {programName(course.program)} · Level {course.level} · {plural(topics.length, 'topic')} · {lec} lecture
          hours · {lab} lab hours
        </p>
      </div>

      <div className={styles.why}>
        <WhyCards course={course} track={track} />
      </div>

      <div className={styles.summary}>
        <span className={styles.aiNote}>
          <SparkleIcon size={15} color="#586174" />
          All three paragraphs are AI-generated from the course specification.
        </span>
        <div className={styles.tiles}>
          {LEVEL_ORDER.map((level) => (
            <div key={level} className={`${styles.tile} ${TILE_CLASS[level]}`}>
              <span className={styles.tileNum}>{course[COUNT_KEY[level]] ?? 0}</span>
              <span className={styles.tileLabel}>{LEVELS[level].label}</span>
            </div>
          ))}
        </div>
      </div>

      <div className={styles.topicsHead}>
        <h2 className={styles.topicsTitle}>Topics</h2>
        <span className={styles.topicsHint}>Grouped by focus level</span>
      </div>

      {groups.map(({ level, topics: list }) => (
        <section key={level} className={styles.group}>
          <div className={styles.groupHead}>
            <FocusBadge level={level} size="md" />
            <span className={styles.groupText}>{groupSummary(level, list)}</span>
          </div>
          {list.map((t) => (
            <Link
              key={t.id}
              to={`/courses/${course.id}/topics/${t.id}`}
              className={`${styles.row} ${level === 'KNOW_IT' ? styles.rowKnow : ''}`}
            >
              <span className={styles.num}>#{pad2(t.syllabusNumber)}</span>
              <span className={styles.rowTitle}>{t.title}</span>
              <span className={`${styles.hrs} ${styles.hrsLec}`}>
                <BookIcon size={16} color="#4F5869" />
                {t.lectureHours} hrs lecture
              </span>
              <span className={`${styles.hrs} ${styles.hrsLab}`}>
                <FlaskIcon size={16} color="#4F5869" />
                {t.labHours > 0 ? `${t.labHours} hrs lab` : 'No lab'}
              </span>
              <span className={styles.badgeCell}>
                <FocusBadge level={level} size="md" />
              </span>
              <ChevronRight size={18} color="#8A92A2" />
            </Link>
          ))}
        </section>
      ))}

      <p className={styles.footnote}>
        <SparkleIcon size={16} color="#586174" />
        Focus levels are generated by AI from the official course specification. Your professor's syllabus and exams
        remain the final word.
      </p>
    </>
  );
}

const M_TILE_STYLE = {
  MASTER_IT: { background: 'var(--master)', color: '#FFFFFF' },
  APPLY_IT: { background: 'var(--apply-bg)', border: '1px solid var(--apply-border)', color: 'var(--apply-fg)' },
  KNOW_IT: { background: '#FFFFFF', border: '1px solid var(--know-border)', color: 'var(--know-fg)' },
};

function MobileBody({ course, track }) {
  const topics = course.topics || [];
  const groups = groupTopics(topics);
  const lec = sum(topics, 'lectureHours');
  const lab = sum(topics, 'labHours');

  return (
    <>
      <div className={styles.mTitleBlock}>
        <span className={styles.mCode}>{course.code}</span>
        <h1 className={styles.mH1}>{course.title}</h1>
        <p className={styles.mMeta}>
          {programName(course.program)} · Level {course.level}
          <br />
          {plural(topics.length, 'topic')} · {lec} lecture hrs · {lab} lab hrs
        </p>
      </div>

      <WhyCards course={course} track={track} mobile />

      <span className={styles.mAiNote}>
        <SparkleIcon size={13} color="#586174" />
        AI-generated from the course specification
      </span>

      <section className={styles.mGroup}>
        <div className={styles.mTiles}>
          {LEVEL_ORDER.map((level) => (
            <div key={level} className={styles.mTile} style={M_TILE_STYLE[level]}>
              <span className={styles.mTileNum}>{course[COUNT_KEY[level]] ?? 0}</span>
              <span className={styles.mTileLabel}>{LEVELS[level].label}</span>
            </div>
          ))}
        </div>
      </section>

      {groups.map(({ level, topics: list }) => (
        <section key={level} className={styles.mGroup}>
          <div className={styles.mGroupHead}>
            <span className={styles.mGroupLabel} style={{ color: LEVELS[level].color }}>
              {LEVELS[level].label} · {list.length}
            </span>
            <span className={styles.mGroupHint}>{LEVELS[level].mobileHint}</span>
          </div>
          {list.map((t) => (
            <Link
              key={t.id}
              to={`/courses/${course.id}/topics/${t.id}`}
              className={`${styles.mRow} ${level === 'KNOW_IT' ? styles.mRowKnow : ''}`}
            >
              <span className={styles.mRowTitle}>{t.title}</span>
              <div className={styles.mRowFoot}>
                <FocusBadge level={level} size="xs" />
                <span className={styles.mRowHrs}>
                  {t.lectureHours}h lecture · {t.labHours > 0 ? `${t.labHours}h lab` : 'no lab'}
                </span>
              </div>
            </Link>
          ))}
        </section>
      ))}
    </>
  );
}
