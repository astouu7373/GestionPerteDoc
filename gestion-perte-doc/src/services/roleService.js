import api from './api';

export const roleService = {
  // Créer un rôle
  async creerRole(roleData) {
    const response = await api.post('/roles', roleData);
    return response.data;
  },

  // Lister tous les rôles
  async listerRoles() {
    const response = await api.get('/roles');
    return response.data;
  },

  // Trouver par ID
  async trouverParId(id) {
    const response = await api.get(`/roles/${id}`);
    return response.data;
  },

  // Trouver par libellé
  async trouverParLibelle(libelle) {
    const response = await api.get(`/roles/libelle/${libelle}`);
    return response.data;
  }
};