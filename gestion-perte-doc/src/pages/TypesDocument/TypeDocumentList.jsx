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
  Chip
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
      },
      onError: (error) => {
        console.error('Erreur modification:', error);
      }
    }
  );

  const deleteMutation = useMutation(
    (id) => typeDocumentService.supprimerTypeDocument(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('types-document');
        setDeleteDialogOpen(false);
        setSelectedType(null); // réinitialiser après suppression
      },
      onError: (error) => {
        console.error('Erreur suppression:', error);
      }
    }
  );

  // --- Handlers ---
  const handleMenuOpen = (event, typeDoc) => {
    console.log('Menu ouvert pour typeDoc:', typeDoc);
    setAnchorEl(event.currentTarget);
    setSelectedType(typeDoc); // garder selectedType jusqu'à action
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
    // Ne pas réinitialiser selectedType ici
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
          {createMutation.isError && (
            <Alert severity="error" sx={{ mt: 2 }}>
              {createMutation.error.response?.data?.erreur || 'Erreur lors de la création'}
            </Alert>
          )}
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
          {updateMutation.isError && (
            <Alert severity="error" sx={{ mt: 2 }}>
              Erreur: {updateMutation.error.response?.data?.erreur || updateMutation.error.message || 'Erreur lors de la modification'}
            </Alert>
          )}
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
    </Box>
  );
};

export default TypeDocumentList;
