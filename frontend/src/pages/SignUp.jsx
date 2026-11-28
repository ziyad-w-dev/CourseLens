import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { register } from '../api/endpoints';
import { saveSession, homePathFor } from '../auth/session';
import { AlertIcon, ArrowRight, CheckIcon, ChevronDown, TrackIcon } from '../components/Icons';
import { PROGRAMS, TRACKS, TRACK_ORDER } from '../lib/domain';
import { SignUpAside } from './AuthAside';
import styles from './Auth.module.css';

export default function SignUp() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    fullName: '',
    email: '',
    password: '',
    program: 'SOFTWARE_ENGINEERING',
    track: 'BACKEND_DEVELOPER',
  });
  const [emailError, setEmailError] = useState(null);
  const [formError, setFormError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const set = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }));

  async function onSubmit(e) {
    e.preventDefault();
    setEmailError(null);
    setFormError(null);
    setSubmitting(true);
    try {
      const session = await register(form);
      saveSession(session);
      navigate(homePathFor(session), { replace: true });
    } catch (err) {
      // 404 = the email's domain isn't a registered university (screen 1c), 409 = email already used
      if (err.status === 404 || err.status === 409) setEmailError(err.message);
      else setFormError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  const selected = TRACKS[form.track];

  return (
    <div className={styles.page}>
      <SignUpAside />

      <form className={styles.main} onSubmit={onSubmit} noValidate={false}>
        <div className={styles.tabs}>
          <span className={styles.tabActive}>Sign up</span>
          <Link to="/login" className={styles.tab}>Log in</Link>
        </div>

        <div className={styles.heading}>
          <h2 className={styles.title}>Create your account</h2>
          <p className={styles.subtitle}>Takes a minute. Your career track shapes every course you open.</p>
        </div>

        <div className={styles.grid}>
          <div className={styles.field}>
            <label htmlFor="su-name" className={styles.label}>Full name</label>
            <input
              id="su-name"
              type="text"
              required
              autoComplete="name"
              value={form.fullName}
              onChange={set('fullName')}
              className={styles.input}
            />
          </div>

          <div className={styles.field}>
            <label htmlFor="su-email" className={styles.label}>University email</label>
            <input
              id="su-email"
              type="email"
              required
              autoComplete="email"
              placeholder="name@uoh.edu.sa"
              value={form.email}
              onChange={(e) => {
                set('email')(e);
                setEmailError(null);
              }}
              aria-invalid={emailError ? 'true' : undefined}
              aria-describedby={emailError ? 'su-email-error' : undefined}
              className={`${styles.input} ${emailError ? styles.inputError : ''}`}
            />
            {emailError && (
              <span id="su-email-error" role="alert" className={styles.errorText}>
                <AlertIcon size={15} color="#A4281B" />
                {emailError}
              </span>
            )}
          </div>

          <div className={styles.field}>
            <label htmlFor="su-pass" className={styles.label}>Password</label>
            <input
              id="su-pass"
              type="password"
              required
              minLength={8}
              autoComplete="new-password"
              value={form.password}
              onChange={set('password')}
              className={styles.input}
            />
            <span className={styles.hint}>At least 8 characters</span>
          </div>

          <div className={styles.field}>
            <label htmlFor="su-program" className={styles.label}>Program</label>
            <div className={styles.selectWrap}>
              <select
                id="su-program"
                value={form.program}
                onChange={set('program')}
                className={`${styles.input} ${styles.select}`}
              >
                {PROGRAMS.map((p) => (
                  <option key={p.id} value={p.id}>{p.name}</option>
                ))}
              </select>
              <span className={styles.selectIcon}><ChevronDown size={18} color="#586174" /></span>
            </div>
          </div>
        </div>

        <fieldset className={styles.fieldset}>
          <div className={styles.fieldsetHead}>
            <legend className={styles.legendLabel}>
              Career track <span className={styles.required}>*</span>
            </legend>
            <span className={styles.hint}>Pick the job you're aiming for — every topic is ranked for it</span>
          </div>
          <div className={styles.tracks}>
            {TRACK_ORDER.map((id) => {
              const t = TRACKS[id];
              const sel = form.track === id;
              return (
                <button
                  key={id}
                  type="button"
                  aria-pressed={sel}
                  onClick={() => setForm((f) => ({ ...f, track: id }))}
                  className={styles.trackCard}
                  style={sel ? { borderColor: t.color, background: t.tint } : undefined}
                >
                  <span className={styles.trackIcon} style={{ background: sel ? t.color : t.tint }}>
                    <TrackIcon track={id} size={20} color={sel ? '#FFFFFF' : t.color} />
                  </span>
                  <span className={styles.trackName} style={{ color: sel ? t.color : 'var(--ink)' }}>{t.name}</span>
                  <span className={styles.trackDesc}>{t.desc}</span>
                  {sel && (
                    <span className={styles.trackCheck} style={{ background: t.color }}>
                      <CheckIcon size={13} color="#FFFFFF" />
                    </span>
                  )}
                </button>
              );
            })}
          </div>
        </fieldset>

        <div className={styles.actions}>
          {formError && (
            <span role="alert" className={styles.errorText}>
              <AlertIcon size={15} color="#A4281B" />
              {formError}
            </span>
          )}
          <button type="submit" className={styles.submit} disabled={submitting}>
            Create account as {selected.name}
            <ArrowRight size={18} color="#FFFFFF" />
          </button>
          <div className={`${styles.switch} ${styles.switchCenter}`}>
            Already have an account? <Link to="/login">Log in</Link>
          </div>
        </div>
      </form>
    </div>
  );
}
