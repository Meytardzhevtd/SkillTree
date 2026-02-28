// courseApi.ts
import axios from 'axios'
import { getToken } from './authStorage'

const api = axios.create({
  baseURL: 'http://localhost:8080/api/course-manager', // <- порт бэкенда
})

api.interceptors.request.use(config => {
  const token = getToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export const getCourses = async (userId: number) => {
  const res = await api.get(`/courses?userId=${userId}`)
  return res.data
}

export const createCourse = async (userId: number, name: string, description: string) => {
  const res = await api.post('/create-course', { userId, name, description })
  return res.data
}

export const createFullCourse = async (data: any) => {
  const res = await api.post('/create-full-course', data)
  return res.data
}

export const addModule = async (courseId: number, name: string) => {
  const res = await api.post('/add-module', { courseId, name })
  return res.data
}

export const addTask = async (moduleId: number, content: string) => {
  const res = await api.post('/add-task', { moduleId, content })
  return res.data
}

export const getCourseById = async (courseId: number) => {
  const res = await api.get(`/course/${courseId}`)
  return res.data
}