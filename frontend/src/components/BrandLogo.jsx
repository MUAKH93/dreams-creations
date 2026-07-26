const LOGO_SRC = '/logo.png'

const VARIANTS = {
  sidebar: { className: 'brand-logo brand-logo--sidebar', width: 56, height: 56 },
  auth: { className: 'brand-logo brand-logo--auth', width: 120, height: 120 },
  print: { className: 'brand-logo brand-logo--print', width: 96, height: 96 },
  label: { className: 'brand-logo brand-logo--label', width: 40, height: 40 },
}

export default function BrandLogo({
  variant = 'auth',
  className = '',
  alt = 'Dreams Creations — Designing Your Dreams',
}) {
  const config = VARIANTS[variant] || VARIANTS.auth

  return (
    <img
      src={LOGO_SRC}
      alt={alt}
      className={`${config.className}${className ? ` ${className}` : ''}`}
      width={config.width}
      height={config.height}
      loading="lazy"
      decoding="async"
    />
  )
}

export function AuthBrandHeader({ subtitle }) {
  return (
    <div className="auth-brand-header">
      <BrandLogo variant="auth" />
      {subtitle && <p className="auth-brand-header__subtitle">{subtitle}</p>}
    </div>
  )
}
