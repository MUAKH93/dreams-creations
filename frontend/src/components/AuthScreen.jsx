/**
 * Shared wrapper for login, register, forgot-password, and reset-password screens.
 */
export default function AuthScreen({ children, maxWidth = 420 }) {
  return (
    <div className="auth-screen">
      <div className="auth-screen__inner" style={{ maxWidth }}>
        {children}
      </div>
    </div>
  )
}
