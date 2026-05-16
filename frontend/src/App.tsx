import { Navigate, Route, Routes } from 'react-router-dom'
import MainLayout from './layouts/MainLayout'
import PublicLayout from './layouts/PublicLayout'
import ProtectedRoute from './components/ProtectedRoute'
import DashboardPage from './pages/DashboardPage'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import CreateCoursePage from './pages/CreateCoursePage'
import MyCoursesPage from './pages/MyCoursesPage'
import CourseConstructorPage from './pages/CourseConstructorPage'
import UserCoursePage from './pages/UserCoursePage'
import ModulePage from './pages/ModulePage'
import CatalogPage from './pages/CatalogPage'
import TaskPage from './pages/TaskPage'
import LessonPage from './pages/LessonPage'

function App() {
  return (
    <Routes>
      <Route element={<PublicLayout />}>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
      </Route>

      <Route element={<ProtectedRoute />}>
        <Route element={<MainLayout />}>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/create-course" element={<CreateCoursePage />} />
          <Route path="/my-courses" element={<MyCoursesPage />} />
          <Route path="/catalog" element={<CatalogPage />} />
          <Route path="/course/:courseId" element={<UserCoursePage />} />
          <Route path="/course/:courseId/edit" element={<CourseConstructorPage />} />
          <Route path="/module/:moduleId" element={<ModulePage />} />
          <Route path="/task/:taskId" element={<TaskPage />} />
          <Route path="/lesson/:lessonId" element={<LessonPage />} />
          
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  )
}

export default App