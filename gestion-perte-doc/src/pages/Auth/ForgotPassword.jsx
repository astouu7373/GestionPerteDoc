import React, { useState } from 'react';
import { Box, TextField, Button, Typography, Snackbar, Alert, CircularProgress } from '@mui/material';
import { utilisateurService } from '../../services/utilisateurService';
import { useNavigate } from 'react-router-dom';

const ForgotPassword = () => {
  const [email, setEmail] = useState('');
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async () => {
    if (!email) {
      setError('Veuillez saisir votre email');
      return;
    }

    setLoading(true);
    setError(null);
    setSuccess(null);

    try {
      await utilisateurService.forgotMotDePasse(email);
      setSuccess('Email envoyé ! Vérifiez votre boîte pour le lien de réinitialisation.');
      setTimeout(() => navigate('/login'), 3000);
    } catch (err) {
      setError(err.response?.data?.erreur || err.message || 'Erreur lors de l’envoi de l’email');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{ minHeight: '100vh', display:'flex', justifyContent:'center', alignItems:'center' }}>
      <Box sx={{ p:4, border:'1px solid #ccc', borderRadius:2, width:'100%', maxWidth:400 }}>
        <Typography variant="h6" gutterBottom>Mot de passe oublié</Typography>
        <Typography variant="body2" gutterBottom>
          Entrez votre email pour recevoir un lien de réinitialisation.
        </Typography>

        <TextField
          label="Email"
          type="email"
          fullWidth
          margin="normal"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />

        <Button
          variant="contained"
          color="primary"
          fullWidth
          sx={{ mt:2 }}
          onClick={handleSubmit}
          disabled={loading}
        >
          {loading ? <CircularProgress size={24} /> : 'Envoyer le lien'}
        </Button>

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

export default ForgotPassword;
