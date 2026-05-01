import { useEffect, useState } from 'react'
import {
  getMyProfile,
  updateMyProfileUsername,
  getMyAvatar,
  uploadAvatar,
  type ProfileResponse,
} from '../services/profileApi'

function DashboardPage() {
  const [profile, setProfile] = useState<ProfileResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [usernameDraft, setUsernameDraft] = useState('')
  const [saving, setSaving] = useState(false)
  const [saveMessage, setSaveMessage] = useState('')

  const [avatarUrl, setAvatarUrl] = useState<string>('')
  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const [uploading, setUploading] = useState(false)

  useEffect(() => {
    const loadProfile = async () => {
      try {
        const data = await getMyProfile()
        setProfile(data)
        setUsernameDraft(data.username)
      } catch (err) {
        if (err instanceof Error) setError(err.message)
        else setError('Ошибка загрузки профиля')
      } finally {
        setLoading(false)
      }
    }
    void loadProfile()
  }, [])

  useEffect(() => {
    const loadAvatar = async () => {
      try {
        const url = await getMyAvatar()
        if (url && url !== 'No avatar') {
          setAvatarUrl(url)
        }
      } catch (err) {
        console.error('Ошибка загрузки аватарки:', err)
      }
    }
    void loadAvatar()
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
      if (err instanceof Error) setSaveMessage(err.message)
      else setSaveMessage('Ошибка обновления профиля')
    } finally {
      setSaving(false)
    }
  }

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setSelectedFile(e.target.files[0])
    }
  }

  const handleUploadAvatar = async () => {
    if (!selectedFile) {
      setSaveMessage('Выберите файл')
      return
    }
    setUploading(true)
    setSaveMessage('')
    try {
      await uploadAvatar(selectedFile)
      window.location.reload()
    } catch (err: any) {
      setSaveMessage(err.message || 'Ошибка загрузки')
      setUploading(false)
    } finally {
      setUploading(false)
    }
  }

  return (
    <div style={{ maxWidth: '800px', margin: '40px auto', padding: '16px' }}>
      <h1>Личный кабинет</h1>

      {loading && <p>Загрузка профиля...</p>}
      {error && <p style={{ color: 'crimson' }}>{error}</p>}

      {profile && (
        <>
          <div style={{ textAlign: 'center', marginBottom: '30px' }}>
            {avatarUrl ? (
              <img
                src={avatarUrl}
                alt="Avatar"
                style={{
                  width: '170px',
                  height: '170px',
                  borderRadius: '50%',
                  objectFit: 'cover',
                  border: '3px solid #007bff'
                }}
              />
            ) : (
              <div
                style={{
                  width: '120px',
                  height: '120px',
                  borderRadius: '50%',
                  background: '#6c757d',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: 'white',
                  fontSize: '48px',
                  margin: '0 auto'
                }}
              >
                {profile.username.charAt(0).toUpperCase()}
              </div>
            )}

            <div style={{ marginTop: '12px' }}>
              <input
                type="file"
                accept="image/jpeg,image/png,image/gif"
                onChange={handleFileChange}
                style={{ marginRight: '8px' }}
              />
              <button onClick={handleUploadAvatar} disabled={uploading || !selectedFile}>
                {uploading ? 'Загрузка...' : 'Загрузить аватарку'}
              </button>
            </div>
          </div>

          <div style={{ lineHeight: 1.8, marginBottom: '24px' }}>
            <p><strong>ID:</strong> {profile.id}</p>
            <p><strong>Username:</strong> {profile.username}</p>
            <p><strong>Email:</strong> {profile.email}</p>
            <p><strong>Role:</strong> {profile.role}</p>
            <p>🎯 Всего баллов: {profile.totalScore}</p>

            <div style={{ marginTop: '16px' }}>
              <label htmlFor="username-edit"><strong>Изменить username</strong></label>
              <br />
              <input
                id="username-edit"
                type="text"
                value={usernameDraft}
                onChange={e => setUsernameDraft(e.target.value)}
              />
              <button
                type="button"
                onClick={handleSaveUsername}
                disabled={saving}
                style={{ marginLeft: '8px' }}
              >
                {saving ? 'Сохранение...' : 'Сохранить'}
              </button>
              {saveMessage && (
                <p style={{ marginTop: '8px', color: saveMessage.includes('ошибка') || saveMessage.includes('Ошибка') ? 'red' : 'green' }}>
                  {saveMessage}
                </p>
              )}
            </div>
          </div>
        </>
      )}
    </div>
  )
}

export default DashboardPage
