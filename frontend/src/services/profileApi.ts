import { getToken } from './authStorage'

export type ProfileResponse = {
  id: number
  username: string
  email: string
  role: string
}

export async function getMyProfile(): Promise<ProfileResponse> {
  const token = getToken()
  if (!token) throw new Error('Необходима авторизация')

  const res = await fetch('/api/profile/me', {
    headers: { Authorization: `Bearer ${token}` },
  })
  if (!res.ok) {
    const msg = await res.text()
    throw new Error(msg || 'Ошибка запроса к серверу')
  }
  return res.json()
}

export async function updateMyProfileUsername(username: string): Promise<ProfileResponse> {
  const token = getToken()
  if (!token) throw new Error('Необходима авторизация')

  const res = await fetch('/api/profile/update', {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ username }),
  })
  if (!res.ok) {
    const msg = await res.text()
    throw new Error(msg || 'Ошибка обновления профиля')
  }
  return res.json()
}