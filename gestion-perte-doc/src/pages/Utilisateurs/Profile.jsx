import React, { useState } from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  Grid,
  Chip,
  Divider,
  Button,
  TextField
} from '@mui/material';
import {
  Edit,
  Person,
  Email,
  Badge,
  Security
} from '@mui/icons-material';
import { useAuth } from '../../contexts/AuthContext';

const Profile = () => {
  const { user, updateUser } = useAuth(); // updateUser doit envoyer les données au backend et mettre à jour le contexte

  const [editMode, setEditMode] = useState(false);
  const [prenom, setPrenom] = useState(user?.prenom || '');
  const [nom, setNom] = useState(user?.nom || '');
  const [email, setEmail] = useState(user?.email || '');

  const getRoleColor = (role) => {
    switch (role) {
      case 'ROLE_ADMIN': return 'error';
      case 'ROLE_SUPERVISEUR': return 'warning';
      case 'ROLE_AGENT': return 'primary';
      default: return 'default';
    }
  };

  const getRoleLabel = (role) => role.replace('ROLE_', '');

  const handleSave = () => {
    updateUser({ prenom, nom, email });
    setEditMode(false);
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom sx={{ fontWeight: 'bold', mb: 4 }}>
        Mon Profil
      </Typography>

      <Grid container spacing={3}>
        {/* Informations personnelles */}
        <Grid item xs={12} md={8}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 3 }}>
                <Typography variant="h5" gutterBottom>
                  Informations Personnelles
                </Typography>
                <Button
                  startIcon={<Edit />}
                  variant="outlined"
                  size="small"
                  onClick={() => setEditMode(!editMode)}
                >
                  {editMode ? 'Annuler' : 'Modifier'}
                </Button>
              </Box>

              <Grid container spacing={3}>
                <Grid item xs={12} sm={6}>
                  {editMode ? (
                    <TextField
                      label="Prénom"
                      value={prenom}
                      onChange={(e) => setPrenom(e.target.value)}
                      fullWidth
                    />
                  ) : (
                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                      <Person sx={{ mr: 2, color: 'primary.main' }} />
                      <Box>
                        <Typography variant="body2" color="textSecondary">
                          Nom Complet
                        </Typography>
                        <Typography variant="body1" fontWeight="medium">
                          {user?.prenom} {user?.nom}
                        </Typography>
                      </Box>
                    </Box>
                  )}
                </Grid>

                <Grid item xs={12} sm={6}>
                  {editMode ? (
                    <TextField
                      label="Email"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      fullWidth
                    />
                  ) : (
                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                      <Email sx={{ mr: 2, color: 'primary.main' }} />
                      <Box>
                        <Typography variant="body2" color="textSecondary">
                          Email
                        </Typography>
                        <Typography variant="body1" fontWeight="medium">
                          {user?.email}
                        </Typography>
                      </Box>
                    </Box>
                  )}
                </Grid>

                {/* Autres infos */}
                <Grid item xs={12} sm={6}>
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                    <Badge sx={{ mr: 2, color: 'primary.main' }} />
                    <Box>
                      <Typography variant="body2" color="textSecondary">
                        Matricule
                      </Typography>
                      <Typography variant="body1" fontWeight="medium">
                        {user?.matricule || 'Non défini'}
                      </Typography>
                    </Box>
                  </Box>
                </Grid>

                <Grid item xs={12} sm={6}>
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                    <Security sx={{ mr: 2, color: 'primary.main' }} />
                    <Box>
                      <Typography variant="body2" color="textSecondary">
                        Poste de Police
                      </Typography>
                      <Typography variant="body1" fontWeight="medium">
                        {user?.postePoliceNom || 'Non affecté'}
                      </Typography>
                    </Box>
                  </Box>
                </Grid>
              </Grid>

              {editMode && (
                <Button
                  variant="contained"
                  color="primary"
                  sx={{ mt: 2 }}
                  onClick={handleSave}
                >
                  Enregistrer
                </Button>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Rôles et statut */}
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Rôles et Permissions
              </Typography>

              <Box sx={{ mb: 3 }}>
                <Typography variant="body2" color="textSecondary" gutterBottom>
                  Rôles attribués
                </Typography>
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                  {user?.roles?.map((role, index) => (
                    <Chip
                      key={index}
                      label={getRoleLabel(role)}
                      color={getRoleColor(role)}
                      variant="outlined"
                      size="small"
                    />
                  ))}
                </Box>
              </Box>

              <Divider sx={{ my: 2 }} />

              <Box>
                <Typography variant="body2" color="textSecondary" gutterBottom>
                  Statut du compte
                </Typography>
                <Chip
                  label={user?.actif ? 'Actif' : 'Inactif'}
                  color={user?.actif ? 'success' : 'default'}
                  variant="filled"
                  size="small"
                />
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Profile;
