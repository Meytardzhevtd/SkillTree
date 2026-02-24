import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { isAuthenticated } from '../services/authStorage'

function ProtectedRoute() {
  const location = useLocation()

  // Если токена нет, отправляем на login.
  // state.from сохраняем для будущего улучшения UX (вернуться туда, куда хотел попасть пользователь).
  if (!isAuthenticated()) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  // Если токен есть — разрешаем рендер вложенных защищённых страниц.
  return <Outlet />
}

export default ProtectedRoute
