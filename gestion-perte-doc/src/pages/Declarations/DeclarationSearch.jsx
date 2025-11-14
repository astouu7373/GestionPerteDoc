import React, { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  TextField,
  Button,
  Grid,
  Alert,
  CircularProgress,
  Chip,
  Divider,
  Paper
} from '@mui/material';
import {
  Search,
  Description,
  Person,
  Add
} from '@mui/icons-material';
import { useMutation } from 'react-query';
import { declarationService } from '../../services/declarationService';
import { format } from 'date-fns';
import { useNavigate } from 'react-router-dom';

const DeclarationSearch = () => {
  const [numeroReference, setNumeroReference] = useState('');
  const [searchResult, setSearchResult] = useState(null);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const searchMutation = useMutation(
    (reference) => declarationService.rechercherParReference(reference),
    {
      onSuccess: (data) => {
        setSearchResult(data);
        setError('');
      },
      onError: (error) => {
        setSearchResult(null);
        setError(error.response?.data?.erreur || 'Déclaration non trouvée');
      }
    }
  );

  const handleSearch = (e) => {
    e.preventDefault();
    if (numeroReference.trim()) {
      searchMutation.mutate(numeroReference.trim());
    }
  };

  const handleEdit = () => {
    if (searchResult?.id) {
      navigate(`/declarations/${searchResult.id}/modifier`);
    }
  };

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

  return (
    <Box>
      <Typography variant="h4" gutterBottom sx={{ fontWeight: 'bold', mb: 4 }}>
        Recherche de Déclaration
      </Typography>

      <Card sx={{ mb: 3 }}>
        <CardContent>
          <form onSubmit={handleSearch}>
            <Grid container spacing={2} alignItems="flex-end">
              <Grid item xs={12} md={8}>
                <TextField
                  fullWidth
                  label="Numéro de référence"
                  value={numeroReference}
                  onChange={(e) => setNumeroReference(e.target.value)}
                  placeholder="Ex: DECL-BKO-001"
                  InputProps={{
                    startAdornment: <Search sx={{ color: 'text.secondary', mr: 1 }} />
                  }}
                />
              </Grid>
              <Grid item xs={12} md={4}>
                <Button
                  fullWidth
                  type="submit"
                  variant="contained"
                  disabled={searchMutation.isLoading || !numeroReference.trim()}
                  startIcon={searchMutation.isLoading ? <CircularProgress size={20} /> : <Search />}
                >
                  {searchMutation.isLoading ? 'Recherche...' : 'Rechercher'}
                </Button>
              </Grid>
            </Grid>
          </form>
        </CardContent>
      </Card>

      {error && <Alert severity="error" sx={{ mt: 2 }}>{error}</Alert>}

      {searchResult && (
        <Card>
          <CardContent>
            {/* En-tête */}
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 3 }}>
              <Box>
                <Typography variant="h5" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <Description color="primary" />
                  Déclaration {searchResult.numeroReference}
                </Typography>
                <Chip
                  label={getStatutLabel(searchResult.statut)}
                  color={getStatutColor(searchResult.statut)}
                  sx={{ mt: 1 }}
                />
              </Box>
              <Box>
                <Button
                  variant="contained"
                  color="secondary"
                  startIcon={<Add />}
                  onClick={handleEdit}
                  disabled={!searchResult.id}
                >
                  Modifier
                </Button>
              </Box>
            </Box>

            <Grid container spacing={3}>
              {/* Déclarant */}
              <Grid item xs={12} md={6}>
                <Paper variant="outlined" sx={{ p: 2 }}>
                  <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <Person color="primary" />
                    Déclarant
                  </Typography>
                  <Divider sx={{ mb: 2 }} />
                  <Typography><strong>Nom:</strong> {searchResult.declarant?.nom} {searchResult.declarant?.prenom}</Typography>
                  {searchResult.declarant?.email && <Typography><strong>Email:</strong> {searchResult.declarant.email}</Typography>}
                  {searchResult.declarant?.telephone && <Typography><strong>Téléphone:</strong> {searchResult.declarant.telephone}</Typography>}
                  {searchResult.declarant?.numNina && <Typography><strong>NINA:</strong> {searchResult.declarant.numNina}</Typography>}
                </Paper>
              </Grid>

              {/* Document */}
              <Grid item xs={12} md={6}>
                <Paper variant="outlined" sx={{ p: 2 }}>
                  <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <Description color="primary" />
                    Document
                  </Typography>
                  <Divider sx={{ mb: 2 }} />
                  <Typography><strong>Type:</strong> {searchResult.typeDocumentLibelle}</Typography>
                  {searchResult.numeroDocument && <Typography><strong>Numéro:</strong> {searchResult.numeroDocument}</Typography>}
                  {searchResult.datePerte && <Typography><strong>Date perte:</strong> {format(new Date(searchResult.datePerte), 'dd/MM/yyyy')}</Typography>}
                  {searchResult.lieuPerte && <Typography><strong>Lieu perte:</strong> {searchResult.lieuPerte}</Typography>}
                </Paper>
              </Grid>

              {/* Circonstances */}
              {searchResult.circonstances && (
                <Grid item xs={12}>
                  <Paper variant="outlined" sx={{ p: 2 }}>
                    <Typography variant="h6" gutterBottom>Circonstances</Typography>
                    <Divider sx={{ mb: 2 }} />
                    <Typography>{searchResult.circonstances}</Typography>
                  </Paper>
                </Grid>
              )}

              {/* Traçabilité */}
              <Grid item xs={12}>
                <Paper variant="outlined" sx={{ p: 2 }}>
                  <Typography variant="h6" gutterBottom>Traçabilité</Typography>
                  <Divider sx={{ mb: 2 }} />
                  <Grid container spacing={2}>
                    <Grid item xs={12} md={6}>
                      <Typography variant="subtitle2" color="textSecondary">Créée par</Typography>
                      <Typography>{searchResult.utilisateurPrenom} {searchResult.utilisateurNom}</Typography>
                    </Grid>
                    <Grid item xs={12} md={6}>
                      <Typography variant="subtitle2" color="textSecondary">Date création</Typography>
                      <Typography>{format(new Date(searchResult.dateDeclaration), 'dd/MM/yyyy à HH:mm')}</Typography>
                    </Grid>
                  </Grid>
                </Paper>
              </Grid>
            </Grid>
          </CardContent>
        </Card>
      )}
    </Box>
  );
};

export default DeclarationSearch;
