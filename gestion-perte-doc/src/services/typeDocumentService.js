// services/typeDocumentService.js
import api from './api';

export const typeDocumentService = {
  // Lister tous les types de documents
  async listerTypesDocument() {
    const response = await api.get('/types-document');
    return response.data;
  },

  // Créer un type de document
  async creerTypeDocument(typeData) {
    const response = await api.post('/types-document', typeData);
    return response.data;
  },

  async mettreAJourTypeDocument(id, typeData) {
      console.log(` PUT /types-document/${id}`, typeData);
      try {
        const response = await api.put(`/types-document/${id}`, typeData);
        console.log(' Réponse modification:', response.data);
        return response.data;
      } catch (error) {
        console.error(' Erreur API modification:', error);
        throw error;
      }
    },

  // Supprimer un type de document
  async supprimerTypeDocument(id) {
      console.log(` DELETE /types-document/${id}`);
      try {
        const response = await api.delete(`/types-document/${id}`);
        console.log(' Réponse suppression:', response.data);
        return response.data;
      } catch (error) {
        console.error(' Erreur API suppression:', error);
        throw error;
      }
    },
  

  // Obtenir un type de document par ID
  async getTypeDocument(id) {
    const response = await api.get(`/types-document/${id}`);
    return response.data;
  }
};