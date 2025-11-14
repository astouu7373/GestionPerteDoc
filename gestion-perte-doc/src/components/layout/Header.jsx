import React, { useState } from 'react';
import {
  AppBar,
  Toolbar,
  Typography,
  Button,
  Box,
  Chip,
  IconButton,
  Badge,
  Menu,
  MenuItem,
  Avatar
} from '@mui/material';
import {
  AccountCircle,
  Notifications,
  Logout
} from '@mui/icons-material';
import { useAuth } from '../../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';

const Header = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [anchorEl, setAnchorEl] = useState(null);

  const handleMenu = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handleLogout = () => {
    logout();
    setAnchorEl(null); // Ferme le menu
    navigate('/login');
  };

  const handleProfile = () => {
    setAnchorEl(null); // Ferme le menu
    navigate('/profile');
  };

  const getRoleColor = (role) => {
    switch (role) {
      case 'ROLE_ADMIN': return 'error';
      case 'ROLE_SUPERVISEUR': return 'warning';
      case 'ROLE_AGENT': return 'primary';
      default: return 'default';
    }
  };

  const getMainRole = () => {
    if (user?.roles?.includes('ROLE_ADMIN')) return 'ADMIN';
    if (user?.roles?.includes('ROLE_SUPERVISEUR')) return 'SUPERVISEUR';
    if (user?.roles?.includes('ROLE_AGENT')) return 'AGENT';
    return 'UTILISATEUR';
  };

  return (
    <AppBar 
      position="static" 
      elevation={2}
      sx={{ 
        backgroundColor: 'white',
        color: 'text.primary',
        borderBottom: '1px solid',
        borderColor: 'divider'
      }}
    >
      <Toolbar>
        <Typography 
          variant="h6" 
          component="div" 
          sx={{ 
            flexGrow: 1, 
            fontWeight: 'bold',
            color: 'primary.main',
            display: 'flex',
            alignItems: 'center',
            gap: 1
          }}
        >
          Gestion Perte Documents
          <Chip 
            label={getMainRole()} 
            color={getRoleColor(user?.roles?.[0])}
            size="small"
            variant="outlined"
          />
        </Typography>

        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Chip
            label={user?.postePoliceNom || 'Poste de Police'}
            variant="outlined"
            sx={{ borderColor: 'primary.main', color: 'primary.main' }}
          />
          
          <IconButton color="inherit">
            <Badge badgeContent={0} color="error">
              <Notifications />
            </Badge>
          </IconButton>

          <Button
            color="inherit"
            startIcon={<AccountCircle />}
            onClick={handleMenu}
            sx={{ 
              textTransform: 'none',
              fontWeight: 600
            }}
          >
            {user?.prenom} {user?.nom}
          </Button>

          <Menu
            anchorEl={anchorEl}
            open={Boolean(anchorEl)}
            onClose={() => setAnchorEl(null)}
            PaperProps={{
              sx: {
                mt: 1,
                minWidth: 200
              }
            }}
          >
            <MenuItem onClick={handleProfile}>
              <AccountCircle sx={{ mr: 1 }} />
              Mon Profil
            </MenuItem>
            <MenuItem onClick={handleLogout} sx={{ color: 'error.main' }}>
              <Logout sx={{ mr: 1 }} />
              Déconnexion
            </MenuItem>
          </Menu>
        </Box>
      </Toolbar>
    </AppBar>
  );
};

export default Header;
