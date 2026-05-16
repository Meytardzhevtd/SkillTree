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

export type ProfileResponse = {
  id: number;
  username: string;
  email: string;
  role: string;
  totalScore: number;
};
export async function getMyProfile(): Promise<ProfileResponse> {
  const res = await api.get('/profile/me');
  return res.data;
}

export async function updateMyProfileUsername(username: string): Promise<ProfileResponse> {
  const res = await api.put('/profile/me', { username });
  return res.data;
}

export async function getMyAvatar(): Promise<string> {
  const res = await api.get('/avatar/me');
  return res.data;
}

export async function uploadAvatar(file: File): Promise<string> {
  const formData = new FormData();
  formData.append('file', file);
  const res = await api.post('/avatar/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
  return res.data;
}