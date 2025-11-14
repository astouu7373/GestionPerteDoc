import React, { useState, useMemo } from 'react';
import {
  Box, Typography, Button, Card, TextField,
  IconButton, Menu, MenuItem, Select, Alert
} from '@mui/material';
import { Add, MoreVert, Edit, Delete, Visibility, Restore, Download } from '@mui/icons-material';
import { DataGrid } from '@mui/x-data-grid';
import { frFR } from '@mui/x-data-grid/locales';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { declarationService } from '../../services/declarationService';
import { usePermissions } from '../../hooks/usePermissions';
import { useNavigate } from 'react-router-dom';
import { format } from 'date-fns';

const DeclarationList = ({ type = 'actives', scope = 'own' }) => {
  const { user, canDeletePermanently, canRestoreDeclaration } = usePermissions();
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  const [anchorEl, setAnchorEl] = useState(null);
  const [selectedDeclaration, setSelectedDeclaration] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');

  const getFetchFunction = () => {
    if (type === 'actives') {
      return scope === 'all'
        ? declarationService.listerDeclarationsActivesPoste
        : declarationService.listerDeclarationsActives;
    }
    if (type === 'supprimees') {
      return scope === 'all'
        ? declarationService.listerDeclarationsSupprimeesPoste
        : declarationService.listerDeclarationsSupprimees;
    }
    return declarationService.listerDeclarations;
  };

  const { data: declarationsData, isLoading, error } = useQuery(
    ['declarations', type, scope, user?.id],
    getFetchFunction(),
    { enabled: !!user }
  );

  const declarations = declarationsData?.declarations || [];

  // 🔹 Mutations
  const deleteMutation = useMutation(
    (id) => declarationService.supprimerDeclaration(id),
    { onSuccess: () => queryClient.invalidateQueries(['declarations', type, scope, user?.id]) }
  );

  const restoreMutation = useMutation(
    (id) => declarationService.restaurerDeclaration(id),
    { onSuccess: () => queryClient.invalidateQueries(['declarations', type, scope, user?.id]) }
  );

  const permanentDeleteMutation = useMutation(
    (id) => declarationService.supprimerDefinitivement(id),
    { onSuccess: () => queryClient.invalidateQueries(['declarations', type, scope, user?.id]) }
  );

  const statutMutation = useMutation(
    ({ id, nouveauStatut }) => declarationService.changerStatut(id, nouveauStatut),
    { onSuccess: () => queryClient.invalidateQueries(['declarations', type, scope, user?.id]) }
  );

  const handleMenuOpen = (event, declaration) => {
    setAnchorEl(event.currentTarget);
    setSelectedDeclaration(declaration);
  };
  const handleMenuClose = () => {
    setAnchorEl(null);
    setSelectedDeclaration(null);
  };

  const handleDelete = () => selectedDeclaration && deleteMutation.mutate(selectedDeclaration.id);
  const handleRestore = () => selectedDeclaration && restoreMutation.mutate(selectedDeclaration.id);
  const handlePermanentDelete = () =>
    selectedDeclaration && permanentDeleteMutation.mutate(selectedDeclaration.id);

  const handleViewDetails = () => {
    if (selectedDeclaration) {
      navigate(`/declarations/${selectedDeclaration.id}`);
      handleMenuClose();
    }
  };
  const handleEdit = () => {
    if (selectedDeclaration) {
      navigate(`/declarations/${selectedDeclaration.id}/modifier`);
      handleMenuClose();
    }
  };
  const handleGeneratePdf = async () => {
    if (!selectedDeclaration) return;
    try {
      const pdfBlob = await declarationService.genererPdf(selectedDeclaration.id);
      const url = window.URL.createObjectURL(pdfBlob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `declaration-${selectedDeclaration.numeroReference}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      console.error('Erreur génération PDF:', err);
    }
    handleMenuClose();
  };

  const filteredDeclarations = useMemo(() => {
    if (!searchTerm) return declarations;
    return declarations.filter((decl) =>
      decl.numeroReference?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      decl.declarant?.nom?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      decl.declarant?.prenom?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      decl.typeDocumentLibelle?.toLowerCase().includes(searchTerm.toLowerCase())
    );
  }, [declarations, searchTerm]);

  const columns = [
    { field: 'numeroReference', headerName: 'Référence', width: 150 },
    {
      field: 'declarant',
      headerName: 'Déclarant',
      width: 200,
      valueGetter: (params) => {
        const d = params.row.declarant;
        return d ? `${d.prenom || ''} ${d.nom || ''}`.trim() : 'Utilisateur supprimé';
      },
    },
    { field: 'typeDocumentLibelle', headerName: 'Type de document', width: 180 },
    {
      field: 'dateDeclaration',
      headerName: 'Date de déclaration',
      width: 170,
      valueFormatter: (params) =>
        params.value ? format(new Date(params.value), 'dd/MM/yyyy') : '',
    },
    {
      field: 'statut',
      headerName: 'Statut',
      width: 160,
      renderCell: (params) => (
        <Select
          size="small"
          value={params.row.statut || 'ENREGISTREE'}
          onChange={(e) =>
            statutMutation.mutate({ id: params.row.id, nouveauStatut: e.target.value })
          }
        >
          <MenuItem value="ENREGISTREE">Enregistrée</MenuItem>
          <MenuItem value="VALIDEE">Validée</MenuItem>
          <MenuItem value="REJETEE">Rejetée</MenuItem>
          <MenuItem value="BROUILLON">Brouillon</MenuItem>
        </Select>
      ),
    },
    {
      field: 'actions',
      headerName: 'Actions',
      width: 100,
      sortable: false,
      renderCell: (params) => (
        <IconButton size="small" onClick={(e) => handleMenuOpen(e, params.row)}>
          <MoreVert />
        </IconButton>
      ),
    },
  ];

  const getTitle = () => {
    if (type === 'actives' && scope === 'own') return 'Mes Déclarations Actives';
    if (type === 'actives' && scope === 'all') return 'Toutes les Déclarations Actives';
    if (type === 'supprimees' && scope === 'own') return 'Mes Déclarations Supprimées';
    if (type === 'supprimees' && scope === 'all') return 'Toutes les Déclarations Supprimées';
    return 'Déclarations';
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4">{getTitle()}</Typography>
        {type === 'actives' && (
          <Button
            variant="contained"
            startIcon={<Add />}
            onClick={() => navigate('/declarations/nouvelle')}
          >
            Nouvelle Déclaration
          </Button>
        )}
      </Box>

      <TextField
        fullWidth
        placeholder="Rechercher..."
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
        sx={{ mb: 2 }}
      />

      {error && <Alert severity="error">Erreur lors du chargement des déclarations</Alert>}

      <Card>
        <DataGrid
          rows={filteredDeclarations}
          columns={columns}
          loading={isLoading}
          autoHeight
          pageSize={10}
          rowsPerPageOptions={[10, 25, 50]}
          localeText={{
            ...frFR.components.MuiDataGrid.defaultProps.localeText,
            noRowsLabel: 'Aucune ligne',
            footerRowSelected: (count) => `${count.toLocaleString()} ligne(s) sélectionnée(s)`,
            toolbarFilters: 'Filtres',
            toolbarExport: 'Exporter',
            columnsPanelTextFieldLabel: 'Rechercher une colonne',
            columnsPanelShowAllButton: 'Tout afficher',
            columnsPanelHideAllButton: 'Tout masquer',
            filterPanelAddFilter: 'Ajouter un filtre',
            filterPanelDeleteIconLabel: 'Supprimer',
            filterPanelOperators: 'Opérateurs',
            filterPanelOperatorAnd: 'Et',
            filterPanelOperatorOr: 'Ou',
            filterPanelColumns: 'Colonnes',
            filterPanelInputLabel: 'Valeur',
            filterPanelInputPlaceholder: 'Valeur du filtre',
            columnsPanelSortIconLabel: 'Trier',
          }}
        />
      </Card>

      <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={handleMenuClose}>
        <MenuItem onClick={handleViewDetails}>
          <Visibility sx={{ mr: 1 }} /> Voir les détails
        </MenuItem>
        {type === 'actives' && (
          <MenuItem onClick={handleEdit}>
            <Edit sx={{ mr: 1 }} /> Modifier
          </MenuItem>
        )}
        {type === 'actives' && (
          <MenuItem onClick={handleDelete}>
            <Delete sx={{ mr: 1 }} /> Supprimer
          </MenuItem>
        )}
        {type === 'supprimees' && canRestoreDeclaration && (
          <MenuItem onClick={handleRestore}>
            <Restore sx={{ mr: 1 }} /> Restaurer
          </MenuItem>
        )}
        {type === 'supprimees' && canDeletePermanently && (
          <MenuItem onClick={handlePermanentDelete} sx={{ color: 'error.main' }}>
            <Delete sx={{ mr: 1 }} /> Supprimer définitivement
          </MenuItem>
        )}
        <MenuItem onClick={handleGeneratePdf}>
          <Download sx={{ mr: 1 }} /> Télécharger en PDF
        </MenuItem>
      </Menu>

      {deleteMutation.isError && <Alert severity="error">Erreur lors de la suppression</Alert>}
      {restoreMutation.isError && <Alert severity="error">Erreur lors de la restauration</Alert>}
      {permanentDeleteMutation.isError && (
        <Alert severity="error">Erreur lors de la suppression définitive</Alert>
      )}
    </Box>
  );
};

export default DeclarationList;
