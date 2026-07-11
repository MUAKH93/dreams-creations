import { createContext, useContext, useState } from 'react'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(() => {
    const token = localStorage.getItem('token')
    const user  = localStorage.getItem('user')
    return token ? { token, ...JSON.parse(user) } : null
  })

  const login = (data) => {
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify({
      username:     data.username,
      role:         data.role,
      userId:       data.userId,
      customerId:   data.customerId ?? null,
      supervisorId: data.supervisorId ?? null,
    }))
    setAuth(data)
  }

  const logout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    setAuth(null)
  }

  return (
    <AuthContext.Provider value={{ auth, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
