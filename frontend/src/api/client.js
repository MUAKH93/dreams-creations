import axios from 'axios'

/**
 * Central Axios instance.
 * All API calls go through this — the interceptor automatically
 * attaches the JWT token from localStorage to every request.
 */
const client = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 15000,
})

/** Human-readable message for API failures */
export function apiErrorMessage(error) {
  if (!error.response) {
    return 'Cannot reach backend. Make sure Spring Boot is running on port 8080.'
  }
  const msg = error.response?.data?.message
  if (msg) return msg
  if (error.response.status === 403) return 'Access denied. Try logging in again.'
  if (error.response.status === 401) return 'Session expired. Please login again.'
  return `Server error (${error.response.status})`
}

// Request interceptor — attach token before every request
client.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    if (config.data instanceof FormData) {
      delete config.headers['Content-Type']
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor — expired/invalid token → redirect to login
client.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status
    const hasToken = !!localStorage.getItem('token')
    if (status === 401 || (status === 403 && hasToken)) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default client
