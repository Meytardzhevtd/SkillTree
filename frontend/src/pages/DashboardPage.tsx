import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  getMyProfile,
  updateMyProfileUsername,
  type ProfileResponse,
} from '../services/profileApi'
import { getCourses } from '../services/courseApi'
import type { CourseDto, ModuleDto, TaskDto } from '../services/types';

function DashboardPage() {
  const [profile, setProfile] = useState<ProfileResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [usernameDraft, setUsernameDraft] = useState('')
  const [saving, setSaving] = useState(false)
  const [saveMessage, setSaveMessage] = useState('')
  const [courses, setCourses] = useState<CourseDto[]>([])
  const [coursesLoading, setCoursesLoading] = useState(true)

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
    if (!profile) return
    const loadCourses = async () => {
      try {
        const data = await getCourses(profile.id)
        setCourses(data)
      } catch (err) {
        console.error(err)
      } finally {
        setCoursesLoading(false)
      }
    }

    void loadCourses()
  }, [profile])

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

  return (
    <div style={{ maxWidth: '640px', margin: '40px auto', padding: '16px' }}>
      <h1>Личный кабинет</h1>

      {loading && <p>Загрузка профиля...</p>}
      {error && <p style={{ color: 'crimson' }}>{error}</p>}

      {profile && (
        <>
          <div style={{ lineHeight: 1.8, marginBottom: '24px' }}>
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
              {saveMessage && <p style={{ marginTop: '8px' }}>{saveMessage}</p>}
            </div>
          </div>

          <div style={{ marginBottom: '24px' }}>
            <Link to="/create-course">
              <button>Создать новый курс</button>
            </Link>
          </div>

          <h2>Мои курсы</h2>
          {coursesLoading && <p>Загрузка курсов...</p>}
          {!coursesLoading && courses.length === 0 && <p>Курсы отсутствуют</p>}
          {!coursesLoading && courses.length > 0 && (
            <ul>
              {courses.map(course => (
                <li key={course.id}>
                  <strong>{course.name}</strong> — {course.description}
                </li>
              ))}
            </ul>
          )}
        </>
      )}
    </div>
  )
}

export default DashboardPage