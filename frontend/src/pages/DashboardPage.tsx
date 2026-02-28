import { useEffect, useState } from 'react'
import type { ProfileResponse } from '../services/profileApi'
import { getMyProfile, updateMyProfileUsername } from '../services/profileApi'
import { getUser } from '../services/authStorage'
import { createCourse, getCourses } from '../services/courseApi'

function DashboardPage() {
  const [profile, setProfile] = useState<ProfileResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [usernameDraft, setUsernameDraft] = useState('')
  const [saving, setSaving] = useState(false)
  const [saveMessage, setSaveMessage] = useState('')
  const [courses, setCourses] = useState<any[]>([])
  const [courseName, setCourseName] = useState('')
  const [courseDesc, setCourseDesc] = useState('')

  useEffect(() => {
    const loadProfile = async () => {
      try {
        const data = await getMyProfile()
        setProfile(data)
        setUsernameDraft(data.username)

        // загружаем курсы
        const coursesData = await getCourses(data.id)
        setCourses(coursesData)
      } catch (err) {
        if (err instanceof Error) setError(err.message)
        else setError('Ошибка загрузки профиля')
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
      if (err instanceof Error) setSaveMessage(err.message)
      else setSaveMessage('Ошибка обновления профиля')
    } finally {
      setSaving(false)
    }
  }

  const handleCreateCourse = async () => {
    if (!courseName.trim()) return
    if (!profile) return

    try {
      await createCourse(profile.id, courseName, courseDesc)
      const updatedCourses = await getCourses(profile.id)
      setCourses(updatedCourses)
      setCourseName('')
      setCourseDesc('')
    } catch (err) {
      if (err instanceof Error) setError(err.message)
      else setError('Ошибка создания курса')
    }
  }

  if (loading) return <p>Загрузка...</p>
  if (!profile) return <p>Необходима авторизация</p>

  return (
    <div style={{ maxWidth: '640px', margin: '40px auto', padding: '16px' }}>
      <h1>Личный кабинет</h1>
      {error && <p style={{ color: 'crimson' }}>{error}</p>}

      <div style={{ lineHeight: 1.8 }}>
        <p><strong>ID:</strong> {profile.id}</p>
        <p><strong>Username:</strong> {profile.username}</p>
        <p><strong>Email:</strong> {profile.email}</p>
        <p><strong>Role:</strong> {profile.role}</p>

        <div style={{ marginTop: '16px' }}>
          <label><strong>Изменить username</strong></label>
          <br />
          <input
            type="text"
            value={usernameDraft}
            onChange={(e) => setUsernameDraft(e.target.value)}
          />
          <button onClick={handleSaveUsername} disabled={saving} style={{ marginLeft: '8px' }}>
            {saving ? 'Сохранение...' : 'Сохранить'}
          </button>
          {saveMessage && <p style={{ marginTop: '8px' }}>{saveMessage}</p>}
        </div>

        {/* Создание нового курса */}
        <div style={{ marginTop: '24px' }}>
          <h2>Создать новый курс</h2>
          <input
            placeholder="Название курса"
            value={courseName}
            onChange={(e) => setCourseName(e.target.value)}
          />
          <br />
          <input
            placeholder="Описание курса"
            value={courseDesc}
            onChange={(e) => setCourseDesc(e.target.value)}
          />
          <br />
          <button onClick={handleCreateCourse} style={{ marginTop: '8px' }}>Создать курс</button>
        </div>

        {/* Список курсов */}
        <div style={{ marginTop: '24px' }}>
          <h2>Мои курсы</h2>
          {courses.map(c => (
            <div key={c.id} style={{ border: '1px solid #ccc', margin: '4px 0', padding: '4px' }}>
              <strong>{c.name}</strong>: {c.description}
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}

export default DashboardPage