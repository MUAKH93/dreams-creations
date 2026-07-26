import { createContext, useContext, useState } from 'react'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(() => {
    const token = localStorage.getItem('token')
    const user  = localStorage.getItem('user')
    return token ? { token, ...JSON.parse(user) } : null
  })

  const login = (data) => {
    const userPayload = {
      username:     data.username,
      role:         data.role,
      userId:       data.userId,
      customerId:   data.customerId ?? null,
      supervisorId: data.supervisorId ?? null,
      profilePhotoUrl: data.profilePhotoUrl ?? null,
    }
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(userPayload))
    setAuth({ token: data.token, ...userPayload })
  }

  const updateProfilePhoto = (profilePhotoUrl) => {
    setAuth(prev => {
      if (!prev) return prev
      const updated = { ...prev, profilePhotoUrl }
      localStorage.setItem('user', JSON.stringify({
        username: updated.username,
        role: updated.role,
        userId: updated.userId,
        customerId: updated.customerId,
        supervisorId: updated.supervisorId,
        profilePhotoUrl: updated.profilePhotoUrl,
      }))
      return updated
    })
  }

  const logout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    setAuth(null)
  }

  return (
    <AuthContext.Provider value={{ auth, login, logout, updateProfilePhoto }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
