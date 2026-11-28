import { Link, useParams } from 'react-router-dom';
import { getSession } from '../auth/session';
import FocusBadge from '../components/FocusBadge';
import {
  ArrowLeft,
  ArrowRight,
  BookIcon,
  BulbIcon,
  DataIcon,
  FlaskIcon,
  LogoutIcon,
  SparkleIcon,
  TrackIcon,
} from '../components/Icons';
import StudentHeader, { useLogout } from '../components/StudentHeader';
import headerStyles from '../components/Header.module.css';
import { LEVELS, LEVEL_SENTENCE, byPriority, trackOf } from '../lib/domain';
import { plural } from '../lib/format';
import { useIsMobile } from '../lib/useMediaQuery';
import { useCourse } from './CourseDetail';
import styles from './TopicDetail.module.css';

// Everything the topic page shows is derived from GET /api/courses/{id}
function deriveTopic(course, topicId) {
  const topics = course.topics || [];
  const topic = topics.find((t) => String(t.id) === String(topicId));
  if (!topic) return null;

  const priority = byPriority(topics); // MASTER → APPLY → KNOW, syllabus order inside
  const sameLevel = priority.filter((t) => t.focusLevel === topic.focusLevel);
  const indexInLevel = sameLevel.findIndex((t) => t.id === topic.id);
  const priorityIndex = priority.findIndex((t) => t.id === topic.id);
  // Next = next topic in study-priority order (rolls into the next level after the last one)
  const next = priority[priorityIndex + 1] || null;

  return {
    topic,
    sameLevel,
    indexInLevel,
    priorityIndex,
    total: priority.length,
    next,
  };
}

const hours = (n) => plural(n, 'hour');

