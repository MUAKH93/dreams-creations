import { useEffect } from 'react'
import client from '../api/client'
import { useAuth } from '../context/AuthContext'

/**
 * On app load, verify the stored JWT is still valid via public health endpoint.
 */
export default function SessionCheck() {
  const { auth, logout } = useAuth()

  useEffect(() => {
    if (!auth?.token) return
    client.get('/health', { timeout: 8000 })
      .catch((err) => {
        const status = err.response?.status
        if (status === 401) {
          logout()
          window.location.href = '/login'
        }
      })
  }, []) // eslint-disable-line react-hooks/exhaustive-deps

  return null
}
