import { useCallback, useEffect, useRef, useState } from 'react';
import { getMyCoursesAsDoctor, uploadCourse } from '../api/endpoints';
import { getSession } from '../auth/session';
import { CheckIcon, FileIcon, FileUpIcon, FileXIcon, LogoutIcon, RefreshIcon, TimerIcon, UploadIcon } from '../components/Icons';
import { LogoMark } from '../components/Logo';
import { useLogout } from '../components/StudentHeader';
import headerStyles from '../components/Header.module.css';
import { programName } from '../lib/domain';
import { formatDate, formatFileSize, initials, plural } from '../lib/format';
import styles from './ProfDashboard.module.css';

const MAX_BYTES = 20 * 1024 * 1024;

// Visual only: there's no progress API, so the steps advance on a timer while the
// ~20s upload request runs, and wait on step 3 until the response comes back.
const STEP_LABELS = ['Reading the PDF…', 'Extracting topics…', 'Personalizing for 3 career tracks…', 'Done'];
const STEP_MS = 4000;

function ProfHeader() {
  const session = getSession();
  const logout = useLogout();
  return (
    <header className={headerStyles.header}>
      <div className={headerStyles.profBrand}>
        <LogoMark size={30} />
        <span className={headerStyles.brandName}>CourseLens</span>
        <span className={headerStyles.profChip}>PROFESSOR</span>
      </div>
      <div className={headerStyles.right}>
        <div className={headerStyles.user}>
          <div className={headerStyles.userText}>
            <span className={headerStyles.userName}>{session?.fullName}</span>
            <span className={headerStyles.userSub}>Department of {programName(session?.program)}</span>
          </div>
          <span className={headerStyles.profAvatar}>{initials(session?.fullName)}</span>
        </div>
        <span className={headerStyles.divider} />
        <button type="button" onClick={logout} className={headerStyles.logout}>
          <LogoutIcon size={16} color="#172033" />
          Log out
        </button>
      </div>
    </header>
  );
}

const isPdf = (file) => file && (file.type === 'application/pdf' || /\.pdf$/i.test(file.name));

