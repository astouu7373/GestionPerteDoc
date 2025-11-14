import React, { useState, useEffect } from 'react';
import {
  Container,
  Box,
  Card,
  CardContent,
  TextField,
  Button,
  Typography,
  Alert,
  CircularProgress,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Grid,
  Link
} from '@mui/material';
import { PersonAdd as PersonAddIcon } from '@mui/icons-material';
import { useNavigate, Link as RouterLink } from 'react-router-dom';
import { authService } from '../../services/authService';
import api from '../../services/api'; // Assure-toi que c'est ton instance axios

const Register = () => {
  const [formData, setFormData] = useState({
    nom: '',
    prenom: '',
    email: '',
    motDePasse: '',
    confirmPassword: '',
    postePoliceId: ''
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const [postesPolice, setPostesPolice] = useState([]);
  const [isFirstUser, setIsFirstUser] = useState(false);

  const navigate = useNavigate();

  // Initialisation postesPolice et premier utilisateur
  useEffect(() => {
    const postesFictifs = [
      { id: 1, nom: 'Poste De Police Principal - Commissariat Central' }
    ];
    setPostesPolice(postesFictifs);

    if (postesFictifs.length > 0) {
      setFormData(prev => ({
        ...prev,
        postePoliceId: postesFictifs[0].id.toString()
      }));
    }

	const checkFirstUser = async () => {
	      try {
	        const response = await api.get("/utilisateurs/existe-admin");
	        console.log("existeAdmin API response:", response.data);
	        setIsFirstUser(!response.data.existe); // <-- si aucun admin, c'est le premier
	      } catch (error) {
	        console.error("Erreur vérification admin", error);
	      }
	    };
	    checkFirstUser();
	}, []);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
    setError('');
    setSuccess('');
  };

  const validateForm = () => {
    if (!formData.nom || !formData.prenom || !formData.email || 
        !formData.motDePasse || !formData.confirmPassword || !formData.postePoliceId) {
      setError('Tous les champs sont obligatoires');
      return false;
    }

    if (formData.motDePasse.length < 6) {
      setError('Le mot de passe doit contenir au moins 6 caractères');
      return false;
    }

    if (formData.motDePasse !== formData.confirmPassword) {
      setError('Les mots de passe ne correspondent pas');
      return false;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(formData.email)) {
      setError('Format d\'email invalide');
      return false;
    }

    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess('');

    if (!validateForm()) {
      setLoading(false);
      return;
    }

    try {
      console.log('Tentative d\'inscription:', formData);

      const registerData = {
        nom: formData.nom,
        prenom: formData.prenom,
        email: formData.email,
        motDePasse: formData.motDePasse,
        postePoliceId: parseInt(formData.postePoliceId),
        // Premier utilisateur = admin actif
        isAdmin: isFirstUser,
        actif: isFirstUser
      };

      const response = await authService.register(registerData);
      console.log('Réponse d\'inscription:', response);

      if (isFirstUser) {
        setSuccess('Compte admin créé avec succès ! Vous pouvez maintenant vous connecter.');
        setTimeout(() => {
          navigate('/login');
        }, 2000);
      } else {
        setSuccess(response.message || 'Compte créé avec succès ! En attente d\'activation par un administrateur.');
      }

      setFormData({
        nom: '',
        prenom: '',
        email: '',
        motDePasse: '',
        confirmPassword: '',
        postePoliceId: postesPolice[0]?.id.toString() || ''
      });

    } catch (err) {
      console.error('Erreur d\'inscription:', err);
      setError(err.response?.data?.erreur || 'Erreur lors de la création du compte');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container component="main" maxWidth="md">
      <Box
        sx={{
          minHeight: '100vh',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          py: 4
        }}
      >
        <Card sx={{ width: '100%', maxWidth: 600 }}>
          <CardContent sx={{ p: 4 }}>
            <Box sx={{ textAlign: 'center', mb: 3 }}>
              <PersonAddIcon sx={{ fontSize: 48, color: 'primary.main', mb: 2 }} />
              <Typography component="h1" variant="h4" gutterBottom>
                {isFirstUser ? 'Créer le compte Admin' : 'Créer un compte'}
              </Typography>
              <Typography color="textSecondary">
                Système de Gestion des Pertes de Documents
              </Typography>
            </Box>

            {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
            {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}

            <Box component="form" onSubmit={handleSubmit}>
              <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Nom"
                    name="nom"
                    value={formData.nom}
                    onChange={handleChange}
                    required
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Prénom"
                    name="prenom"
                    value={formData.prenom}
                    onChange={handleChange}
                    required
                  />
                </Grid>
              </Grid>

              <TextField
                fullWidth
                label="Email"
                name="email"
                type="email"
                value={formData.email}
                onChange={handleChange}
                required
              />

              <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Mot de passe"
                    name="motDePasse"
                    type="password"
                    value={formData.motDePasse}
                    onChange={handleChange}
                    required
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Confirmer le mot de passe"
                    name="confirmPassword"
                    type="password"
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    required
                  />
                </Grid>
              </Grid>

              <FormControl fullWidth margin="normal" required>
                <InputLabel>Poste de police</InputLabel>
                <Select
                  name="postePoliceId"
                  value={formData.postePoliceId}
                  onChange={handleChange}
                >
                  {postesPolice.map((poste) => (
                    <MenuItem key={poste.id} value={poste.id}>
                      {poste.nom}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>

              <Button
                type="submit"
                fullWidth
                variant="contained"
                size="large"
                disabled={loading}
                sx={{ mt: 3, mb: 2 }}
              >
                {loading ? <CircularProgress size={24} /> : 
                  isFirstUser ? 'Créer le compte Admin' : 'Créer le compte'}
              </Button>

              <Box sx={{ textAlign: 'center' }}>
                <Typography variant="body2">
                  Déjà un compte ?{' '}
                  <Link component={RouterLink} to="/login" variant="body2">
                    Se connecter
                  </Link>
                </Typography>
              </Box>
            </Box>

            <Alert severity={isFirstUser ? "success" : "info"} sx={{ mt: 3 }}>
              <Typography variant="body2">
                <strong>
                  {isFirstUser
                    ? "Important: Ce premier compte sera créé en tant qu'Administrateur avec tous les privilèges."
                    : "Important: Votre compte sera créé en statut 'inactif'. Un administrateur devra l'activer avant que vous puissiez vous connecter."
                  }
                </strong>
              </Typography>
            </Alert>
          </CardContent>
        </Card>
      </Box>
    </Container>
  );
};

export default Register;
