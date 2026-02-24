export type RegisterPayload = {
  username: string
  email: string
  password: string
}

export type LoginPayload = {
  email: string
  password: string
}

async function postAuth(path: string, payload: RegisterPayload | LoginPayload): Promise<string> {
  try {
    const response = await fetch(path, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    })

    const message = await response.text()

    if (!response.ok) {
      throw new Error(message || 'Ошибка запроса к серверу')
    }

    return message
  } catch (error) {
    if (error instanceof Error) {
      throw error
    }

    throw new Error('Не удалось отправить запрос. Проверь, запущен ли backend на :8080')
  }
}

export async function registerUser(payload: RegisterPayload): Promise<string> {
  return postAuth('/api/auth/register', payload)
}

export async function loginUser(payload: LoginPayload): Promise<string> {
  return postAuth('/api/auth/login', payload)
}
