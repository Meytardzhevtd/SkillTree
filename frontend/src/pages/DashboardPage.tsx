import { useEffect, useState } from 'react'
import { getMyProfile, type ProfileResponse } from '../services/profileApi'

function DashboardPage() {
  // Состояние для профиля и UI-статусов.
  const [profile, setProfile] = useState<ProfileResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    // Загружаем профиль один раз при открытии страницы.
    const loadProfile = async () => {
      try {
        const data = await getMyProfile()
        setProfile(data)
      } catch (err) {
        if (err instanceof Error) {
          setError(err.message)
        } else {
          setError('Ошибка загрузки профиля')
        }
      } finally {
        setLoading(false)
      }
    }

    void loadProfile()
  }, [])

  return (
    <div style={{ maxWidth: '640px', margin: '40px auto', padding: '16px' }}>
      <h1>Личный кабинет</h1>

      {loading && <p>Загрузка профиля...</p>}
      {error && <p style={{ color: 'crimson' }}>{error}</p>}

      {profile && (
        <div style={{ lineHeight: 1.8 }}>
          <p>
            <strong>ID:</strong> {profile.id}
          </p>
          <p>
            <strong>Username:</strong> {profile.username}
          </p>
          <p>
            <strong>Email:</strong> {profile.email}
          </p>
          <p>
            <strong>Role:</strong> {profile.role}
          </p>
        </div>
      )}
    </div>
  )
}

export default DashboardPage
