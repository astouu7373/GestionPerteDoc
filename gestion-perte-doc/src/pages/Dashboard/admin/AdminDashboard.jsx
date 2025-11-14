import React from 'react';
import { Box, Grid, Typography, Button } from '@mui/material';
import { Description, CheckCircle, Pending, Cancel, Add } from '@mui/icons-material';
import { useQuery } from 'react-query';
import { declarationService } from '../../../services/declarationService';
import { useNavigate } from 'react-router-dom';
import { usePermissions } from '../../../hooks/usePermissions';
import StatCard from '../../../components/StatCard';

const AdminDashboard = () => {
  const navigate = useNavigate();
  const { user } = usePermissions();

  const { data: stats, isLoading } = useQuery(
    ['user-stats', user?.id],
    () => declarationService.getStatsUser(user.id),
    { enabled: !!user }
  );

  const { data: statsPoste } = useQuery(
    ['poste-stats', user?.id],
    () => declarationService.getStatsPoste(user.id),
    { enabled: !!user }
  );

  if (isLoading) return <div>Chargement...</div>;

  const quickActions = [
    { label: 'Nouvelle Déclaration', path: '/declarations/nouvelle', variant: 'contained' },
    { label: 'Mes Déclarations', path: '/declarations?tab=mes-declarations', variant: 'outlined' },
    { label: 'Toutes les Déclarations', path: '/declarations?tab=toutes-declarations', variant: 'outlined' },
    { label: 'Mes Déclarations Supprimées', path: '/declarations?tab=mes-supprimees', variant: 'outlined' },
    { label: 'Toutes les Déclarations Supprimées', path: '/declarations?tab=toutes-supprimees', variant: 'outlined' },
    { label: 'Rechercher Déclaration', path: '/recherche-declaration', variant: 'outlined' }
  ];

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom sx={{ fontWeight: 'bold', mb: 4 }}>Tableau de Bord - Admin</Typography>

      {/* Statistiques personnelles */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard title="Total Déclarations" value={stats?.totalDeclarations || 0} icon={<Description sx={{ fontSize: 40 }} />} color="#1976d2" />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard title="Validées" value={stats?.declarationsValidees || 0} icon={<CheckCircle sx={{ fontSize: 40 }} />} color="#2e7d32" />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard title="En Attente" value={stats?.declarationsEnregistrees || 0} icon={<Pending sx={{ fontSize: 40 }} />} color="#ed6c02" />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard title="Supprimées" value={stats?.declarationsSupprimees || 0} icon={<Cancel sx={{ fontSize: 40 }} />} color="#d32f2f" />
        </Grid>
      </Grid>

      {/* Statistiques globales du poste */}
      <Typography variant="h5" gutterBottom sx={{ mb: 2 }}>Statistiques Globales du Poste</Typography>
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard title="Total Déclarations" value={statsPoste?.totalDeclarations || 0} icon={<Description sx={{ fontSize: 40 }} />} color="#1976d2" />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard title="Validées" value={statsPoste?.declarationsValidees || 0} icon={<CheckCircle sx={{ fontSize: 40 }} />} color="#2e7d32" />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard title="En Attente" value={statsPoste?.declarationsEnregistrees || 0} icon={<Pending sx={{ fontSize: 40 }} />} color="#ed6c02" />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard title="Supprimées" value={statsPoste?.declarationsSupprimees || 0} icon={<Cancel sx={{ fontSize: 40 }} />} color="#d32f2f" />
        </Grid>
      </Grid>

      {/* Actions rapides */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        {quickActions.map((action, index) => (
          <Grid item xs={12} sm={6} md={3} key={index}>
            <Button
              fullWidth
              variant={action.variant}
              color="primary"
              onClick={() => navigate(action.path)}
              startIcon={index === 0 ? <Add /> : undefined}
              sx={{ py: 1.5 }}
            >
              {action.label}
            </Button>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
};

export default AdminDashboard;
