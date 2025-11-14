import api from './api';

export const authService = {
  async login(email, password) {
    const response = await api.post('/auth/login', { email, password });
    return response.data;
  },

  async register(userData) {
    const response = await api.post('/auth/register', userData);
    return response.data;
  },

  async getProfile() {
    const response = await api.get('/auth/profile');
    return response.data;
  },

  async refreshToken() {
    const response = await api.post('/auth/refresh');
    return response.data;
  },

  async testAuth() {
    const response = await api.get('/declarations/test-auth');
    return response.data;
  },
  async getTypesDocument() {
      const response = await api.get('/types-document');
      return response.data;
    }
};