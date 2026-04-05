import { Link, Outlet, useNavigate } from 'react-router-dom'
import { clearAccessToken, isAuthenticated } from '../services/authStorage'

function MainLayout() {
  const navigate = useNavigate()
  const authenticated = isAuthenticated()

  const handleLogout = () => {
    clearAccessToken()
    navigate('/login')
  }

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <header style={{ borderBottom: '1px solid #ddd', padding: '12px 16px' }}>
        <strong style={{ fontSize: '20px' }}>SkillTree</strong>
        <nav style={{ marginTop: '8px', display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
          <Link to="/dashboard">Личный кабинет</Link>
          <Link to="/my-courses">Мои курсы</Link>
          <Link to="/create-course">Создать курс</Link>
          {authenticated && (
            <button type="button" onClick={handleLogout} style={{ marginLeft: 'auto' }}>
              Выйти
            </button>
          )}
        </nav>
      </header>

      <main style={{ flex: 1 }}>
        <Outlet />
      </main>

      <footer style={{ borderTop: '1px solid #ddd', padding: '12px 16px', textAlign: 'center' }}>
        © SkillTree
      </footer>
    </div>
  )
}

export default MainLayout