const ACCESS_TOKEN_KEY = 'accessToken'
const USER_KEY = 'user'

export function saveAccessToken(token: string): void {
  localStorage.setItem(ACCESS_TOKEN_KEY, token)
}

export function getToken(): string | null {
  return localStorage.getItem(ACCESS_TOKEN_KEY)
}

export function clearAccessToken(): void {
  localStorage.removeItem(ACCESS_TOKEN_KEY)
}

export function isAuthenticated(): boolean {
  return Boolean(getToken())
}

export function saveUser(user: { id: number; username: string; email: string; role: string }): void {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function getUser(): { id: number; username: string; email: string; role: string } | null {
  const data = localStorage.getItem(USER_KEY)
  try {
    return data ? JSON.parse(data) : null
  } catch {
    localStorage.removeItem(USER_KEY)
    return null
  }
}

export function clearUser(): void {
  localStorage.removeItem(USER_KEY)
}

export function logout(): void {
  clearAccessToken()
  clearUser()
}