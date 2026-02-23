import { useState } from 'react'
import type { FormEvent } from 'react'
import './App.css'

type Role = 'NONE' | 'BASE_USER' | 'ADMIN' | 'TOP_ADMIN'

type ProfileResponse = {
  id: number
  username: string
  email: string
  role: Role
}

function App() {
  const [apiStatus, setApiStatus] = useState('')
  const [registerMessage, setRegisterMessage] = useState('')
  const [profileEmail, setProfileEmail] = useState('')
  const [profile, setProfile] = useState<ProfileResponse | null>(null)
  const [error, setError] = useState('')

  const [registerForm, setRegisterForm] = useState({
    username: '',
    email: '',
    password: '',
  })

  const checkApi = async () => {
    setError('')
    try {
      const response = await fetch('/api/hello')
      const text = await response.text()
      setApiStatus(text)
    } catch {
      setError('Не удалось подключиться к backend API')
    }
  }

  const registerUser = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError('')
    setRegisterMessage('')
    try {
      const response = await fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(registerForm),
      })

      const text = await response.text()
      if (!response.ok) {
        setError(text)
        return
      }

      setRegisterMessage(text)
      setRegisterForm({ username: '', email: '', password: '' })
    } catch {
      setError('Ошибка при регистрации')
    }
  }

  const loadProfile = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError('')
    setProfile(null)
    try {
      const params = new URLSearchParams({ email: profileEmail })
      const response = await fetch(`/api/profile/me?${params.toString()}`)

      if (!response.ok) {
        const text = await response.text()
        setError(text)
        return
      }

      const data = (await response.json()) as ProfileResponse
      setProfile(data)
    } catch {
      setError('Ошибка при загрузке профиля')
    }
  }

  return (
    <main className="page">
      <h1>SkillTree — Личный кабинет (MVP)</h1>

      <section className="card">
        <h2>1) Проверка backend</h2>
        <button onClick={checkApi}>Проверить /api/hello</button>
        {apiStatus && <p className="ok">{apiStatus}</p>}
      </section>

      <section className="card">
        <h2>2) Регистрация</h2>
        <form onSubmit={registerUser}>
          <input
            placeholder="username"
            value={registerForm.username}
            onChange={(e) => setRegisterForm((prev) => ({ ...prev, username: e.target.value }))}
            required
          />
          <input
            placeholder="email"
            type="email"
            value={registerForm.email}
            onChange={(e) => setRegisterForm((prev) => ({ ...prev, email: e.target.value }))}
            required
          />
          <input
            placeholder="password"
            type="password"
            value={registerForm.password}
            onChange={(e) => setRegisterForm((prev) => ({ ...prev, password: e.target.value }))}
            required
          />
          <button type="submit">Создать пользователя</button>
        </form>
        {registerMessage && <p className="ok">{registerMessage}</p>}
      </section>

      <section className="card">
        <h2>3) Профиль</h2>
        <form onSubmit={loadProfile}>
          <input
            placeholder="email для поиска"
            type="email"
            value={profileEmail}
            onChange={(e) => setProfileEmail(e.target.value)}
            required
          />
          <button type="submit">Загрузить профиль</button>
        </form>

        {profile && (
          <div className="profile">
            <p>ID: {profile.id}</p>
            <p>Username: {profile.username}</p>
            <p>Email: {profile.email}</p>
            <p>Role: {profile.role}</p>
          </div>
        )}
      </section>

      {error && <p className="error">{error}</p>}
    </main>
  )
}

export default App
