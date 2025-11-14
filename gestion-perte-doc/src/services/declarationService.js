import api from './api';

export const declarationService = {
  // Créer une déclaration
  async creerDeclaration(declarationData) {
    const response = await api.post('/declarations', declarationData);
    return response.data;
  },

  // Lister les déclarations générales
  async listerDeclarations() {
    const response = await api.get('/declarations');
    return response.data;
  },

  // Lister les déclarations actives de l'utilisateur
  async listerDeclarationsActives() {
    const response = await api.get('/declarations/actives');
    return response.data;
  },

  // Lister les déclarations actives du poste
  async listerDeclarationsActivesPoste() {
    const response = await api.get('/declarations/actives/poste');
    return response.data;
  },

  // Lister les déclarations supprimées de l'utilisateur
  async listerDeclarationsSupprimees() {
    const response = await api.get('/declarations/supprimees');
    return response.data;
  },

  // Lister les déclarations supprimées du poste
  async listerDeclarationsSupprimeesPoste() {
    const response = await api.get('/declarations/supprimees/poste');
    return response.data;
  },

  // Modifier une déclaration
  async modifierDeclaration(declarationId, declarationData) {
    const response = await api.put(`/declarations/${declarationId}`, declarationData);
    return response.data;
  },

  // Supprimer une déclaration (logique)
  async supprimerDeclaration(declarationId) {
    const response = await api.delete(`/declarations/${declarationId}`);
    return response.data;
  },

  // Restaurer une déclaration
  async restaurerDeclaration(declarationId) {
    const response = await api.patch(`/declarations/${declarationId}/restaurer`);
    return response.data;
  },

  // Supprimer définitivement une déclaration
  async supprimerDefinitivement(declarationId) {
    const response = await api.delete(`/declarations/${declarationId}/definitif`);
    return response.data;
  },

  // Rechercher par numéro de référence
  async rechercherParReference(numeroReference) {
    const response = await api.get(`/declarations/reference/${numeroReference}`);
    return response.data;
  },

  // Obtenir les statistiques générales (ancienne méthode)
  async getStats() {
    const response = await api.get('/dashboard/stats');
    return response.data;
  },

  // Stats personnelles
  async getStatsUser(userId) {
    const response = await api.get(`/dashboard/stats/user/${userId}`);
    return response.data;
  },

  // Stats poste (admin)
  async getStatsPoste(userId) {
    const response = await api.get(`/dashboard/stats/poste?userId=${userId}`);
    return response.data;
  },

  async rechercherDeclarant({ email, numNina, numPassePort, numCarteIdentite }) {
    const response = await api.post(`declarations/rechercher-declarant`, {
      email,
      numNina,
      numPassePort,
      numCarteIdentite
    });
    return response.data;
  },
  // Changer le statut
  async changerStatut(declarationId, statut) {
    const response = await api.patch(
      `/declarations/${declarationId}/statut?statut=${statut}`
    );
    return response.data;
  },

  // Obtenir les détails d'une déclaration
  async getDeclarationDetail(declarationId) {
    const response = await api.get(`/declarations/${declarationId}`);
    return response.data;
  },

  // Générer PDF
  async genererPdf(declarationId) {
    const response = await api.get(`/declarations/${declarationId}/pdf`, { responseType: 'blob' });
    return response.data;
  },

  // Vérifier les permissions
  async verifierPermissions(declarationId) {
    const response = await api.get(`/declarations/${declarationId}/permissions`);
    return response.data;
  },
};