export default function ProfDashboard() {
  // phase: idle | processing | done | busy | pdf
  const [phase, setPhase] = useState('idle');
  const [file, setFile] = useState(null);
  const [step, setStep] = useState(0);
  const [result, setResult] = useState(null);
  const [courses, setCourses] = useState(null);
  const [listError, setListError] = useState(null);
  const [dragging, setDragging] = useState(false);
  const inputRef = useRef(null);

  const loadCourses = useCallback(() => {
    getMyCoursesAsDoctor()
      .then((list) => setCourses([...list].sort((a, b) => (b.createdAt || '').localeCompare(a.createdAt || ''))))
      .catch((err) => err.status !== 401 && setListError(err.message));
  }, []);

  useEffect(loadCourses, [loadCourses]);

  // fake step progress while processing
  useEffect(() => {
    if (phase !== 'processing') return undefined;
    setStep(0);
    const timer = setInterval(() => setStep((s) => Math.min(s + 1, 2)), STEP_MS);
    return () => clearInterval(timer);
  }, [phase]);

  async function start(chosen) {
    if (!chosen) return;
    setFile(chosen);
    setResult(null);
    if (!isPdf(chosen) || chosen.size > MAX_BYTES || chosen.size === 0) {
      setPhase('pdf');
      return;
    }
    setPhase('processing');
    try {
      const course = await uploadCourse(chosen);
      setResult(course);
      setStep(3);
      setPhase('done');
      loadCourses();
    } catch (err) {
      if (err.status === 401) return; // redirected to login
      // 400 = the file itself is bad (not a PDF, no text, empty, too big); anything else = AI busy/failed
      setPhase(err.status === 400 ? 'pdf' : 'busy');
    }
  }

  const pickFile = () => inputRef.current?.click();

  const onDrop = (e) => {
    e.preventDefault();
    setDragging(false);
    start(e.dataTransfer.files?.[0]);
  };

  return (
    <div className={styles.page}>
      <ProfHeader />
      <main className={styles.main}>
        <input
          ref={inputRef}
          type="file"
          accept="application/pdf,.pdf"
          hidden
          onChange={(e) => {
            start(e.target.files?.[0]);
            e.target.value = '';
          }}
        />

        <div className={styles.intro}>
          <h1 className={styles.h1}>Upload a course specification</h1>
          <p className={styles.lead}>
            Upload once. CourseLens extracts every topic and personalizes it for all 3 career tracks — it takes about 20
            seconds.
          </p>
        </div>

        {phase === 'idle' && (
          <>
            <section
              className={`${styles.drop} ${dragging ? styles.dropActive : ''}`}
              onDragOver={(e) => {
                e.preventDefault();
                setDragging(true);
              }}
              onDragLeave={() => setDragging(false)}
              onDrop={onDrop}
            >
              <span className={styles.bigIcon}><FileUpIcon size={34} color="#172033" sw={1.8} /></span>
              <div className={styles.dropText}>
                <span className={styles.dropTitle}>Drag &amp; drop your course specification PDF</span>
                <span className={styles.dropHint}>PDF only · up to 20 MB · one course per file</span>
              </div>
              <div className={styles.or}>
                <span className={styles.orLine} />
                <span className={styles.orText}>or</span>
                <span className={styles.orLine} />
              </div>
              <button type="button" className={styles.btn} onClick={pickFile}>
                <UploadIcon size={18} color="#FFFFFF" />
                Choose PDF
              </button>
            </section>

            <div className={styles.steps}>
              {['We read the PDF', 'Extract every topic and its hours', 'Personalize for 3 career tracks', 'Students see it instantly'].map(
                (label, i) => (
                  <div key={label} className={styles.step}>
                    <span className={styles.stepNum}>{i + 1}</span>
                    {label}
                  </div>
                ),
              )}
            </div>
          </>
        )}

        {(phase === 'processing' || phase === 'done') && (
          <Processing file={file} step={phase === 'done' ? 3 : step} result={result} onAnother={() => setPhase('idle')} />
        )}

        {phase === 'busy' && (
          <section role="alert" className={styles.error}>
            <span className={styles.errorIcon}><TimerIcon size={34} color="#A4281B" sw={1.9} /></span>
            <div className={styles.errorText}>
              <h2 className={styles.errorTitle}>The AI service is busy</h2>
              <p className={styles.errorBody}>Nothing was saved. Try again in a minute.</p>
            </div>
            <button type="button" className={styles.errorBtn} onClick={() => start(file)}>
              <RefreshIcon size={18} color="#FFFFFF" />
              Try again
            </button>
          </section>
        )}

        {phase === 'pdf' && (
          <section role="alert" className={styles.error}>
            <span className={styles.errorIcon}><FileXIcon size={34} color="#A4281B" sw={1.9} /></span>
            <div className={styles.errorText}>
              <h2 className={styles.errorTitle}>This file isn't a readable PDF</h2>
              <p className={styles.errorBody}>Upload the course specification as a PDF.</p>
            </div>
            <button type="button" className={styles.errorBtn} onClick={pickFile}>
              <UploadIcon size={18} color="#FFFFFF" />
              Choose another PDF
            </button>
          </section>
        )}

        <hr className={styles.hr} />

        <div className={styles.listHead}>
          <h2 className={styles.h2}>My uploaded courses</h2>
          {courses && <span className={styles.count}>{plural(courses.length, 'course')}</span>}
        </div>

        {listError && <p className={styles.status} role="alert">{listError}</p>}

        {courses && (
          <div className={styles.grid}>
            {courses.map((c) => (
              <div key={c.id} className={styles.card}>
                <span className={styles.code}>{c.code}</span>
                <div className={styles.cardTitle}>{c.title}</div>
                <div className={styles.cardFoot}>Uploaded {formatDate(c.createdAt)}</div>
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}

function Processing({ file, step, result, onAnother }) {
  const isDone = step === 3;
  return (
    <section aria-live="polite" className={styles.proc}>
      <div className={styles.file}>
        <span className={styles.fileIcon}><FileIcon size={24} color="#B23E1C" sw={1.9} /></span>
        <div className={styles.fileText}>
          <span className={styles.fileName}>{file?.name}</span>
          <span className={styles.fileSize}>{file ? formatFileSize(file.size) : ''}</span>
        </div>
        <span className={styles.procStatus} style={{ color: isDone ? 'var(--data)' : 'var(--backend)' }}>
          {isDone ? 'Done' : STEP_LABELS[step]}
        </span>
      </div>

      {isDone ? (
        <div className={styles.doneBar} />
      ) : (
        <div role="progressbar" aria-label="Processing the course specification" className={styles.track}>
          <span className={styles.slider} />
        </div>
      )}

      <ol className={styles.procSteps}>
        {STEP_LABELS.map((label, i) => {
          const done = i < step || (i === 3 && isDone);
          const active = i === step && i !== 3;
          return (
            <li
              key={label}
              className={styles.procStep}
              style={{
                background: active ? '#ECEEF9' : done ? '#FFFFFF' : '#FAF9F5',
                borderColor: active ? '#C9D0F0' : '#E4E2DA',
              }}
            >
              {done && <span className={styles.stepDone}><CheckIcon size={15} color="#FFFFFF" /></span>}
              {active && <span className={styles.stepSpin} />}
              {!done && !active && <span className={styles.stepTodo}>{i + 1}</span>}
              <span
                className={styles.procLabel}
                style={{ color: active ? 'var(--backend)' : done ? 'var(--ink)' : 'var(--muted)' }}
              >
                {label}
              </span>
            </li>
          );
        })}
      </ol>

      {isDone && result && (
        <div className={styles.live}>
          <div className={styles.liveText}>
            <span className={styles.liveTitle}>{result.code} · {result.title} is live</span>
            <span className={styles.liveSub}>
              Personalized for all 3 career tracks. Students enrolled in the course can open it now.
            </span>
          </div>
          <button type="button" className={styles.liveBtn} onClick={onAnother}>Upload another</button>
        </div>
      )}
    </section>
  );
}
