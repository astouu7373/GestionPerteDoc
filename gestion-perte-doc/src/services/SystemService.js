// src/services/systemService.js
import api from './api';

export const systemService = {
  // Vérifier si le système est initialisé (admin existant)
  async etatSysteme() {
    const response = await api.get('/system/etat');
    return response.data; // { initialised: true/false }
  },

  // Initialiser le système (poste + admin)
  async initialiserSysteme(initialisationData) {
    // On ne fournit plus codeUnique ni matricule
    const response = await api.post('/system/initialiser', initialisationData);
    return response.data;
  }
};
