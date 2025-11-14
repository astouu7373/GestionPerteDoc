// src/pages/Declarations/DeclarationDetail.jsx
import React from 'react';
import {
  Container,
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  Alert,
  CircularProgress,
  Grid,
  Paper,
  Chip,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Snackbar
} from '@mui/material';
import {
  ArrowBack,
  Edit,
  Delete,
  Download,
  Person,
  Description,
  CalendarToday,
  LocationOn,
  Assignment,
  Security,
  Restore
} from '@mui/icons-material';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { declarationService } from '../../services/declarationService';
import { usePermissions } from '../../hooks/usePermissions';
import { useAuth } from '../../contexts/AuthContext';
import { format } from 'date-fns';
import api from '../../services/api';

const DeclarationDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { user: currentUser } = useAuth();

  // Permissions depuis le hook partagé
  const {
    isAdmin,
    isSuperviseur,
    isAgent,
    canDeletePermanently,
    canRestoreDeclaration
  } = usePermissions();

  // Chargement de la déclaration
  const { data: declaration, isLoading, error } = useQuery(
    ['declaration', id],
    () => declarationService.getDeclarationDetail(id),
    { enabled: !!id, retry: false }
  );

  // Mutations
  const deleteMutation = useMutation(
    () => declarationService.supprimerDeclaration(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['declarations']);
        navigate('/declarations');
      }
    }
  );

  const restoreMutation = useMutation(
    () => declarationService.restaurerDeclaration(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['declaration', id]);
        queryClient.invalidateQueries(['declarations']);
      }
    }
  );

  const permanentDeleteMutation = useMutation(
    () => declarationService.supprimerDefinitivement(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['declarations']);
        navigate('/declarations');
      }
    }
  );

  // Snackbar state pour feedback utilisateur
  const [snackbar, setSnackbar] = React.useState({ open: false, message: '', severity: 'info' });
  const showSnackbar = (message, severity = 'info') => setSnackbar({ open: true, message, severity });
  const closeSnackbar = () => setSnackbar({ open: false, message: '', severity: 'info' });

  const handleGeneratePdf = async () => {
    try {
      const response = await api.get(`/declarations/${declaration.id}/pdf`, { responseType: 'blob' });
      const pdfBlob = new Blob([response.data], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(pdfBlob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `declaration-${declaration.numeroReference || declaration.id}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      console.error('Erreur génération PDF:', err);
      showSnackbar('Erreur génération PDF', 'error');
    }
  };


  // Helpers pour labels & couleurs statut
  const getStatutColor = (statut) => {
    switch (statut) {
      case 'VALIDEE': return 'success';
      case 'ENREGISTREE': return 'primary';
      case 'REJETEE': return 'error';
      case 'BROUILLON': return 'default';
      default: return 'default';
    }
  };

  const getStatutLabel = (statut) => {
    switch (statut) {
      case 'VALIDEE': return 'Validée';
      case 'ENREGISTREE': return 'Enregistrée';
      case 'REJETEE': return 'Rejetée';
      case 'BROUILLON': return 'Brouillon';
      default: return statut;
    }
  };

  // Permissions locales pour actions
  const canEditDeclaration = () => {
    if (!declaration || !currentUser) return false;
    if (declaration.supprime) return false;
    return isAdmin || isSuperviseur || (isAgent && declaration.utilisateur?.id === currentUser.id);
  };

  const canDeleteDeclaration = () => {
    if (!declaration || !currentUser) return false;
    if (declaration.supprime) return false;
    return isAdmin || isSuperviseur || (isAgent && declaration.utilisateur?.id === currentUser.id);
  };

  // Gestion des erreurs de mutation pour afficher un message lisible
  React.useEffect(() => {
    if (deleteMutation.isError) {
      const err = deleteMutation.error;
      const message = err?.response?.data?.erreur || err?.message || 'Erreur lors de la suppression';
      showSnackbar(message, 'error');
    }
  }, [deleteMutation.isError]);

  React.useEffect(() => {
    if (restoreMutation.isError) {
      const err = restoreMutation.error;
      const message = err?.response?.data?.erreur || err?.message || 'Erreur lors de la restauration';
      showSnackbar(message, 'error');
    } else if (restoreMutation.isSuccess) {
      showSnackbar('Déclaration restaurée avec succès', 'success');
    }
  }, [restoreMutation.isError, restoreMutation.isSuccess]);

  React.useEffect(() => {
    if (permanentDeleteMutation.isError) {
      const err = permanentDeleteMutation.error;
      const message = err?.response?.data?.erreur || err?.message || 'Erreur lors de la suppression définitive';
      showSnackbar(message, 'error');
    }
  }, [permanentDeleteMutation.isError]);

  // Loading / error UI
  if (isLoading) {
    return (
      <Container>
        <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '50vh' }}>
          <CircularProgress />
          <Typography sx={{ ml: 2 }}>Chargement de la déclaration...</Typography>
        </Box>
      </Container>
    );
  }

  if (error) {
    const serverMessage = error?.response?.data?.erreur || error?.message || 'Erreur lors du chargement';
    return (
      <Container>
        <Alert severity="error" sx={{ mt: 2 }}>{serverMessage}</Alert>
        <Button startIcon={<ArrowBack />} onClick={() => navigate('/declarations')} sx={{ mt: 2 }}>Retour aux déclarations</Button>
      </Container>
    );
  }

  if (!declaration) {
    return (
      <Container>
        <Alert severity="warning" sx={{ mt: 2 }}>Déclaration non trouvée</Alert>
        <Button startIcon={<ArrowBack />} onClick={() => navigate('/declarations')} sx={{ mt: 2 }}>Retour aux déclarations</Button>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg">
      <Box sx={{ py: 4 }}>
        {/* En-tête */}
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 4 }}>
          <Button startIcon={<ArrowBack />} onClick={() => navigate('/declarations')} sx={{ mr: 2 }}>Retour</Button>
          <Typography variant="h4" component="h1" sx={{ flexGrow: 1 }}>Détails de la déclaration</Typography>
          <Box sx={{ display: 'flex', gap: 1 }}>

            {/* Actions quand la déclaration n'est pas supprimée */}
            {!declaration.supprime && (
              <>
                {canEditDeclaration() && (
                  <Button variant="outlined" startIcon={<Edit />} onClick={() => navigate(`/declarations/${id}/modifier`)}>
                    Modifier
                  </Button>
                )}

                <Button variant="outlined" startIcon={<Download />} onClick={handleGeneratePdf}>
                  PDF
                </Button>

                {canDeleteDeclaration() && (
                  <Button
                    variant="outlined"
                    color="error"
                    startIcon={<Delete />}
                    onClick={() => {
                      if (window.confirm('Confirmer la suppression (action réversible) ?')) {
                        deleteMutation.mutate();
                      }
                    }}
                    disabled={deleteMutation.isLoading}
                  >
                    {deleteMutation.isLoading ? 'Suppression...' : 'Supprimer'}
                  </Button>
                )}
              </>
            )}

            {/* Actions quand la déclaration est supprimée */}
            {declaration.supprime && (
              <>
                {canRestoreDeclaration && (
                  <Button
                    variant="outlined"
                    startIcon={<Restore />}
                    onClick={() => {
                      if (window.confirm('Restaurer cette déclaration ?')) {
                        restoreMutation.mutate();
                      }
                    }}
                    disabled={restoreMutation.isLoading}
                  >
                    {restoreMutation.isLoading ? 'Restauration...' : 'Restaurer'}
                  </Button>
                )}

                {/* Suppression définitive : UNIQUEMENT ADMIN */}
                {isAdmin && (
                  <Button
                    variant="outlined"
                    color="error"
                    startIcon={<Delete />}
                    onClick={() => {
                      if (window.confirm(' Suppression définitive irréversible — Confirmer ?')) {
                        permanentDeleteMutation.mutate();
                      }
                    }}
                    disabled={permanentDeleteMutation.isLoading}
                  >
                    {permanentDeleteMutation.isLoading ? 'Suppression...' : 'Supprimer définitivement'}
                  </Button>
                )}
              </>
            )}

          </Box>
        </Box>

        {/* Statut et référence */}
        <Card sx={{ mb: 3 }}>
          <CardContent>
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <Box>
                <Typography variant="h5" gutterBottom>
                  Référence: <strong>{declaration.numeroReference}</strong>
                </Typography>
                <Chip label={getStatutLabel(declaration.statut)} color={getStatutColor(declaration.statut)} size="medium" />
                {declaration.supprime && <Chip label="SUPPRIMÉE" color="error" variant="outlined" size="small" sx={{ ml: 1 }} />}
              </Box>
              <Typography variant="body2" color="textSecondary">
                Créée le: {declaration.dateDeclaration ? format(new Date(declaration.dateDeclaration), 'dd/MM/yyyy à HH:mm') : 'Date inconnue'}
              </Typography>
            </Box>
          </CardContent>
        </Card>

        {/* Section complète : Document perdu, Déclarant, Traçabilité */}
        <Grid container spacing={3}>

          {/* Document perdu */}
          <Grid item xs={12} md={6}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                  <Description sx={{ mr: 1, color: 'primary.main' }} />
                  <Typography variant="h6">Document perdu</Typography>
                </Box>
                <List>
                  <ListItem>
                    <ListItemIcon><Assignment /></ListItemIcon>
                    <ListItemText primary="Type de document" secondary={declaration.typeDocumentLibelle || declaration.typeDocument?.libelle || 'Non spécifié'} />
                  </ListItem>
                  <ListItem>
                    <ListItemIcon><Description /></ListItemIcon>
                    <ListItemText primary="Numéro du document" secondary={declaration.numeroDocument || 'Non spécifié'} />
                  </ListItem>
                  <ListItem>
                    <ListItemIcon><CalendarToday /></ListItemIcon>
                    <ListItemText primary="Date de perte" secondary={declaration.datePerte ? format(new Date(declaration.datePerte), 'dd/MM/yyyy') : 'Non spécifiée'} />
                  </ListItem>
                  <ListItem>
                    <ListItemIcon><LocationOn /></ListItemIcon>
                    <ListItemText primary="Lieu de perte" secondary={declaration.lieuPerte || 'Non spécifié'} />
                  </ListItem>
                  {declaration.circonstances && (
                    <ListItem>
                      <ListItemText primary="Circonstances" secondary={declaration.circonstances} sx={{ whiteSpace: 'pre-wrap' }} />
                    </ListItem>
                  )}
                </List>
              </CardContent>
            </Card>
          </Grid>

          {/* Déclarant */}
          <Grid item xs={12} md={6}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                  <Person sx={{ mr: 1, color: 'primary.main' }} />
                  <Typography variant="h6">Déclarant</Typography>
                </Box>
                <List>
                  <ListItem>
                    <ListItemText
                      primary="Nom complet"
                      secondary={
                        declaration.declarant
                          ? `${declaration.declarant.prenom || ''} ${declaration.declarant.nom || ''}`.trim() || 'Non spécifié'
                          : 'Non spécifié'
                      }
                    />
                  </ListItem>

                  {declaration.declarant?.numCarteIdentite && <ListItem><ListItemText primary="Numéro Carte d'Identité" secondary={declaration.declarant.numCarteIdentite} /></ListItem>}
                  {declaration.declarant?.numNina && <ListItem><ListItemText primary="Numéro NINA" secondary={declaration.declarant.numNina} /></ListItem>}
                  {declaration.declarant?.numPasseport && <ListItem><ListItemText primary="Numéro Passeport" secondary={declaration.declarant.numPasseport || 'Non spécifié'} /></ListItem>}
                  <ListItem><ListItemText primary="Téléphone" secondary={declaration.declarant?.telephone || 'Non spécifié'} /></ListItem>
                  {declaration.declarant?.email && <ListItem><ListItemText primary="Email" secondary={declaration.declarant.email} /></ListItem>}
                  {declaration.declarant?.adresse && <ListItem><ListItemText primary="Adresse" secondary={declaration.declarant.adresse} /></ListItem>}
                  {declaration.declarant?.dateNaissance && <ListItem><ListItemText primary="Date de naissance" secondary={format(new Date(declaration.declarant.dateNaissance), 'dd/MM/yyyy')} /></ListItem>}
                  {declaration.declarant?.lieuNaissance && <ListItem><ListItemText primary="Lieu de naissance" secondary={declaration.declarant.lieuNaissance} /></ListItem>}
                </List>
              </CardContent>
            </Card>
          </Grid>

          {/* Traçabilité */}
          <Grid item xs={12}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>Informations de traçabilité</Typography>
                <Grid container spacing={3}>

                  {/* Création */}
                  <Grid item xs={12} md={6}>
                    <Paper sx={{ p: 2, bgcolor: 'background.default' }}>
                      <Typography variant="subtitle2" gutterBottom color="primary">Création</Typography>
                      <Typography><strong>Matricule:</strong> {declaration.utilisateurMatricule || 'Non spécifié'}</Typography>
                      <Typography><strong>Nom:</strong> {declaration.utilisateurPrenom && declaration.utilisateurNom ? `${declaration.utilisateurPrenom} ${declaration.utilisateurNom}` : 'Non spécifié'}</Typography>
                      <Typography><strong>Date:</strong> {declaration.dateDeclaration ? format(new Date(declaration.dateDeclaration), 'dd/MM/yyyy à HH:mm') : 'Non spécifiée'}</Typography>
                      <Typography><strong>Créé par:</strong> {declaration.creeParNom || 'Non spécifié'}{declaration.creeLe ? ` le ${format(new Date(declaration.creeLe), 'dd/MM/yyyy à HH:mm')}` : ''}</Typography>
                    </Paper>
                  </Grid>

                  {/* Dernière modification */}
                  {declaration.modifieLe && (
                    <Grid item xs={12} md={6}>
                      <Paper sx={{ p: 2, bgcolor: 'background.default' }}>
                        <Typography variant="subtitle2" gutterBottom color="primary">Dernière modification</Typography>
                        <Typography>
                          <strong>Modifié par:</strong>{' '}
                          {declaration.modifieParPrenom && declaration.modifieParNom
                            ? `${declaration.modifieParPrenom} ${declaration.modifieParNom} (${declaration.modifieParMatricule || 'N/A'})`
                            : declaration.modifieParNom || 'Aucune information'}
                        </Typography>
                        <Typography><strong>Date:</strong> {format(new Date(declaration.modifieLe), 'dd/MM/yyyy à HH:mm')}</Typography>
                      </Paper>
                    </Grid>
                  )}

                  {/* Suppression */}
                  {declaration.supprime && declaration.supprimeLe && (
                    <Grid item xs={12} md={6}>
                      <Paper sx={{ p: 2, bgcolor: 'error.light', color: 'error.contrastText' }}>
                        <Typography variant="subtitle2" gutterBottom>Suppression</Typography>
                        <Typography><strong>Supprimé par:</strong> {declaration.supprimeParNom || 'Non spécifié'}</Typography>
                        <Typography><strong>Date:</strong> {format(new Date(declaration.supprimeLe), 'dd/MM/yyyy à HH:mm')}</Typography>
                      </Paper>
                    </Grid>
                  )}

                </Grid>
              </CardContent>
            </Card>
          </Grid>
        </Grid>

        {/* Messages d'état des mutations */}
        {deleteMutation.isError && (
          <Alert severity="error" sx={{ mt: 2 }}>
            {deleteMutation.error?.response?.data?.erreur || deleteMutation.error?.message || 'Erreur lors de la suppression'}
          </Alert>
        )}

        {restoreMutation.isError && (
          <Alert severity="error" sx={{ mt: 2 }}>
            {restoreMutation.error?.response?.data?.erreur || restoreMutation.error?.message || 'Erreur lors de la restauration'}
          </Alert>
        )}

        {permanentDeleteMutation.isError && (
          <Alert severity="error" sx={{ mt: 2 }}>
            {permanentDeleteMutation.error?.response?.data?.erreur || permanentDeleteMutation.error?.message || 'Erreur lors de la suppression définitive'}
          </Alert>
        )}

        <Snackbar
          open={snackbar.open}
          autoHideDuration={4000}
          onClose={closeSnackbar}
          message={snackbar.message}
          anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
        />
      </Box>
    </Container>
  );
};

export default DeclarationDetail;
