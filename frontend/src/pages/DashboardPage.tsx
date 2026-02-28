import { useEffect, useState } from 'react'
import {
  getMyProfile,
  updateMyProfileUsername,
  type ProfileResponse,
} from '../services/profileApi'

function DashboardPage() {
  const [profile, setProfile] = useState<ProfileResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [usernameDraft, setUsernameDraft] = useState('')
  const [saving, setSaving] = useState(false)
  const [saveMessage, setSaveMessage] = useState('')

  useEffect(() => {
    const loadProfile = async () => {
      try {
        const data = await getMyProfile()
        setProfile(data)
        setUsernameDraft(data.username)
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

  const handleSaveUsername = async () => {
    setSaveMessage('')

    if (!usernameDraft.trim()) {
      setSaveMessage('Имя пользователя не может быть пустым')
      return
    }

    try {
      setSaving(true)
      const updatedProfile = await updateMyProfileUsername(usernameDraft)
      setProfile(updatedProfile)
      setSaveMessage('Имя пользователя обновлено')
    } catch (err) {
      if (err instanceof Error) {
        setSaveMessage(err.message)
      } else {
        setSaveMessage('Ошибка обновления профиля')
      }
    } finally {
      setSaving(false)
    }
  }

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

          <div style={{ marginTop: '16px' }}>
            <label htmlFor="username-edit">
              <strong>Изменить username</strong>
            </label>
            <br />
            <input
              id="username-edit"
              type="text"
              value={usernameDraft}
              onChange={(event) => setUsernameDraft(event.target.value)}
            />
            <button type="button" onClick={handleSaveUsername} disabled={saving} style={{ marginLeft: '8px' }}>
              {saving ? 'Сохранение...' : 'Сохранить'}
            </button>
            {saveMessage && <p style={{ marginTop: '8px' }}>{saveMessage}</p>}
          </div>
        </div>
      )}
    </div>
  )
}

export default DashboardPage
