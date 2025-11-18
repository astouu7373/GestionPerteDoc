import React, { useState } from 'react';
import {
  Box, Card, CardContent, Typography, List, ListItem,
  Stack, Chip, Button, FormControl, InputLabel, Select,
  MenuItem, Snackbar, Alert, Grid, TextField
} from '@mui/material';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { utilisateurService } from '../../../services/utilisateurService';
import { usePermissions } from '../../../hooks/usePermissions';

// --- Fonction utilitaire pour gérer les erreurs API ---
function handleApiError(err) {
  if (err.response?.data?.erreur) return err.response.data.erreur;
  if (err.response?.status) return `Erreur ${err.response.status} : ${err.response.statusText}`;
  return err.message || 'Erreur inconnue';
}

// --- Vérifie si un utilisateur est Admin ---
function estAdmin(utilisateur) {
  return utilisateur.roles?.some(r => r.libelle === 'ROLE_ADMIN');
}

const GestionUtilisateurs = () => {
  const { user, isAdmin, isSuperviseur } = usePermissions();
  const queryClient = useQueryClient();

  const [roleSelection, setRoleSelection] = useState({});
  const [nouvelAdminId, setNouvelAdminId] = useState(null);
  const [nouveauCompte, setNouveauCompte] = useState({
    nom: '',
    prenom: '',
    email: '',
    roles: [],
    postePoliceId: ''
  });
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  // --- Requêtes pour charger les données ---
  const { data: postesPolice } = useQuery('postesPolice', utilisateurService.listerPostesPolice);
  const { data: comptesActifs } = useQuery(['utilisateurs', 'actifs'], utilisateurService.listerUtilisateursActifs);
  const { data: comptesInactifs } = useQuery(['utilisateurs', 'inactifs'], utilisateurService.listerUtilisateursInactifs);
  const { data: comptesSupprimes } = useQuery(['utilisateurs', 'supprimes'], utilisateurService.listerUtilisateursSupprimes);

  const rolesDisponibles = [
    { id: 2, libelle: 'Superviseur' },
    { id: 3, libelle: 'Agent' }
  ];
  const rolesPourActivation = isSuperviseur ? rolesDisponibles.filter(r => r.id !== 1) : rolesDisponibles;

  // --- Mutations ---
  const activerMutation = useMutation(
    ({ id, roleIds }) => utilisateurService.activerUtilisateur(id, roleIds),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['utilisateurs', 'actifs']);
        queryClient.invalidateQueries(['utilisateurs', 'inactifs']);
        setRoleSelection({});
        setSuccess('Utilisateur activé avec succès');
      },
      onError: (err) => setError(handleApiError(err))
    }
  );

  const desactiverMutation = useMutation(
    id => utilisateurService.desactiverUtilisateur(id),
    {
      onSuccess: (id) => {
        const actifs = queryClient.getQueryData(['utilisateurs', 'actifs']) || [];
        const inactifs = queryClient.getQueryData(['utilisateurs', 'inactifs']) || [];
        const utilisateur = actifs.find(u => u.id === id);
        if (utilisateur) {
          queryClient.setQueryData(['utilisateurs', 'actifs'], actifs.filter(u => u.id !== id));
          queryClient.setQueryData(['utilisateurs', 'inactifs'], [...inactifs, utilisateur]);
        }
        setSuccess('Utilisateur désactivé avec succès');
      },
      onError: (err) => setError(handleApiError(err))
    }
  );

  const supprimerSoftMutation = useMutation(
    id => utilisateurService.supprimerUtilisateurSoft(id),
    {
      onSuccess: (id) => {
        const actifs = queryClient.getQueryData(['utilisateurs', 'actifs']) || [];
        const inactifs = queryClient.getQueryData(['utilisateurs', 'inactifs']) || [];
        const supprimes = queryClient.getQueryData(['utilisateurs', 'supprimes']) || [];

        let utilisateur = actifs.find(u => u.id === id) || inactifs.find(u => u.id === id);
        if (utilisateur) {
          queryClient.setQueryData(['utilisateurs', 'actifs'], actifs.filter(u => u.id !== id));
          queryClient.setQueryData(['utilisateurs', 'inactifs'], inactifs.filter(u => u.id !== id));
          queryClient.setQueryData(['utilisateurs', 'supprimes'], [...supprimes, utilisateur]);
        }

        setSuccess('Utilisateur supprimé avec succès (soft delete). Ses déclarations sont conservées.');
      },
      onError: (err) => setError(handleApiError(err))
    }
  );
  const supprimerDefinitifMutation = useMutation(
      id => utilisateurService.supprimerUtilisateurDefinitif(id),
      {
        onSuccess: (id) => {
          const actifs = queryClient.getQueryData(['utilisateurs', 'actifs']) || [];
          const inactifs = queryClient.getQueryData(['utilisateurs', 'inactifs']) || [];
          const supprimes = queryClient.getQueryData(['utilisateurs', 'supprimes']) || [];

          let utilisateur = actifs.find(u => u.id === id) || inactifs.find(u => u.id === id);
          if (utilisateur) {
            queryClient.setQueryData(['utilisateurs', 'actifs'], actifs.filter(u => u.id !== id));
            queryClient.setQueryData(['utilisateurs', 'inactifs'], inactifs.filter(u => u.id !== id));
            queryClient.setQueryData(['utilisateurs', 'supprimes'], [...supprimes, utilisateur]);
          }

          setSuccess('Utilisateur supprimé avec succès ');
        },
        onError: (err) => setError(handleApiError(err))
      }
    );

  const restaurerMutation = useMutation(
    ({ id, roleIds }) => utilisateurService.restaurerUtilisateur(id, roleIds),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['utilisateurs', 'actifs']);
        queryClient.invalidateQueries(['utilisateurs', 'supprimes']);
        setSuccess('Utilisateur restauré avec succès');
      },
      onError: (err) => setError(handleApiError(err))
    }
  );

  const transfererAdminMutation = useMutation(
    ({ ancienAdminId, nouveauAdminId }) => utilisateurService.transfererRoleAdmin(ancienAdminId, nouveauAdminId),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['utilisateurs']);
        setNouvelAdminId(null);
        setSuccess('Rôle admin transféré avec succès');
      },
      onError: (err) => setError(handleApiError(err))
    }
  );

  const creerCompteMutation = useMutation(
    (compte) => utilisateurService.creerUtilisateur(compte),
    {
      onSuccess: (data) => {
        queryClient.invalidateQueries(['utilisateurs']);
        setSuccess(`Compte créé avec succès pour ${data.nom} ${data.prenom}. Un email a été envoyé pour activer le compte.`);
        setNouveauCompte({ nom: '', prenom: '', email: '', roles: [], postePoliceId: '' });
      },
      onError: (err) => setError(handleApiError(err))
    }
  );

  // --- Handlers ---
  const handleTransfertAdmin = () => {
    if (!nouvelAdminId) return setError('Veuillez sélectionner un nouvel administrateur');
    if (nouvelAdminId === user.id) return setError('Vous ne pouvez pas vous transférer le rôle à vous-même');
    transfererAdminMutation.mutate({ ancienAdminId: user.id, nouveauAdminId: nouvelAdminId });
  };

  const handleCreerCompte = () => {
    if (!nouveauCompte.nom || !nouveauCompte.prenom || !nouveauCompte.email || !nouveauCompte.postePoliceId) {
      return setError('Veuillez remplir tous les champs obligatoires');
    }
    if (!nouveauCompte.roles?.length) return setError('Veuillez sélectionner au moins un rôle');

    const comptePrepare = {
      ...nouveauCompte,
      roles: nouveauCompte.roles.map(r => Number(r))
    };

    creerCompteMutation.mutate(comptePrepare);
  };

  // --- Interface ---
  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom sx={{ mb: 4 }}>
        Gestion des Utilisateurs
      </Typography>

      {/* Notifications */}
      <Snackbar open={!!error} autoHideDuration={6000} onClose={() => setError(null)} anchorOrigin={{ vertical: 'top', horizontal: 'center' }}>
        <Alert onClose={() => setError(null)} severity="error">{error}</Alert>
      </Snackbar>
      <Snackbar open={!!success} autoHideDuration={6000} onClose={() => setSuccess(null)} anchorOrigin={{ vertical: 'top', horizontal: 'center' }}>
        <Alert onClose={() => setSuccess(null)} severity="success">{success}</Alert>
      </Snackbar>

      {/* --- Création compte --- */}
      {(isAdmin || isSuperviseur) && (
        <Card sx={{ mb: 4 }}>
          <CardContent>
            <Typography variant="h6" gutterBottom>Créer un nouveau compte</Typography>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={3}>
                <TextField label="Nom" fullWidth size="small" value={nouveauCompte.nom} onChange={e => setNouveauCompte({ ...nouveauCompte, nom: e.target.value })} />
              </Grid>
              <Grid item xs={12} sm={3}>
                <TextField label="Prénom" fullWidth size="small" value={nouveauCompte.prenom} onChange={e => setNouveauCompte({ ...nouveauCompte, prenom: e.target.value })} />
              </Grid>
              <Grid item xs={12} sm={3}>
                <TextField label="Email" fullWidth size="small" value={nouveauCompte.email} onChange={e => setNouveauCompte({ ...nouveauCompte, email: e.target.value })} />
              </Grid>
              <Grid item xs={12} sm={3}>
                {postesPolice?.length ? (
                  <FormControl fullWidth size="small">
                    <InputLabel>Poste Police</InputLabel>
                    <Select
                      value={nouveauCompte.postePoliceId}
                      onChange={e => setNouveauCompte({ ...nouveauCompte, postePoliceId: Number(e.target.value) })}
                      label="Poste Police"
                    >
                      {postesPolice.map(p => (
                        <MenuItem key={p.id} value={p.id}>{p.nom}</MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                ) : <Typography color="error">Aucun poste de police disponible</Typography>}
              </Grid>

              <Grid item xs={12}>
                <FormControl fullWidth size="small">
                  <InputLabel>Rôles</InputLabel>
                  <Select
                    multiple
                    value={nouveauCompte.roles}
                    onChange={e => setNouveauCompte({ ...nouveauCompte, roles: e.target.value.map(v => Number(v)) })}
                    renderValue={(selected) => selected.map(id => rolesDisponibles.find(r => r.id === id)?.libelle).join(', ')}
                  >
                    {rolesDisponibles.map(r => <MenuItem key={r.id} value={r.id}>{r.libelle}</MenuItem>)}
                  </Select>
                </FormControl>
              </Grid>

              <Grid item xs={12}>
                <Button variant="contained" color="primary" onClick={handleCreerCompte} disabled={creerCompteMutation.isLoading}>
                  {creerCompteMutation.isLoading ? 'Création...' : 'Créer'}
                </Button>
              </Grid>
            </Grid>
          </CardContent>
        </Card>
      )}

      {/* --- Transfert Admin --- */}
      {isAdmin && (
        <Card sx={{ mb: 4 }}>
          <CardContent>
            <Typography variant="h6" gutterBottom>Transfert du rôle Admin</Typography>
            <FormControl fullWidth size="small" sx={{ mb: 2 }}>
              <InputLabel>Nouvel administrateur</InputLabel>
              <Select
                value={nouvelAdminId || ''}
                onChange={(e) => setNouvelAdminId(Number(e.target.value))}
                label="Nouvel administrateur"
              >
                {comptesActifs?.filter(u => !estAdmin(u) && u.id !== user.id)
                  ?.map(u => <MenuItem key={u.id} value={u.id}>{u.nom} {u.prenom}</MenuItem>)}
              </Select>
            </FormControl>
            <Button
              variant="contained"
              color="warning"
              disabled={!nouvelAdminId || transfererAdminMutation.isLoading}
              onClick={handleTransfertAdmin}
            >
              {transfererAdminMutation.isLoading ? 'Transfert en cours...' : 'Transférer le rôle Admin'}
            </Button>
          </CardContent>
        </Card>
      )}

      {/* --- Comptes Actifs / Inactifs / Supprimés --- */}
      <Grid container spacing={3}>
        {/* Comptes Actifs */}
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>Comptes Actifs</Typography>
			  {/* Comptes Actifs */}
			  <List>
			    {comptesActifs?.map(u => {
			      const estAdmin = u.roles.some(r => r.libelle === 'ROLE_ADMIN'); // Vérifie si l'utilisateur est admin
			      return (
			        <ListItem key={u.id} sx={{ flexDirection: 'column', alignItems: 'flex-start', mb: 1, borderBottom: '1px solid #eee', pb: 1 }}>
			          <Stack direction="row" spacing={1} sx={{ mb: 1, flexWrap: 'wrap', alignItems: 'center' }}>
			            <Typography sx={{ fontWeight: 'bold' }}>{u.nom} {u.prenom}</Typography>
			            {u.roles.map((role, index) => (
			              <Chip key={`${u.id}-${role.id ?? index}`} label={role.libelle} size="small" color="primary" />
			            ))}
			          </Stack>

			          {/* Boutons uniquement si ce n'est pas un admin */}
			          {!estAdmin && (
			            <Stack direction="row" spacing={1}>
			              <Button
			                size="small"
			                color="error"
			                variant="outlined"
			                disabled={u.id === user.id || desactiverMutation.isLoading}
			                onClick={() => desactiverMutation.mutate(u.id)}
			              >
			                {desactiverMutation.isLoading ? 'Désactivation...' : 'Désactiver'}
			              </Button>
			              <Button
			                size="small"
			                color="error"
			                variant="outlined"
			                disabled={u.id === user.id || supprimerSoftMutation.isLoading}
			                onClick={() => supprimerSoftMutation.mutate(u.id)}
			              >
			                {supprimerSoftMutation.isLoading ? 'Suppression...' : 'Supprimer'}
			              </Button>
			            </Stack>
			          )}
			        </ListItem>
			      );
			    })}
			  </List>

            </CardContent>
          </Card>
        </Grid>

        {/* Comptes Inactifs */}
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>Comptes Inactifs</Typography>
              <List>
                {comptesInactifs?.map(u => (
                  <ListItem key={u.id} sx={{ flexDirection: 'column', alignItems: 'flex-start', mb: 1, borderBottom: '1px solid #eee', pb: 1 }}>
                    <Typography sx={{ fontWeight: 'bold', mb: 1 }}>{u.nom} {u.prenom}</Typography>
                    <FormControl fullWidth size="small" sx={{ mb: 1 }}>
                      <InputLabel>Rôles à attribuer</InputLabel>
                      <Select
                        multiple
                        value={roleSelection[u.id] || []}
                        onChange={(e) => setRoleSelection(prev => ({ ...prev, [u.id]: e.target.value.map(v => Number(v)) }))}
                        renderValue={(selected) => (
                          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                            {selected.map(roleId => {
                              const role = rolesPourActivation.find(r => r.id === roleId);
                              return <Chip key={roleId} label={role?.libelle} size="small" />;
                            })}
                          </Box>
                        )}
                      >
                        {rolesPourActivation.map(r => <MenuItem key={r.id} value={r.id}>{r.libelle}</MenuItem>)}
                      </Select>
                    </FormControl>
                    {!estAdmin(u) && (
                      <Button color="success" disabled={!roleSelection[u.id]?.length || activerMutation.isLoading} onClick={() => activerMutation.mutate({ id: u.id, roleIds: roleSelection[u.id] })}>
                        {activerMutation.isLoading ? 'Activation...' : 'Activer'}
                      </Button>
                    )}
                  </ListItem>
                ))}
              </List>
            </CardContent>
          </Card>
        </Grid>

        {/* Comptes Supprimés */}
		<Grid item xs={12} md={4}>
		  <Card>
		    <CardContent>
		      <Typography variant="h6" gutterBottom>Comptes Supprimés</Typography>
		      <List>
		        {comptesSupprimes?.map(u => (
		          <ListItem key={u.id} sx={{ flexDirection: 'column', alignItems: 'flex-start', mb: 1, borderBottom: '1px solid #eee', pb: 1 }}>
		            <Typography sx={{ fontWeight: 'bold', mb: 1 }}>{u.nom} {u.prenom}</Typography>
		            <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap', mb: 1 }}>
		              {u.roles.map((role, index) => <Chip key={`${u.id}-${role.id ?? index}`} label={role.libelle} size="small" color="default" />)}
		            </Stack>
		            <Stack direction="row" spacing={1}>
		              <Button
		                size="small"
		                color="success"
		                variant="outlined"
		                onClick={() => restaurerMutation.mutate({ id: u.id, roleIds: [3] })}
		              >
		                Restaurer
		              </Button>
		              <Button
		                size="small"
		                color="error"
		                variant="outlined"
		                onClick={() => supprimerDefinitifMutation.mutate(u.id)}
		              >
		                Supprimer
		              </Button>
		            </Stack>
		          </ListItem>
		        ))}
		      </List>
		    </CardContent>
		  </Card>
		</Grid>
      </Grid>
    </Box>
  );
};

export default GestionUtilisateurs;
