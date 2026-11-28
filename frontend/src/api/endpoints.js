import { request } from './client';

// ---- auth (AuthController) ----
export const register = (body) => request('/auth/register', { method: 'POST', body, auth: false });
export const login = (body) => request('/auth/login', { method: 'POST', body, auth: false });

// ---- courses (CourseController) ----
export const getMyCoursesAsStudent = () => request('/courses');
export const getCourse = (id) => request(`/courses/${id}`);
export const getMyCoursesAsDoctor = () => request('/courses/my');

export function uploadCourse(file) {
  const form = new FormData();
  form.append('file', file);
  return request('/courses/upload', { method: 'POST', body: form });
}
