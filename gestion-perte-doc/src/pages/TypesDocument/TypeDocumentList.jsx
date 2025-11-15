import React, { useState } from 'react';
import {
  Box,
  Typography,
  Button,
  Card,
  IconButton,
  Menu,
  MenuItem,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Grid,
  Alert,
  Chip,
  Snackbar // Ajout pour les notifications
} from '@mui/material';
import { Add, MoreVert, Edit, Delete } from '@mui/icons-material';
import { DataGrid } from '@mui/x-data-grid';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { typeDocumentService } from '../../services/typeDocumentService';
import { usePermissions } from '../../hooks/usePermissions';

const TypeDocumentList = () => {
  const queryClient = useQueryClient();
  const { canManageTypesDocument } = usePermissions();

  const [anchorEl, setAnchorEl] = useState(null);
  const [selectedType, setSelectedType] = useState(null);
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' });

  const [formData, setFormData] = useState({
    libelleTypeDocument: '',
    codeTypeDocument: ''
  });

  const { data: typesDocument, isLoading, error } = useQuery(
    'types-document',
    typeDocumentService.listerTypesDocument
  );

  // --- Assure que chaque ligne a un id ---
  const rowsWithId = (typesDocument || []).map(doc => ({
    ...doc,
    id: doc.id
  }));

  // --- Mutations ---
  const createMutation = useMutation(
    (data) => typeDocumentService.creerTypeDocument(data),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('types-document');
        setCreateDialogOpen(false);
        resetForm();
        showSnackbar('Type de document créé avec succès', 'success');
      },
      onError: (error) => {
        showSnackbar(error.response?.data?.erreur || 'Erreur lors de la création', 'error');
      }
    }
  );

  const updateMutation = useMutation(
    ({ id, data }) => typeDocumentService.mettreAJourTypeDocument(id, data),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('types-document');
        setEditDialogOpen(false);
        resetForm();
        showSnackbar('Type de document modifié avec succès', 'success');
      },
      onError: (error) => {
        showSnackbar(error.response?.data?.erreur || 'Erreur lors de la modification', 'error');
      }
    }
  );

  const deleteMutation = useMutation(
    (id) => typeDocumentService.supprimerTypeDocument(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('types-document');
        setDeleteDialogOpen(false);
        setSelectedType(null);
        showSnackbar('Type de document supprimé avec succès', 'success');
      },
      onError: (error) => {
        // Gestion spécifique du message de suppression interdit
        const errorMessage = error.response?.data?.message === "Vous ne pouvez pas supprimer un document" 
          ? "Vous ne pouvez pas supprimer un document" 
          : error.response?.data?.erreur || 'Erreur lors de la suppression';
        
        showSnackbar(errorMessage, 'error');
        
        // Fermer le dialogue si l'erreur est liée aux permissions
        if (error.response?.data?.message === "Vous ne pouvez pas supprimer un document") {
          setDeleteDialogOpen(false);
        }
      }
    }
  );

  // --- Fonction pour afficher les notifications ---
  const showSnackbar = (message, severity = 'success') => {
    setSnackbar({ open: true, message, severity });
  };

  // --- Handlers ---
  const handleMenuOpen = (event, typeDoc) => {
    setAnchorEl(event.currentTarget);
    setSelectedType(typeDoc);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
  };

  const resetForm = () => {
    setFormData({
      libelleTypeDocument: '',
      codeTypeDocument: ''
    });
  };

  const handleCreateOpen = () => {
    resetForm();
    setCreateDialogOpen(true);
  };

  const handleEditOpen = () => {
    if (selectedType) {
      setFormData({
        libelleTypeDocument: selectedType.libelleTypeDocument,
        codeTypeDocument: selectedType.codeTypeDocument || ''
      });
      setEditDialogOpen(true);
    }
    handleMenuClose();
  };

  const handleDeleteOpen = () => {
    setDeleteDialogOpen(true);
    handleMenuClose();
  };

  const handleCreate = () => {
    const payload = {
      libelleTypeDocument: formData.libelleTypeDocument,
      codeTypeDocument: formData.codeTypeDocument.trim() || undefined
    };
    createMutation.mutate(payload);
  };

  const handleUpdate = () => {
    if (!selectedType?.id) return;
    const payload = {
      libelleTypeDocument: formData.libelleTypeDocument,
      codeTypeDocument: formData.codeTypeDocument.trim() || undefined
    };
    updateMutation.mutate({ id: selectedType.id, data: payload });
  };

  const handleDelete = () => {
    if (!selectedType?.id) {
      console.error('Aucun ID défini pour la suppression', selectedType);
      return;
    }
    deleteMutation.mutate(selectedType.id);
  };

  // --- DataGrid Columns ---
  const columns = [
    {
      field: 'codeTypeDocument',
      headerName: 'Code',
      width: 150,
      renderCell: (params) => (
        <Chip 
          label={params.value || '—'} 
          variant="outlined" 
          color="primary"
          size="small"
        />
      )
    },
    {
      field: 'libelleTypeDocument',
      headerName: 'Libellé',
      width: 300,
      flex: 1
    },
    {
      field: 'actions',
      headerName: 'Actions',
      width: 80,
      sortable: false,
      renderCell: (params) => (
        <IconButton
          size="small"
          onClick={(e) => handleMenuOpen(e, params.row)}
        >
          <MoreVert />
        </IconButton>
      )
    }
  ];

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" sx={{ fontWeight: 'bold' }}>
          Types de Documents
        </Typography>
        {canManageTypesDocument && (
          <Button
            variant="contained"
            startIcon={<Add />}
            onClick={handleCreateOpen}
          >
            Nouveau Type
          </Button>
        )}
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          Erreur lors du chargement des types de document
        </Alert>
      )}

      <Card>
        <DataGrid
          rows={rowsWithId}
          columns={columns}
          getRowId={(row) => row.id}
          loading={isLoading}
          autoHeight
          pageSize={10}
          rowsPerPageOptions={[10, 25, 50]}
          sx={{ border: 0 }}
        />
      </Card>

      {canManageTypesDocument && (
        <Menu
          anchorEl={anchorEl}
          open={Boolean(anchorEl)}
          onClose={handleMenuClose}
        >
          <MenuItem onClick={handleEditOpen}>
            <Edit sx={{ mr: 1 }} />
            Modifier
          </MenuItem>
          <MenuItem onClick={handleDeleteOpen} sx={{ color: 'error.main' }}>
            <Delete sx={{ mr: 1 }} />
            Supprimer
          </MenuItem>
        </Menu>
      )}

      {/* Dialogues */}
      <Dialog open={createDialogOpen} onClose={() => setCreateDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Nouveau type de document</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Libellé *"
                value={formData.libelleTypeDocument}
                onChange={(e) => setFormData(prev => ({ ...prev, libelleTypeDocument: e.target.value }))}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Code"
                value={formData.codeTypeDocument}
                onChange={(e) => setFormData(prev => ({ ...prev, codeTypeDocument: e.target.value }))}
                helperText="Laissez vide pour génération automatique"
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCreateDialogOpen(false)}>Annuler</Button>
          <Button
            onClick={handleCreate}
            variant="contained"
            disabled={!formData.libelleTypeDocument.trim() || createMutation.isLoading}
          >
            {createMutation.isLoading ? 'Création...' : 'Créer'}
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={editDialogOpen} onClose={() => setEditDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Modifier le type de document</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Libellé *"
                value={formData.libelleTypeDocument}
                onChange={(e) => setFormData(prev => ({ ...prev, libelleTypeDocument: e.target.value }))}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Code"
                value={formData.codeTypeDocument}
                onChange={(e) => setFormData(prev => ({ ...prev, codeTypeDocument: e.target.value }))}
                helperText="Laissez vide pour génération automatique"
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEditDialogOpen(false)}>Annuler</Button>
          <Button
            onClick={handleUpdate}
            variant="contained"
            disabled={!formData.libelleTypeDocument.trim() || updateMutation.isLoading}
          >
            {updateMutation.isLoading ? 'Modification...' : 'Modifier'}
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
        <DialogTitle>Confirmer la suppression</DialogTitle>
        <DialogContent>
          <Typography>
            Êtes-vous sûr de vouloir supprimer le type de document "{selectedType?.libelleTypeDocument}" ?
          </Typography>
          <Alert severity="warning" sx={{ mt: 2 }}>
            Cette action est irréversible. Les déclarations utilisant ce type seront affectées.
          </Alert>
          {deleteMutation.isError && (
            <Alert severity="error" sx={{ mt: 2 }}>
              {deleteMutation.error.response?.data?.erreur || 'Vous ne pouvez pas supprimer de document'}
            </Alert>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>Annuler</Button>
          <Button
            onClick={handleDelete}
            color="error"
            variant="contained"
            disabled={deleteMutation.isLoading}
          >
            {deleteMutation.isLoading ? 'Suppression...' : 'Supprimer'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Snackbar pour les notifications */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={6000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert 
          severity={snackbar.severity} 
          onClose={() => setSnackbar({ ...snackbar, open: false })}
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default TypeDocumentList;