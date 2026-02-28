import { getAccessToken } from './authStorage'

export type ProfileResponse = {
  id: number
  username: string
  email: string
  role: 'NONE' | 'BASE_USER' | 'ADMIN' | 'TOP_ADMIN'
}

export async function getMyProfile(): Promise<ProfileResponse> {
  const token = getAccessToken()

  if (!token) {
    throw new Error('Вы не авторизованы. Выполните вход.')
  }

  const response = await fetch('/api/profile/me', {
    method: 'GET',
    headers: {
      Authorization: `Bearer ${token}`,
    },
  })

  if (!response.ok) {
    const message = await response.text()
    throw new Error(message || 'Не удалось загрузить профиль')
  }

  return (await response.json()) as ProfileResponse
}

export async function updateMyProfileUsername(username: string): Promise<ProfileResponse> {
  const token = getAccessToken()

  if (!token) {
    throw new Error('Вы не авторизованы. Выполните вход.')
  }

  const response = await fetch('/api/profile/me', {
    method: 'PUT',
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ username }),
  })

  if (!response.ok) {
    const message = await response.text()
    throw new Error(message || 'Не удалось обновить профиль')
  }

  return (await response.json()) as ProfileResponse
}
