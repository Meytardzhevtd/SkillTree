export type RegisterPayload = {
  username: string
  email: string
  password: string
}

export type LoginPayload = {
  email: string
  password: string
}

export type LoginResponse = {
  token: string
  tokenType: string
}

async function postAuthAsText(path: string, payload: RegisterPayload): Promise<string> {
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

async function postAuthAsJson<T>(path: string, payload: LoginPayload): Promise<T> {
  try {
    const response = await fetch(path, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    })

    if (!response.ok) {
      const message = await response.text()
      throw new Error(message || 'Ошибка запроса к серверу')
    }

    const data = (await response.json()) as T
    return data
  } catch (error) {
    if (error instanceof Error) {
      throw error
    }

    throw new Error('Не удалось отправить запрос. Проверь, запущен ли backend на :8080')
  }
}

export async function registerUser(payload: RegisterPayload): Promise<string> {
  return postAuthAsText('/api/auth/register', payload)
}

export async function loginUser(payload: LoginPayload): Promise<LoginResponse> {
  return postAuthAsJson<LoginResponse>('/api/auth/login', payload)
}
