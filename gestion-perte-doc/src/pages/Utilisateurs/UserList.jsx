import React, { useState } from 'react';
import {
  Box,
  Typography,
  Button,
  Card,
  Chip,
  IconButton,
  Menu,
  MenuItem,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormControl,
  InputLabel,
  Select,
  Alert
} from '@mui/material';
import { MoreVert, PersonAdd, PersonOff, AdminPanelSettings, Delete } from '@mui/icons-material';
import { DataGrid } from '@mui/x-data-grid';
import { frFR } from '@mui/x-data-grid/locales';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { utilisateurService } from '../../services/utilisateurService';
import { roleService } from '../../services/roleService';
import { usePermissions } from '../../hooks/usePermissions';

const StatutChip = ({ actif }) => (
  <Chip label={actif ? 'Actif' : 'Inactif'} color={actif ? 'success' : 'default'} size="small" />
);

const UserRoleChip = ({ roles }) => {
  const getRoleColor = (role) => {
    if (role.includes('ADMIN')) return 'error';
    if (role.includes('SUPERVISEUR')) return 'warning';
    if (role.includes('AGENT')) return 'primary';
    return 'default';
  };

  return (
    <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>
      {roles?.map((role, index) => (
        <Chip
          key={index}
          label={role.replace('ROLE_', '')}
          size="small"
          color={getRoleColor(role)}
          variant="outlined"
        />
      ))}
    </Box>
  );
};

const UserList = () => {
  const queryClient = useQueryClient();
  const { user: currentUser, isAdmin } = usePermissions();

  const [anchorEl, setAnchorEl] = useState(null);
  const [selectedUser, setSelectedUser] = useState(null);
  const [activationDialogOpen, setActivationDialogOpen] = useState(false);
  const [desactivationDialogOpen, setDesactivationDialogOpen] = useState(false);
  const [transferAdminDialogOpen, setTransferAdminDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [selectedRoles, setSelectedRoles] = useState([]);
  const [newAdminId, setNewAdminId] = useState('');

  // -------------------- QUERIES --------------------
  const { data: utilisateurs, isLoading } = useQuery('utilisateurs', utilisateurService.listerTousUtilisateurs);
  const { data: roles } = useQuery('roles', roleService.listerRoles);
  const { data: utilisateursActifs } = useQuery(
    'utilisateurs-actifs',
    utilisateurService.listerUtilisateursActifs,
    { enabled: transferAdminDialogOpen }
  );

  // -------------------- MUTATIONS --------------------
  const activationMutation = useMutation(
    ({ id, roleIds }) => utilisateurService.activerUtilisateur(id, roleIds),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('utilisateurs');
        setActivationDialogOpen(false);
        setSelectedRoles([]);
      }
    }
  );

  const desactivationMutation = useMutation(
    (id) => utilisateurService.desactiverUtilisateur(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('utilisateurs');
        setDesactivationDialogOpen(false);
      }
    }
  );

  const transferAdminMutation = useMutation(
    ({ ancienAdminId, nouveauAdminId }) => utilisateurService.transfererRoleAdmin(ancienAdminId, nouveauAdminId),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('utilisateurs');
        setTransferAdminDialogOpen(false);
        setNewAdminId('');
      }
    }
  );

  const deleteMutation = useMutation(
    (id) => utilisateurService.supprimerUtilisateur(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('utilisateurs');
        setDeleteDialogOpen(false);
      }
    }
  );

  // -------------------- HANDLERS --------------------
  const handleMenuOpen = (event, user) => {
    setAnchorEl(event.currentTarget);
    setSelectedUser(user);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
    setSelectedUser(null);
  };

  const handleActivation = () => {
    if (selectedUser && selectedRoles.length > 0) {
      activationMutation.mutate({ id: selectedUser.id, roleIds: selectedRoles });
    }
  };

  const handleDesactivation = () => {
    if (selectedUser) desactivationMutation.mutate(selectedUser.id);
  };

  const handleTransferAdmin = () => {
    if (selectedUser && newAdminId) {
      transferAdminMutation.mutate({ ancienAdminId: selectedUser.id, nouveauAdminId: newAdminId });
    }
  };

  const handleDelete = () => {
    if (selectedUser) deleteMutation.mutate(selectedUser.id);
  };

  const canModifyUser = (user) => {
    if (user.id === currentUser.id) return false;
    if (user.roles?.includes('ROLE_ADMIN') && !currentUser.roles?.includes('ROLE_ADMIN')) return false;
    return true;
  };

  // -------------------- COLUMNS --------------------
  const columns = [
    { field: 'matricule', headerName: 'Matricule', width: 120 },
    { field: 'nomComplet', headerName: 'Nom Complet', width: 200, valueGetter: (params) => `${params.row.prenom} ${params.row.nom}` },
    { field: 'email', headerName: 'Email', width: 200 },
    { field: 'postePoliceNom', headerName: 'Poste', width: 150 },
    { field: 'actif', headerName: 'Statut', width: 100, renderCell: (params) => <StatutChip actif={params.value} /> },
    { field: 'roles', headerName: 'Rôles', width: 200, renderCell: (params) => <UserRoleChip roles={params.value} /> },
    {
      field: 'actions',
      headerName: 'Actions',
      width: 80,
      sortable: false,
      renderCell: (params) => (
        <IconButton size="small" onClick={(e) => handleMenuOpen(e, params.row)} disabled={!canModifyUser(params.row)}>
          <MoreVert />
        </IconButton>
      )
    }
  ];

  return (
    <Box>
      <Typography variant="h4" sx={{ fontWeight: 'bold', mb: 3 }}>Liste des Utilisateurs</Typography>

      <Card>
        <DataGrid
          rows={utilisateurs?.map(u => ({ ...u, id: u.id })) || []}
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

      {/* -------------------- MENU CONTEXTUEL -------------------- */}
      <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={handleMenuClose}>
        {!selectedUser?.actif && (
          <MenuItem onClick={() => setActivationDialogOpen(true)}>
            <PersonAdd sx={{ mr: 1 }} /> Activer et attribuer rôles
          </MenuItem>
        )}

        {selectedUser?.actif && !selectedUser?.roles?.includes('ROLE_ADMIN') && (
          <MenuItem onClick={() => setDesactivationDialogOpen(true)}>
            <PersonOff sx={{ mr: 1 }} /> Désactiver
          </MenuItem>
        )}

        {isAdmin && selectedUser?.actif && selectedUser?.roles?.includes('ROLE_ADMIN') && (
          <MenuItem onClick={() => setTransferAdminDialogOpen(true)}>
            <AdminPanelSettings sx={{ mr: 1 }} /> Transférer rôle admin
          </MenuItem>
        )}

        {selectedUser?.actif && !selectedUser?.roles?.includes('ROLE_ADMIN') && (
          <MenuItem onClick={() => setDeleteDialogOpen(true)} sx={{ color: 'error.main' }}>
            <Delete sx={{ mr: 1 }} /> Supprimer définitivement
          </MenuItem>
        )}
      </Menu>

      {/* -------------------- DIALOGS -------------------- */}
      {/* Activation, Désactivation, Transfert Admin, Suppression */}
      {/* ... identical code aux dialogues existants ... */}
    </Box>
  );
};

export default UserList;
