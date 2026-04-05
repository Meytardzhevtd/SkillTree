import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  getMyProfile,
  updateMyProfileUsername,
  type ProfileResponse,
} from '../services/profileApi'
import { getMyCourses, getModulesByCourseId, getTasksByModuleId } from '../services/courseApi'

function DashboardPage() {
  const [profile, setProfile] = useState<ProfileResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [usernameDraft, setUsernameDraft] = useState('')
  const [saving, setSaving] = useState(false)
  const [saveMessage, setSaveMessage] = useState('')
  const [courses, setCourses] = useState<any[]>([])
  const [coursesLoading, setCoursesLoading] = useState(true)
  const [expandedCourseId, setExpandedCourseId] = useState<number | null>(null)
  const [modulesCache, setModulesCache] = useState<Record<number, any[]>>({})
  const [tasksCache, setTasksCache] = useState<Record<number, any[]>>({})

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
    const loadCourses = async () => {
      try {
        const data = await getMyCourses()
        setCourses(data)
      } catch (err) {
        console.error(err)
      } finally {
        setCoursesLoading(false)
      }
    }
    void loadCourses()
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

  const toggleCourse = async (courseId: number) => {
    if (expandedCourseId === courseId) {
      setExpandedCourseId(null)
      return
    }

    setExpandedCourseId(courseId)

    if (!modulesCache[courseId]) {
      try {
        const modules = await getModulesByCourseId(courseId)
        setModulesCache(prev => ({ ...prev, [courseId]: modules }))

        for (const module of modules) {
          if (!tasksCache[module.moduleId]) {
            const tasks = await getTasksByModuleId(module.moduleId)
            setTasksCache(prev => ({ ...prev, [module.moduleId]: tasks }))
          }
        }
      } catch (err) {
        console.error(err)
      }
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
            <p><strong>ID:</strong> {profile.id}</p>
            <p><strong>Username:</strong> {profile.username}</p>
            <p><strong>Email:</strong> {profile.email}</p>
            <p><strong>Role:</strong> {profile.role}</p>

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
            <ul style={{ listStyle: 'none', padding: 0 }}>
              {courses.map((course: any) => (
                <li key={course.courseId} style={{ border: '1px solid #ccc', borderRadius: '6px', marginBottom: '8px', padding: '12px' }}>
                  <div
                    onClick={() => toggleCourse(course.courseId)}
                    style={{ cursor: 'pointer', display: 'flex', justifyContent: 'space-between' }}
                  >
                    <span><strong>{course.title}</strong> — {course.description}</span>
                    <span>{expandedCourseId === course.courseId ? '▲' : '▼'}</span>
                  </div>

                  {expandedCourseId === course.courseId && (
                    <div style={{ marginTop: '8px', paddingLeft: '12px' }}>
                      {!modulesCache[course.courseId] ? (
                        <p>Загрузка модулей...</p>
                      ) : modulesCache[course.courseId].length === 0 ? (
                        <p style={{ color: '#888' }}>Модули отсутствуют</p>
                      ) : (
                        modulesCache[course.courseId].map((mod: any) => (
                          <div key={mod.moduleId} style={{ marginBottom: '8px' }}>
                            <strong>📚 {mod.name}</strong> {!mod.isOpen && '🔒'}
                            {!tasksCache[mod.moduleId] ? (
                              <p style={{ marginLeft: '12px' }}>Загрузка задач...</p>
                            ) : tasksCache[mod.moduleId].length === 0 ? (
                              <p style={{ color: '#888', marginLeft: '12px' }}>Задания отсутствуют</p>
                            ) : (
                              <ul>
                                {tasksCache[mod.moduleId].map((task: any) => (
                                  <li key={task.taskId}>
                                    📝 Задача #{task.taskId} {task.isCompleted && '✅'}
                                  </li>
                                ))}
                              </ul>
                            )}
                          </div>
                        ))
                      )}
                    </div>
                  )}
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