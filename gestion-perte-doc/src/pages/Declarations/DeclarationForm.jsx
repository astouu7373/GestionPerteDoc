import React, { useState } from 'react';
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
  Stepper,
  Step,
  StepLabel,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Paper
} from '@mui/material';
import { Save, Search, ArrowBack, Description, Person, Add } from '@mui/icons-material';
import { useNavigate, useParams } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { declarationService } from '../../services/declarationService';
import { typeDocumentService } from '../../services/typeDocumentService';
import { useAuth } from '../../contexts/AuthContext';
import { usePermissions } from '../../hooks/usePermissions';

const steps = ['Informations du Document', 'Informations du Déclarant', 'Confirmation'];

const DeclarationForm = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { user } = useAuth();
  const { isAdmin, isSuperviseur, isAgent } = usePermissions();

  const [activeStep, setActiveStep] = useState(0);
  const [searchDialogOpen, setSearchDialogOpen] = useState(false);
  const [createTypeDialogOpen, setCreateTypeDialogOpen] = useState(false);
  const [newTypeName, setNewTypeName] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [searchData, setSearchData] = useState({
    email: '',
    numNina: '',
    numPassePort: '',
    numCarteIdentite: ''
  });

  const [formData, setFormData] = useState({
    typeDocumentId: '',
    numeroDocument: '',
    datePerte: '',
    lieuPerte: '',
    circonstances: '',
    declarant: {
      nom: '',
      prenom: '',
      dateNaissance: '',
      lieuNaissance: '',
      adresse: '',
      telephone: '',
      email: '',
      numCarteIdentite: '',
      numNina: '',
      numPassePort: ''
    },
    statut: 'ENREGISTREE'
  });

  // Charger les types de documents
  const { data: typesDocument, isLoading: typesLoading, refetch: refetchTypes } = useQuery(
    'types-document',
    typeDocumentService.listerTypesDocument,
    {
      onError: () => setError('Erreur lors du chargement des types de document')
    }
  );

  // Charger une déclaration existante
  const { data: existingDeclaration, isLoading: declarationLoading } = useQuery(
    ['declaration', id],
    () => declarationService.getDeclarationDetail(id),
    {
      enabled: !!id,
      onSuccess: (data) => {
        setFormData({
          typeDocumentId: data.typeDocument?.id || '',
          numeroDocument: data.numeroDocument || '',
          datePerte: data.datePerte ? data.datePerte.split('T')[0] : '',
          lieuPerte: data.lieuPerte || '',
          circonstances: data.circonstances || '',
          declarant: {
            nom: data.declarant?.nom || '',
            prenom: data.declarant?.prenom || '',
            dateNaissance: data.declarant?.dateNaissance ? data.declarant.dateNaissance.split('T')[0] : '',
            lieuNaissance: data.declarant?.lieuNaissance || '',
            adresse: data.declarant?.adresse || '',
            telephone: data.declarant?.telephone || '',
            email: data.declarant?.email || '',
            numCarteIdentite: data.declarant?.numCarteIdentite || '',
            numNina: data.declarant?.numNina || '',
            numPassePort: data.declarant?.numPassePort || ''
          },
          statut: data.statut || 'ENREGISTREE'
        });
      },
      onError: () => setError('Erreur lors du chargement de la déclaration')
    }
  );

  // Mutation pour créer un type de document
  const createTypeMutation = useMutation(
    (libelle) => typeDocumentService.creerTypeDocument({ libelle }),
    {
      onSuccess: (response) => {
        refetchTypes();
        setCreateTypeDialogOpen(false);
        setNewTypeName('');
        if (response.id) {
          setFormData(prev => ({ ...prev, typeDocumentId: response.id }));
        }
      },
      onError: (error) => {
        setError(error.response?.data?.erreur || 'Erreur lors de la création du type');
      }
    }
  );

  // Mutation pour créer/modifier une déclaration
  const mutation = useMutation(
    (data) => {
      if (id) {
        return declarationService.modifierDeclaration(id, data);
      } else {
        return declarationService.creerDeclaration(data);
      }
    },
    {
      onSuccess: () => {
        setSuccess(id ? 'Déclaration modifiée avec succès!' : 'Déclaration créée avec succès! Un mail de Confirmation vous sera envoyé si possible');
        queryClient.invalidateQueries(['declarations']);
        setTimeout(() => navigate('/declarations'), 2000);
      },
      onError: (error) => {
        setError(error.response?.data?.erreur || 'Erreur lors de la sauvegarde');
      }
    }
  );

  // Recherche par email, NINA, Passeport, Carte d'identité
  const handleSearch = () => {
    const { email, numNina, numPassePort, numCarteIdentite } = searchData;

    if (!email && !numNina && !numPassePort && !numCarteIdentite) {
      setError('Veuillez saisir au moins un identifiant pour rechercher');
      return;
    }

    declarationService.rechercherDeclarant({
      email: email || "",
      numNina: numNina || "",
      numPassePort: numPassePort || "",
      numCarteIdentite: numCarteIdentite || ""
    })
    .then((data) => {
      if (data) {
        const declarant = data;
        setFormData(prev => ({
          ...prev,
          declarant: {
            nom: declarant.nom || '',
            prenom: declarant.prenom || '',
            dateNaissance: declarant.dateNaissance ? declarant.dateNaissance.split('T')[0] : '',
            lieuNaissance: declarant.lieuNaissance || '',
            adresse: declarant.adresse || '',
            telephone: declarant.telephone || '',
            email: declarant.email || '',
            numCarteIdentite: declarant.numCarteIdentite || '',
            numNina: declarant.numNina || '',
            numPassePort: declarant.numPassePort || ''
          },
          typeDocumentId: '',
          numeroDocument: '',
          datePerte: '',
          lieuPerte: '',
          circonstances: ''
        }));
        setSearchDialogOpen(false);
        setSuccess('Ancien déclarant trouvé. Vous pouvez créer une nouvelle déclaration.');
      } else {
        setError('Aucun déclarant trouvé avec ces informations');
      }
    })
    .catch(() => setError('Erreur lors de la recherche du déclarant'));
  };

  const handleNext = () => {
    if (activeStep === 0 && !validateStep1()) return;
    if (activeStep === 1 && !validateStep2()) return;
    setActiveStep(prev => prev + 1);
    setError('');
  };

  const handleBack = () => {
    setActiveStep(prev => prev - 1);
    setError('');
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    if (name.startsWith('declarant.')) {
      const field = name.split('.')[1];
      setFormData(prev => ({ ...prev, declarant: { ...prev.declarant, [field]: value } }));
    } else {
      setFormData(prev => ({ ...prev, [name]: value }));
    }
  };

  const handleCreateType = () => {
    if (newTypeName.trim()) {
      createTypeMutation.mutate(newTypeName.trim());
    } else {
      setError('Veuillez saisir un nom pour le type de document');
    }
  };

  const validateStep1 = () => {
    if (!formData.typeDocumentId) { setError('Le type de document est obligatoire'); return false; }
    if (!formData.numeroDocument?.trim()) { setError('Le numéro du document est obligatoire'); return false; }
    if (!formData.datePerte) { setError('La date de perte est obligatoire'); return false; }
    if (!formData.lieuPerte?.trim()) { setError('Le lieu de perte est obligatoire'); return false; }
    return true;
  };

  const validateStep2 = () => {
    if (!formData.declarant.nom?.trim()) { setError('Le nom du déclarant est obligatoire'); return false; }
    if (!formData.declarant.prenom?.trim()) { setError('Le prénom du déclarant est obligatoire'); return false; }
    if (!formData.declarant.telephone?.trim()) { setError('Le téléphone du déclarant est obligatoire'); return false; }
    return true;
  };

  const handleSubmit = () => {
    if (!validateStep1() || !validateStep2()) { setActiveStep(0); return; }
    const selectedType = typesDocument?.find(type => type.id === formData.typeDocumentId);
    const submissionData = {
      typeDocument: selectedType,
      numeroDocument: formData.numeroDocument,
      datePerte: formData.datePerte,
      lieuPerte: formData.lieuPerte,
      circonstances: formData.circonstances,
      declarant: formData.declarant,
      statut: formData.statut
    };
    mutation.mutate(submissionData);
  };

  if (declarationLoading) {
    return (
      <Container>
        <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '50vh' }}>
          <CircularProgress />
          <Typography sx={{ ml: 2 }}>Chargement de la déclaration...</Typography>
        </Box>
      </Container>
    );
  }

  // Render étapes
  const renderStep1 = () => (
    <Grid container spacing={3}>
      <Grid item xs={12}>
        <FormControl fullWidth required error={!formData.typeDocumentId && activeStep === 0}>
          <InputLabel>Type de document *</InputLabel>
          <Select
            value={formData.typeDocumentId}
            onChange={(e) => setFormData(prev => ({ ...prev, typeDocumentId: e.target.value }))}
            disabled={typesLoading}
          >
            {typesLoading ? (
              <MenuItem disabled>Chargement des types...</MenuItem>
            ) : typesDocument?.length > 0 ? (
              typesDocument.map((type) => (
                <MenuItem key={type.id} value={type.id}>{type.libelleTypeDocument || type.libelle}</MenuItem>
              ))
            ) : (
              <MenuItem disabled>Aucun type disponible</MenuItem>
            )}
          </Select>
        </FormControl>
        <Box sx={{ mt: 1, textAlign: 'right' }}>
          <Button size="small" startIcon={<Add />} onClick={() => setCreateTypeDialogOpen(true)} disabled={typesLoading}>
            Ajouter un type de document
          </Button>
        </Box>
      </Grid>
      <Grid item xs={12} md={6}>
        <TextField fullWidth label="Numéro du document *" name="numeroDocument" value={formData.numeroDocument} onChange={handleChange} required />
      </Grid>
      <Grid item xs={12} md={6}>
        <TextField fullWidth label="Date de perte *" name="datePerte" type="date" InputLabelProps={{ shrink: true }} value={formData.datePerte} onChange={handleChange} required />
      </Grid>
      <Grid item xs={12}>
        <TextField fullWidth label="Lieu de perte" name="lieuPerte" value={formData.lieuPerte} onChange={handleChange} />
      </Grid>
      <Grid item xs={12}>
        <TextField fullWidth label="Circonstances" name="circonstances" multiline rows={4} value={formData.circonstances} onChange={handleChange} />
      </Grid>
    </Grid>
  );

  const renderStep2 = () => (
    <Grid container spacing={3}>
      <Grid item xs={12} md={6}><TextField fullWidth label="Nom *" name="declarant.nom" value={formData.declarant.nom} onChange={handleChange} required /></Grid>
      <Grid item xs={12} md={6}><TextField fullWidth label="Prénom *" name="declarant.prenom" value={formData.declarant.prenom} onChange={handleChange} required /></Grid>
      <Grid item xs={12} md={6}><TextField fullWidth label="Date de naissance" name="declarant.dateNaissance" type="date" InputLabelProps={{ shrink: true }} value={formData.declarant.dateNaissance} onChange={handleChange} /></Grid>
      <Grid item xs={12} md={6}><TextField fullWidth label="Lieu de naissance" name="declarant.lieuNaissance" value={formData.declarant.lieuNaissance} onChange={handleChange} /></Grid>
      <Grid item xs={12} md={4}><TextField fullWidth label="Numéro Carte d'Identité" name="declarant.numCarteIdentite" value={formData.declarant.numCarteIdentite} onChange={handleChange} /></Grid>
      <Grid item xs={12} md={4}><TextField fullWidth label="Numéro NINA" name="declarant.numNina" value={formData.declarant.numNina} onChange={handleChange} /></Grid>
      <Grid item xs={12} md={4}><TextField fullWidth label="Numéro Passeport" name="declarant.numPassePort" value={formData.declarant.numPassePort} onChange={handleChange} /></Grid>
      <Grid item xs={12}><TextField fullWidth label="Adresse" name="declarant.adresse" multiline rows={2} value={formData.declarant.adresse} onChange={handleChange} /></Grid>
      <Grid item xs={12} md={6}><TextField fullWidth label="Téléphone *" name="declarant.telephone" value={formData.declarant.telephone} onChange={handleChange} required /></Grid>
      <Grid item xs={12} md={6}><TextField fullWidth label="Email" name="declarant.email" type="email" value={formData.declarant.email} onChange={handleChange} /></Grid>
      {(isAdmin || isSuperviseur || isAgent) && (
        <Grid item xs={12} md={6}>
          <FormControl fullWidth>
            <InputLabel>Statut</InputLabel>
            <Select value={formData.statut} onChange={(e) => setFormData(prev => ({ ...prev, statut: e.target.value }))}>
              <MenuItem value="ENREGISTREE">Enregistrée</MenuItem>
              <MenuItem value="VALIDEE">Validée</MenuItem>
              <MenuItem value="REJETEE">Rejetée</MenuItem>
            </Select>
          </FormControl>
        </Grid>
      )}
    </Grid>
  );

  const renderStep3 = () => {
    const selectedType = typesDocument?.find(type => type.id === formData.typeDocumentId);
    return (
      <Box>
        <Typography variant="h6" gutterBottom color="primary">Récapitulatif</Typography>
        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <Paper sx={{ p: 2 }}><Description sx={{ mr: 1 }} /> <Typography variant="h6">Document perdu</Typography>
              <Typography><strong>Type:</strong> {selectedType?.libelleTypeDocument || selectedType?.libelle || 'Non spécifié'}</Typography>
              <Typography><strong>Numéro:</strong> {formData.numeroDocument}</Typography>
              <Typography><strong>Date:</strong> {formData.datePerte}</Typography>
              <Typography><strong>Lieu:</strong> {formData.lieuPerte}</Typography>
              {formData.circonstances && <Typography><strong>Circonstances:</strong> {formData.circonstances}</Typography>}
            </Paper>
          </Grid>
          <Grid item xs={12} md={6}>
            <Paper sx={{ p: 2 }}><Person sx={{ mr: 1 }} /> <Typography variant="h6">Déclarant</Typography>
              <Typography><strong>Nom:</strong> {formData.declarant.nom} {formData.declarant.prenom}</Typography>
              {formData.declarant.numCarteIdentite && <Typography><strong>Carte d'identité:</strong> {formData.declarant.numCarteIdentite}</Typography>}
              {formData.declarant.numNina && <Typography><strong>NINA:</strong> {formData.declarant.numNina}</Typography>}
              {formData.declarant.numPassePort && <Typography><strong>Passeport:</strong> {formData.declarant.numPassePort}</Typography>}
              <Typography><strong>Téléphone:</strong> {formData.declarant.telephone}</Typography>
              {formData.declarant.email && <Typography><strong>Email:</strong> {formData.declarant.email}</Typography>}
              {formData.declarant.adresse && <Typography><strong>Adresse:</strong> {formData.declarant.adresse}</Typography>}
            </Paper>
          </Grid>
        </Grid>
      </Box>
    );
  };

  return (
    <Container maxWidth="lg">
      <Box sx={{ py: 4 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 4 }}>
          <Button startIcon={<ArrowBack />} onClick={() => navigate('/declarations')} sx={{ mr: 2 }}>Retour</Button>
          <Typography variant="h4" sx={{ flexGrow: 1 }}>{id ? 'Modifier la déclaration' : 'Nouvelle déclaration'}</Typography>
          {!id && <Button variant="outlined" startIcon={<Search />} onClick={() => setSearchDialogOpen(true)}>Rechercher un déclarant</Button>}
        </Box>

        {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}
        {success && <Alert severity="success" sx={{ mb: 3 }}>{success}</Alert>}

        <Stepper activeStep={activeStep} sx={{ mb: 4 }}>
          {steps.map(label => <Step key={label}><StepLabel>{label}</StepLabel></Step>)}
        </Stepper>

        <Card>
          <CardContent sx={{ py: 4 }}>
            {activeStep === 0 && renderStep1()}
            {activeStep === 1 && renderStep2()}
            {activeStep === 2 && renderStep3()}

            <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 4 }}>
              <Button onClick={handleBack} disabled={activeStep === 0 || mutation.isLoading}>Retour</Button>
              <Button variant="contained" onClick={activeStep < steps.length - 1 ? handleNext : handleSubmit} disabled={mutation.isLoading}>
                {mutation.isLoading ? 'Enregistrement...' : (activeStep < steps.length - 1 ? 'Suivant' : (id ? 'Modifier' : 'Créer'))}
              </Button>
            </Box>
          </CardContent>
        </Card>
      </Box>

      {/* Dialogue recherche */}
      <Dialog open={searchDialogOpen} onClose={() => setSearchDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Rechercher un ancien déclarant</DialogTitle>
        <DialogContent>
          <TextField fullWidth label="Email" value={searchData.email} onChange={(e) => setSearchData(prev => ({ ...prev, email: e.target.value }))} sx={{ mb: 2 }} />
          <TextField fullWidth label="Numéro NINA" value={searchData.numNina} onChange={(e) => setSearchData(prev => ({ ...prev, numNina: e.target.value }))} sx={{ mb: 2 }} />
          <TextField fullWidth label="Numéro Passeport" value={searchData.numPassePort} onChange={(e) => setSearchData(prev => ({ ...prev, numPassePort: e.target.value }))} sx={{ mb: 2 }} />
          <TextField fullWidth label="Numéro Carte d'identité" value={searchData.numCarteIdentite} onChange={(e) => setSearchData(prev => ({ ...prev, numCarteIdentite: e.target.value }))} sx={{ mb: 2 }} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setSearchDialogOpen(false)}>Annuler</Button>
          <Button variant="contained" onClick={handleSearch}>Rechercher</Button>
        </DialogActions>
      </Dialog>

      {/* Dialogue création type */}
      <Dialog open={createTypeDialogOpen} onClose={() => setCreateTypeDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Nouveau type de document</DialogTitle>
        <DialogContent>
          <TextField autoFocus fullWidth label="Nom du type" value={newTypeName} onChange={(e) => setNewTypeName(e.target.value)} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCreateTypeDialogOpen(false)}>Annuler</Button>
          <Button onClick={handleCreateType} variant="contained" disabled={!newTypeName.trim() || createTypeMutation.isLoading}>
            {createTypeMutation.isLoading ? 'Création...' : 'Créer'}
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default DeclarationForm;
