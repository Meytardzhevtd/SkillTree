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
        <strong>SkillTree</strong>
        <nav style={{ marginTop: '8px', display: 'flex', gap: '12px' }}>
          <Link to="/login">Вход</Link>
          <Link to="/register">Регистрация</Link>
          <Link to="/dashboard">Личный кабинет</Link>
          {authenticated && (
            <button type="button" onClick={handleLogout}>
              Выйти
            </button>
          )}
        </nav>
      </header>

      <main style={{ flex: 1 }}>
        <Outlet />
      </main>

      <footer style={{ borderTop: '1px solid #ddd', padding: '12px 16px' }}>
        © SkillTree
      </footer>
    </div>
  )
}

export default MainLayout
