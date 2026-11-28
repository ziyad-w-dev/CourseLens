// CourseLens mark. `dark` = light tile on the navy side panel.
export function LogoMark({ size = 30, dark = false }) {
  return (
    <svg width={size} height={size} viewBox="0 0 32 32" fill="none" aria-hidden="true">
      <rect width="32" height="32" rx="9" fill={dark ? '#F5F4EF' : '#172033'} />
      <circle cx="13" cy="16" r="6.5" stroke={dark ? '#172033' : '#F5F4EF'} strokeWidth="2" />
      <circle cx="19" cy="16" r="6.5" stroke={dark ? '#C4552A' : '#E08A5E'} strokeWidth="2" />
    </svg>
  );
}