export default function TopicDetail() {
  const { courseId, topicId } = useParams();
  const { course, error } = useCourse(courseId);
  const isMobile = useIsMobile();
  const session = getSession();
  const track = trackOf(session?.track);

  const data = course ? deriveTopic(course, topicId) : null;
  const notFound = course && !data ? 'Topic not found' : null;
  const status = error || notFound;

  if (isMobile) {
    return (
      <div className={styles.page}>
        <MobileHeader course={course} courseId={courseId} trackId={session?.track} />
        <main className={styles.mMain}>
          {data ? (
            <MobileBody course={course} data={data} track={track} />
          ) : (
            <p className={styles.status} role={status ? 'alert' : undefined}>{status || ''}</p>
          )}
        </main>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <StudentHeader />
      <main className={styles.main}>
        <Link to={`/courses/${courseId}`} className={styles.back}>
          <ArrowLeft size={18} color="#172033" />
          Back to {course?.title || 'course'}
        </Link>
        {data ? (
          <DesktopBody course={course} data={data} track={track} />
        ) : (
          <p className={styles.status} role={status ? 'alert' : undefined}>{status || ''}</p>
        )}
      </main>
    </div>
  );
}

function LevelSentence({ level, track, className }) {
  const [before, after] = LEVEL_SENTENCE[level];
  return (
    <span className={className}>
      {before}
      <strong style={{ color: track.color }}>{track.name}</strong>
      {after}
    </span>
  );
}

function DesktopBody({ course, data, track }) {
  const { topic, sameLevel, indexInLevel, priorityIndex, total, next } = data;
  const level = LEVELS[topic.focusLevel];

  return (
    <div className={styles.layout}>
      <article className={styles.article}>
        <div className={styles.head}>
          <span className={styles.eyebrow}>
            {course.code} · {level.label} TOPIC {indexInLevel + 1} OF {sameLevel.length}
          </span>
          <h1 className={styles.h1}>{topic.title}</h1>
          <div className={styles.levelLine}>
            <FocusBadge level={topic.focusLevel} size="xl" />
            <LevelSentence level={topic.focusLevel} track={track} className={styles.levelText} />
          </div>
        </div>

        <div className={styles.stats}>
          <div className={styles.stat}>
            <span className={styles.statIcon}><BookIcon size={22} color="#172033" /></span>
            <div className={styles.statText}>
              <span className={styles.statNum}>{hours(topic.lectureHours)}</span>
              <span className={styles.statLabel}>Lecture</span>
            </div>
          </div>
          <div className={styles.stat}>
            <span className={styles.statIcon}><FlaskIcon size={22} color="#172033" /></span>
            <div className={styles.statText}>
              <span className={styles.statNum}>{hours(topic.labHours)}</span>
              <span className={styles.statLabel}>Lab</span>
            </div>
          </div>
          <div className={styles.stat}>
            <span className={styles.statIcon} style={{ background: track.tint }}>
              <DataIcon size={22} color={track.color} />
            </span>
            <div className={styles.statText}>
              <span className={styles.statNum}>{priorityIndex + 1} of {total}</span>
              <span className={styles.statLabel}>Your study priority</span>
            </div>
          </div>
        </div>

        {topic.focusReason && (
          <section className={styles.section}>
            <div className={styles.sectionHead}>
              <h2 className={styles.h2}>Why this matters for you</h2>
              <span className={styles.writtenFor} style={{ background: track.tint, color: track.color }}>
                <SparkleIcon size={14} color={track.color} />
                Written for {track.name}
              </span>
            </div>
            <p className={styles.body}>{topic.focusReason}</p>
          </section>
        )}

        {topic.realWorldExample && (
          <section className={styles.example}>
            <div className={styles.exampleHead}>
              <span className={styles.exampleIcon}><BulbIcon size={22} color="#FFFFFF" /></span>
              <h2 className={styles.h2}>Real-world example</h2>
            </div>
            <p className={styles.exampleBody}>{topic.realWorldExample}</p>
          </section>
        )}

        <div className={styles.aiLine}>
          <span className={styles.aiText}>
            <SparkleIcon size={16} color="#586174" />
            Generated by AI from the {course.code} course specification.
          </span>
        </div>

        {next && (
          <Link to={`/courses/${course.id}/topics/${next.id}`} className={styles.next}>
            <div className={styles.nextText}>
              <span className={styles.nextLabel}>NEXT IN {LEVELS[next.focusLevel].label}</span>
              <span className={styles.nextTitle}>{next.title}</span>
            </div>
            <ArrowRight size={22} color="#172033" />
          </Link>
        )}
      </article>

      <aside className={styles.aside}>
        <section className={styles.list}>
          <h2 className={styles.listTitle}>Your {level.label} topics</h2>
          <div className={styles.listItems}>
            {sameLevel.map((t, i) => {
              const current = t.id === topic.id;
              const inner = (
                <>
                  <span className={styles.dot} style={current ? CURRENT_DOT[topic.focusLevel] : undefined}>{i + 1}</span>
                  <span className={styles.itemTitle}>{t.title}</span>
                </>
              );
              return current ? (
                <div key={t.id} className={`${styles.item} ${styles.itemCurrent}`} aria-current="page">{inner}</div>
              ) : (
                <Link key={t.id} to={`/courses/${course.id}/topics/${t.id}`} className={styles.item}>{inner}</Link>
              );
            })}
          </div>
        </section>
      </aside>
    </div>
  );
}

// Filled number for the current topic in the sidebar, in the level's badge colours
const CURRENT_DOT = {
  MASTER_IT: { background: 'var(--master)', borderColor: 'var(--master)', color: '#FFFFFF' },
  APPLY_IT: { background: 'var(--apply-bg)', borderColor: 'var(--apply-border)', color: 'var(--apply-fg)' },
  KNOW_IT: { background: 'var(--know-fg)', borderColor: 'var(--know-fg)', color: '#FFFFFF' },
};

function MobileHeader({ course, courseId, trackId }) {
  const logout = useLogout();
  const track = trackOf(trackId);
  return (
    <header className={headerStyles.mHeader}>
      <Link
        to={`/courses/${courseId}`}
        aria-label={`Back to ${course?.title || 'course'}`}
        className={headerStyles.mBack}
      >
        <ArrowLeft size={22} color="#172033" />
      </Link>
      <span className={headerStyles.mCode}>{course?.code}</span>
      <div className={headerStyles.mRight}>
        <button type="button" onClick={logout} className={headerStyles.mLogout} style={{ padding: '0 6px' }}>
          <LogoutIcon size={16} color="#172033" />
          Log out
        </button>
        <span
          className={headerStyles.mTrackCircle}
          style={{ background: track.color }}
          aria-label={`${track.name} track`}
          role="img"
        >
          <TrackIcon track={track.id} size={15} color="#FFFFFF" sw={2.2} />
        </span>
      </div>
    </header>
  );
}

function MobileBody({ course, data, track }) {
  const { topic, sameLevel, indexInLevel, next } = data;
  const level = LEVELS[topic.focusLevel];

  return (
    <>
      <div className={styles.mHead}>
        <span className={styles.mEyebrow}>
          {course.code} · {level.label} TOPIC {indexInLevel + 1} OF {sameLevel.length}
        </span>
        <h1 className={styles.mH1}>{topic.title}</h1>
        <FocusBadge level={topic.focusLevel} size="xlm" />
        <LevelSentence level={topic.focusLevel} track={track} className={styles.mLevelText} />
      </div>

      <div className={styles.mStats}>
        <div className={styles.mStat}>
          <span className={styles.mStatNum}>{hours(topic.lectureHours)}</span>
          <span className={styles.mStatLabel}>Lecture</span>
        </div>
        <div className={styles.mStat}>
          <span className={styles.mStatNum}>{hours(topic.labHours)}</span>
          <span className={styles.mStatLabel}>Lab</span>
        </div>
      </div>

      {topic.focusReason && (
        <section className={styles.mSection}>
          <h2 className={styles.mH2}>Why this matters for you</h2>
          <p className={styles.mBody}>{topic.focusReason}</p>
        </section>
      )}

      {topic.realWorldExample && (
        <section className={styles.mExample}>
          <div className={styles.mExampleHead}>
            <span className={styles.mExampleIcon}><BulbIcon size={20} color="#FFFFFF" /></span>
            <h2 className={styles.mExampleTitle}>Real-world example</h2>
          </div>
          <p className={styles.mExampleBody}>{topic.realWorldExample}</p>
        </section>
      )}

      {next && (
        <Link to={`/courses/${course.id}/topics/${next.id}`} className={styles.mNext}>
          <div className={styles.mNextText}>
            <span className={styles.mNextLabel}>NEXT IN {LEVELS[next.focusLevel].label}</span>
            <span className={styles.mNextTitle}>{next.title}</span>
          </div>
          <ArrowRight size={20} color="#FFFFFF" />
        </Link>
      )}
    </>
  );
}
