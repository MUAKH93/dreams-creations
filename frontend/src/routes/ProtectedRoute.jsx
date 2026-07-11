import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

/**
 * Wraps any route that requires authentication.
 * If not logged in → redirects to /login.
 * If logged in but wrong role → redirects to /unauthorized.
 *
 * Usage:
 *   <ProtectedRoute roles={['ADMIN', 'MANAGER']}>
 *     <BatchesPage />
 *   </ProtectedRoute>
 */
export default function ProtectedRoute({ children, roles }) {
  const { auth } = useAuth()

  if (!auth) {
    return <Navigate to="/login" replace />
  }

  if (roles && !roles.includes(auth.role)) {
    return <Navigate to="/unauthorized" replace />
  }

  return children
}
