import { AuthBrandHeader } from './BrandLogo'

/**
 * Shared wrapper for login, register, forgot-password, and reset-password screens.
 */
export default function AuthScreen({ children, maxWidth = 420, subtitle }) {
  return (
    <div className="auth-screen">
      <div className="auth-screen__inner" style={{ maxWidth }}>
        <AuthBrandHeader subtitle={subtitle} />
        {children}
      </div>
    </div>
  )
}
