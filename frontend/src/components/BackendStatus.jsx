import { useEffect, useState } from 'react'
import { Alert } from 'antd'
import client from '../api/client'

/**
 * Shows a banner when the Spring Boot backend is unreachable.
 * Helps distinguish "empty table" from "cannot connect".
 */
export default function BackendStatus() {
  const [status, setStatus] = useState('checking') // checking | up | down

  useEffect(() => {
    let cancelled = false
    client.get('/health', { timeout: 5000 })
      .then(() => { if (!cancelled) setStatus('up') })
      .catch(() => { if (!cancelled) setStatus('down') })
    return () => { cancelled = true }
  }, [])

  if (status !== 'down') return null

  return (
    <Alert
      type="error"
      showIcon
      style={{ marginBottom: 16 }}
      message="Backend is not running"
      description="Start Spring Boot in the dreams-creations folder: .\mvnw.cmd spring-boot:run — then refresh this page."
    />
  )
}
