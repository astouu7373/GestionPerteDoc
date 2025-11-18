import api from './api';

export const utilisateurService = {
  // ===================== CRÉATION =====================
  async creerUtilisateur(utilisateurData) {
    const { postePoliceId, roles, nom, prenom, email } = utilisateurData;

    if (!postePoliceId) throw new Error('Le poste de police est obligatoire');
    if (!roles || !roles.length) throw new Error('Au moins un rôle doit être sélectionné');

    const roleIds = roles.map(r => (typeof r === 'object' ? r.id : Number(r)));
    if (!roleIds.length) throw new Error('Au moins un rôle valide doit être sélectionné');

    const bodyData = { nom, prenom, email };
    const queryParams = new URLSearchParams();
    queryParams.append('postePoliceId', postePoliceId);
    roleIds.forEach(id => queryParams.append('roleIds', id));

    const response = await api.post(`/utilisateurs?${queryParams.toString()}`, bodyData);
    return response.data;
  },

  // ===================== ACTIVATION =====================
  async activerUtilisateur(id, roleIds = []) {
    if (!roleIds.length) throw new Error('Au moins un rôle doit être sélectionné pour activer un utilisateur');

    const payload = roleIds.map(r => (typeof r === 'object' ? r.id : Number(r)));
    const response = await api.post(`/utilisateurs/${id}/activer`, payload);
    return response.data;
  },

  // ===================== DÉSACTIVATION =====================
  async desactiverUtilisateur(id) {
    const response = await api.post(`/utilisateurs/${id}/desactiver`);
    return response.data;
  },

  // ===================== TRANSFERT ADMIN =====================
  async transfererRoleAdmin(ancienAdminId, nouveauAdminId) {
    const response = await api.post(`/utilisateurs/${ancienAdminId}/transferer-admin/${nouveauAdminId}`);
    return response.data;
  },

  // ===================== MISE À JOUR =====================
  async mettreAJourUtilisateur(id, utilisateurData) {
    const { postePoliceId, roles } = utilisateurData;
    if (!postePoliceId) throw new Error('Le poste de police est obligatoire');

    const payload = {
      ...utilisateurData,
      postePoliceId: Number(postePoliceId),
      roleIds: roles?.map(r => (typeof r === 'object' ? r.id : Number(r))) || []
    };

    const response = await api.put(`/utilisateurs/${id}`, payload);
    return response.data;
  },

  // ===================== SUPPRESSION (SOFT DELETE) =====================
  async supprimerUtilisateurSoft(id) {
    const response = await api.delete(`/utilisateurs/${id}`);
    return response.data;
  },
  async supprimerUtilisateurDefinitif(id) {
      const response = await api.delete(`/utilisateurs/${id}/definitif`);
      return response.data;
  },

  // ===================== LISTING =====================
  async listerUtilisateursActifs() {
    const response = await api.get('/utilisateurs/actifs');
    return response.data;
  },

  async listerUtilisateursInactifs() {
    const response = await api.get('/utilisateurs/inactifs');
    return response.data;
  },

  async listerTousUtilisateurs() {
    const response = await api.get('/utilisateurs');
    return response.data;
  },

  async listerPostesPolice() {
    const response = await api.get('/postes-police');
    return response.data;
  },

  async trouverParId(id) {
    const response = await api.get(`/utilisateurs/${id}`);
    return response.data;
  },

  async existeAdmin() {
    const response = await api.get('/utilisateurs/existe-admin');
    return response.data.existe;
  },

  // ===================== RÉINITIALISATION MOT DE PASSE =====================
  async resetMotDePasse(token, nouveauMotDePasse) {
    if (!token || !nouveauMotDePasse)
      throw new Error('Le token et le nouveau mot de passe sont requis');

    const response = await api.post('/utilisateurs/reset-password', {
      resetToken: token,
      nouveauMotDePasse
    });

    return response.data;
  },

  async forgotMotDePasse(email) {
    if (!email) throw new Error('L’email est requis');

    const response = await api.post('/utilisateurs/forgot-password', { email });
    return response.data;
  },

  async listerUtilisateursSupprimes() {
    const response = await api.get('/utilisateurs/supprimes');
    return response.data;
  },
  async restaurerUtilisateur(id, roleIds = []) {
    const response = await api.post(`/utilisateurs/${id}/restaurer`, { roleIds });
    return response.data;
  }

};
