// ResetPassword.jsx
import React, { useState } from 'react';
import { Box, TextField, Button, Typography, Snackbar, Alert } from '@mui/material';
import { useParams, useNavigate } from 'react-router-dom';
import { utilisateurService } from '../../services/utilisateurService';

const ResetPassword = () => {
  const { token } = useParams();
  const navigate = useNavigate();

  const [nouveauMotDePasse, setNouveauMotDePasse] = useState('');
  const [confirmation, setConfirmation] = useState('');
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  const handleSubmit = async () => {
    if (!nouveauMotDePasse || !confirmation) {
      return setError('Veuillez remplir tous les champs');
    }
    if (nouveauMotDePasse !== confirmation) {
      return setError('Les mots de passe ne correspondent pas');
    }

    try {
      // Correction : passer deux arguments séparés
      await utilisateurService.resetMotDePasse(token, nouveauMotDePasse);
      setSuccess('Mot de passe réinitialisé avec succès ! Vous pouvez maintenant vous connecter.');
      setTimeout(() => navigate('/login'), 2000);
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la réinitialisation');
    }
  };

  if (!token) return <Typography>Token invalide ou expiré</Typography>;

  return (
    <Box sx={{ minHeight: '100vh', display:'flex', justifyContent:'center', alignItems:'center' }}>
      <Box sx={{ p:4, border:'1px solid #ccc', borderRadius:2, width:'100%', maxWidth:400 }}>
        <Typography variant="h6" gutterBottom>Réinitialiser le mot de passe</Typography>

        <TextField
          type="password"
          label="Nouveau mot de passe"
          fullWidth
          margin="normal"
          value={nouveauMotDePasse}
          onChange={(e) => setNouveauMotDePasse(e.target.value)}
        />

        <TextField
          type="password"
          label="Confirmer le mot de passe"
          fullWidth
          margin="normal"
          value={confirmation}
          onChange={(e) => setConfirmation(e.target.value)}
        />

        <Button variant="contained" color="primary" fullWidth sx={{ mt:2 }} onClick={handleSubmit}>
          Réinitialiser
        </Button>

        {/* Notifications */}
        <Snackbar open={!!error} autoHideDuration={6000} onClose={() => setError(null)}>
          <Alert onClose={() => setError(null)} severity="error">{error}</Alert>
        </Snackbar>

        <Snackbar open={!!success} autoHideDuration={6000} onClose={() => setSuccess(null)}>
          <Alert onClose={() => setSuccess(null)} severity="success">{success}</Alert>
        </Snackbar>
      </Box>
    </Box>
  );
};

export default ResetPassword;
