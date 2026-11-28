import { LEVELS } from '../lib/domain';
import styles from './FocusBadge.module.css';

// Bars: MASTER = 3 filled, APPLY = 2, KNOW = 1 (the rest dimmed)
const FILLED = { MASTER_IT: 3, APPLY_IT: 2, KNOW_IT: 1 };
const KIND = { MASTER_IT: styles.master, APPLY_IT: styles.apply, KNOW_IT: styles.know };

/**
 * size: 'xs' (mobile topic rows) | 'sm' (26px) | 'md' (28px) | 'lg' (40px, foundations)
 *       | 'xl' (52px, topic page) | 'xlm' (46px, topic page mobile)
 */
export default function FocusBadge({ level, size = 'md' }) {
  const filled = FILLED[level] ?? 1;
  return (
    <span className={`${styles.badge} ${styles[size]} ${KIND[level] || styles.know}`}>
      <span className={styles.bars} aria-hidden="true">
        {[0, 1, 2].map((i) => (
          <span key={i} className={i < filled ? styles.on : styles.off} />
        ))}
      </span>
      {LEVELS[level]?.label}
    </span>
  );
}
