// Display data for the backend enums (Track, Program, FocusLevel).

export const TRACKS = {
  BACKEND_DEVELOPER: {
    id: 'BACKEND_DEVELOPER',
    name: 'Backend Developer',
    short: 'Backend',
    job: 'BACKEND',
    desc: 'APIs, databases and server logic',
    color: 'var(--backend)',
    tint: 'var(--backend-tint)',
    // course page "personalized" card
    cardBorder: '#D5DAF2',
    cardTitle: '#1F2A6B',
    cardText: '#28305A',
  },
  FRONTEND_DEVELOPER: {
    id: 'FRONTEND_DEVELOPER',
    name: 'Frontend Developer',
    short: 'Frontend',
    job: 'FRONTEND',
    desc: 'Interfaces people see and use',
    color: 'var(--frontend)',
    tint: 'var(--frontend-tint)',
    cardBorder: '#C6E2EB',
    cardTitle: '#0B4558',
    cardText: '#1F4A57',
  },
  DATA_ANALYST: {
    id: 'DATA_ANALYST',
    name: 'Data Analyst',
    short: 'Data',
    job: 'DATA ANALYST',
    desc: 'Data into decisions and dashboards',
    color: 'var(--data)',
    tint: 'var(--data-tint)',
    cardBorder: 'var(--success-border)',
    cardTitle: 'var(--success-title)',
    cardText: 'var(--success-text)',
  },
};

export const TRACK_ORDER = ['BACKEND_DEVELOPER', 'FRONTEND_DEVELOPER', 'DATA_ANALYST'];

export const trackOf = (id) => TRACKS[id] || TRACKS.BACKEND_DEVELOPER;

export const PROGRAMS = [
  { id: 'SOFTWARE_ENGINEERING', name: 'Software Engineering' },
  { id: 'COMPUTER_SCIENCE', name: 'Computer Science' },
  { id: 'DATA_ANALYTICS', name: 'Data Analytics' },
  { id: 'COMPUTER_ENGINEERING', name: 'Computer Engineering' },
  { id: 'ARTIFICIAL_INTELLIGENCE', name: 'Artificial Intelligence' },
];

export const programName = (id) => PROGRAMS.find((p) => p.id === id)?.name || '';

export const LEVELS = {
  MASTER_IT: {
    id: 'MASTER_IT',
    label: 'MASTER IT',
    rank: 0,
    summary: 'Core for your track — study deeply',
    mobileHint: 'Study deeply',
    color: 'var(--master)',
  },
  APPLY_IT: {
    id: 'APPLY_IT',
    label: 'APPLY IT',
    rank: 1,
    summary: "You'll use it in practice",
    mobileHint: 'Use it in practice',
    color: 'var(--apply-fg)',
  },
  KNOW_IT: {
    id: 'KNOW_IT',
    label: 'KNOW IT',
    rank: 2,
    summary: "Understand the concept, don't over-invest",
    mobileHint: "Don't over-invest",
    color: 'var(--know-fg)',
  },
};

export const LEVEL_ORDER = ['MASTER_IT', 'APPLY_IT', 'KNOW_IT'];

// Study priority = MASTER first, then APPLY, then KNOW; syllabus order inside each level
export function byPriority(topics) {
  return [...topics].sort(
    (a, b) => LEVELS[a.focusLevel].rank - LEVELS[b.focusLevel].rank || a.syllabusNumber - b.syllabusNumber,
  );
}

// Topic-page sentence after the focus badge, e.g. "Core for your <Backend Developer> track — study deeply."
export const LEVEL_SENTENCE = {
  MASTER_IT: ['Core for your ', ' track — study deeply.'],
  APPLY_IT: ["You'll use it in practice on your ", ' track.'],
  KNOW_IT: ['Understand the concept for your ', " track — don't over-invest."],
};
