import { getAccessToken } from './authStorage'

export type ProfileResponse = {
  id: number
  username: string
  email: string
  role: 'NONE' | 'BASE_USER' | 'ADMIN' | 'TOP_ADMIN'
}

export async function getMyProfile(): Promise<ProfileResponse> {
  const token = getAccessToken()

  // Если токена нет, запрос отправлять нет смысла.
  if (!token) {
    throw new Error('Вы не авторизованы. Выполните вход.')
  }

  const response = await fetch('/api/profile/me', {
    method: 'GET',
    headers: {
      // Ключевой момент JWT-потока: передаём токен в Bearer-заголовке.
      Authorization: `Bearer ${token}`,
    },
  })

  if (!response.ok) {
    const message = await response.text()
    throw new Error(message || 'Не удалось загрузить профиль')
  }

  return (await response.json()) as ProfileResponse
}
