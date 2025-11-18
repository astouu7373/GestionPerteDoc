// src/pages/Setup/SetupSystem.js
import React, { useEffect, useState } from 'react';
import { systemService } from '../services/systemService';

const SetupSystem = () => {
  const [loading, setLoading] = useState(true);
  const [initialised, setInitialised] = useState(false);
  const [formData, setFormData] = useState({
    posteNom: '',
    adresse: '',
    posteTelephone: '',
    nomAdmin: '',
    prenomAdmin: '',
    email: '',
    motDePasse: ''
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Vérifier l'état du système au chargement
  useEffect(() => {
    const checkSystem = async () => {
      try {
        const result = await systemService.etatSysteme();
        setInitialised(result.initialised);
      } catch (err) {
        console.error(err);
        setError('Impossible de vérifier l’état du système');
      } finally {
        setLoading(false);
      }
    };
    checkSystem();
  }, []);

  const handleChange = (e) => {
    setFormData(prev => ({
      ...prev,
      [e.target.name]: e.target.value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      await systemService.initialiserSysteme(formData);
      setSuccess('Système initialisé avec succès !');
      setInitialised(true);
    } catch (err) {
      console.error(err);
      setError(err.response?.data || 'Erreur lors de l’initialisation');
    }
  };

  if (loading) {
    return <div style={{ display:'flex', justifyContent:'center', alignItems:'center', height:'100vh' }}>Chargement...</div>;
  }

  if (initialised) {
    return (
      <div style={{ textAlign:'center', marginTop:'50px' }}>
        <h2>Le système est déjà initialisé</h2>
        <p>Vous pouvez maintenant vous connecter avec votre compte administrateur.</p>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: '600px', margin: '50px auto', padding: '20px', border: '1px solid #ccc', borderRadius: '8px' }}>
      <h2>Initialisation du Système</h2>

      {error && <div style={{ color: 'red', marginBottom: '10px' }}>{error}</div>}
      {success && <div style={{ color: 'green', marginBottom: '10px' }}>{success}</div>}

      <form onSubmit={handleSubmit}>
        <h3>Poste de Police</h3>
        <div>
          <label>Nom du poste</label>
          <input type="text" name="posteNom" value={formData.posteNom} onChange={handleChange} required />
        </div>
        <div>
          <label>Adresse</label>
          <input type="text" name="adresse" value={formData.adresse} onChange={handleChange} required />
        </div>
        <div>
          <label>Téléphone</label>
          <input type="text" name="posteTelephone" value={formData.posteTelephone} onChange={handleChange} required />
        </div>

        <h3>Administrateur</h3>
        <div>
          <label>Nom</label>
          <input type="text" name="nomAdmin" value={formData.nomAdmin} onChange={handleChange} required />
        </div>
        <div>
          <label>Prénom</label>
          <input type="text" name="prenomAdmin" value={formData.prenomAdmin} onChange={handleChange} required />
        </div>
        <div>
          <label>Email</label>
          <input type="email" name="email" value={formData.email} onChange={handleChange} required />
        </div>
        <div>
          <label>Mot de passe</label>
          <input type="password" name="motDePasse" value={formData.motDePasse} onChange={handleChange} required />
        </div>

        <button type="submit" style={{ marginTop: '20px' }}>Initialiser le système</button>
      </form>
    </div>
  );
};

export default SetupSystem;
