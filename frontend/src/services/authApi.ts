import { saveAccessToken, saveUser } from './authStorage'

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
  user: {
    id: number
    username: string
    email: string
    role: string
  }
}

async function postAuthAsText(path: string, payload: RegisterPayload): Promise<string> {
  const res = await fetch(path, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  const text = await res.text()
  if (!res.ok) throw new Error(text || 'Ошибка запроса к серверу')
  return text
}

async function postAuthAsJson<T>(path: string, payload: LoginPayload): Promise<T> {
  const res = await fetch(path, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  if (!res.ok) {
    const msg = await res.text()
    throw new Error(msg || 'Ошибка запроса к серверу')
  }
  return res.json() as Promise<T>
}

export async function registerUser(payload: RegisterPayload): Promise<string> {
  return postAuthAsText('/api/auth/register', payload)
}

export async function loginUser(payload: LoginPayload): Promise<LoginResponse> {
  const data = await postAuthAsJson<LoginResponse>('/api/auth/login', payload)
  saveAccessToken(data.token)
  saveUser(data.user)
  return data
}