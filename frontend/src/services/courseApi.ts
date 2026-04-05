// src/services/courseApi.ts
import axios from 'axios';
import { getToken } from './authStorage';

const api = axios.create({
  baseURL: '/api',
});

api.interceptors.request.use(config => {
  const token = getToken();
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export const getMyCreatedCourses = async () => {
  const res = await api.get('/user/my-courses');
  return res.data;
};

export const getMyEnrolledCourses = async () => {
  const res = await api.get('/user/courses');
  return res.data;
};

export const getMyCourses = async () => {
  const res = await api.get('/user/courses');
  return res.data;
};

export const createCourse = async (name: string, description: string) => {
  const res = await api.post('/course', { name, description });
  return res.data;
};

export const createModule = async (courseId: number, name: string, canBeOpen: boolean = false) => {
  const res = await api.post('/module', { courseId, name, can_be_open: canBeOpen });
  return res.data;
};

export const createTask = async (moduleId: number, taskTypeId: number, content: any) => {
  const res = await api.post('/tasks', { moduleId, taskTypeId, content });
  return res.data;
};

export const getCourseById = async (courseId: number) => {
  const res = await api.get(`/course/${courseId}`);
  return res.data;
};

export const getModulesByCourseId = async (courseId: number) => {
  const res = await api.get(`/module/courses/${courseId}`);
  return res.data;
};

export const getTasksByModuleId = async (moduleId: number) => {
  const res = await api.get(`/tasks?moduleId=${moduleId}`);
  return res.data;
};