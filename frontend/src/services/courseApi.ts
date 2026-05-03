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

export const deleteModule = async (moduleId: number) => {
  const res = await api.delete(`/module/${moduleId}`);
  return res.data;
};

export const createTask = async (moduleId: number, taskTypeId: number, content: any, score: number = 10) => {
  const res = await api.post('/tasks', { taskTypeId, moduleId, content, score });
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

export const getTasksByModuleId = async (moduleId: number, progressModuleId?: number) => {
  const url = progressModuleId
    ? `/tasks?moduleId=${moduleId}&progressModuleId=${progressModuleId}`
    : `/tasks?moduleId=${moduleId}`;
  const res = await api.get(url);
  return res.data;
};

export const getTaskById = async (taskId: number) => {
  const res = await api.get(`/tasks/${taskId}`);
  return res.data;
};

export const getModuleById = async (moduleId: number) => {
  const res = await api.get(`/module/${moduleId}`);
  return res.data;
};

export const getAllCourses = async () => {
  const res = await api.get('/course/all');
  return res.data;
};

export const enrollToCourse = async (courseId: number, userId: number, role: string = 'student') => {
  const res = await api.post('/take/course', { courseId, userId, role });
  return res.data;
};

export const getMyTakenCourses = async () => {
  const res = await api.get('/take/course/my');
  return res.data;
};

export const getMyCoursesByRole = async (role: string) => {
  const res = await api.get(`/course/my/${role}`);
  return res.data;
};

export const getMyRoleInCourse = async (courseId: number): Promise<string> => {
  const res = await api.get(`/course/${courseId}/my-role`);
  return res.data.role;
};

export const startModule = async (moduleId: number, takenCourseId: number) => {
  const res = await api.post(`/module/${moduleId}/start?takenCourseId=${takenCourseId}`);
  return res.data as { progressModuleId: number; progress: number };
};

export const submitAnswer = async (
  taskId: number,
  progressModuleId: number,
  answer: number | number[]
) => {
  const res = await api.post(`/tasks/${taskId}/submit`, { progressModuleId, answer });
  return res.data as {
    correct: boolean;
    alreadySolved: boolean;
    message: string;
    moduleProgress: number;
    tasks: { taskId: number; isCompleted: boolean }[];
  };
};


export const getLessonsByModuleId = async (moduleId: number) => {
  const res = await api.get(`/lessons/module/${moduleId}`);
  return res.data;
};

export const createLesson = async (moduleId: number, title: string, content: string) => {
  const res = await api.post('/lessons', { moduleId, title, content });
  return res.data;
};

export const deleteLesson = async (lessonId: number) => {
  const res = await api.delete(`/lessons/${lessonId}`);
  return res.data;
};

export const getLessonById = async (lessonId: number) => {
  const res = await api.get(`/lessons/${lessonId}`);
  return res.data;
};

export const getAllCourseDependencies = async (courseId: number) => {
  const res = await api.get(`/dependencies/graph/${courseId}`);
  return res.data;
};

export const createDependency = async (blockerId: number, dependentId: number) => {
  const res = await api.post(`/dependencies/${blockerId}/${dependentId}`);
  return res.data; // boolean
};

export const deleteDependency = async (dependencyId: number) => {
  const res = await api.delete(`/dependencies/${dependencyId}`);
  return res.data;
};

export const getStudentDependencyGraph = async (takenCourseId: number) => {
  const res = await api.get(`/dependencies/graph/takenCourse/${takenCourseId}`);
  return res.data;
};