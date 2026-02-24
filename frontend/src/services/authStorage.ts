const ACCESS_TOKEN_KEY = 'accessToken'

export function saveAccessToken(token: string): void {
  // Сохраняем JWT после успешного логина.
  localStorage.setItem(ACCESS_TOKEN_KEY, token)
}

export function getAccessToken(): string | null {
  // Читаем JWT для добавления в Authorization заголовок.
  return localStorage.getItem(ACCESS_TOKEN_KEY)
}

export function clearAccessToken(): void {
  // Вызывается при logout.
  localStorage.removeItem(ACCESS_TOKEN_KEY)
}

export function isAuthenticated(): boolean {
  // Базовая клиентская проверка: токен существует или нет.
  // Важно: финальная проверка доступа всё равно делается на backend.
  return Boolean(getAccessToken())
}
