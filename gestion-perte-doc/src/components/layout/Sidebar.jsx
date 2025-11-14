import React from 'react';
import { Drawer, List, ListItem, ListItemIcon, ListItemText, ListItemButton, Box, Typography } from '@mui/material';
import { Dashboard, Description, People, Assignment, Search } from '@mui/icons-material';
import { useNavigate, useLocation } from 'react-router-dom';
import { usePermissions } from '../../hooks/usePermissions';

const menuItems = [
  { text: 'Tableau de bord', icon: <Dashboard />, path: '/' },
  { text: 'Déclarations', icon: <Description />, path: '/declarations' },
  { text: 'Recherche Déclaration', icon: <Search />, path: '/recherche-declaration' },
  { text: 'Types Documents', icon: <Assignment />, path: '/types-document' },
  { text: 'Utilisateurs', icon: <People />, path: '/utilisateurs', roles: ['ROLE_ADMIN', 'ROLE_SUPERVISEUR'] },
  { text: 'Gestion Utilisateurs', icon: <People />, path: '/gestion-utilisateurs', roles: ['ROLE_ADMIN', 'ROLE_SUPERVISEUR'] },
];

const Sidebar = ({ width = 280 }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const { isAdmin, isSuperviseur } = usePermissions();

  const filteredMenuItems = menuItems.filter(item => {
    if (item.roles) return isAdmin || isSuperviseur;
    return true;
  });

  return (
    <Drawer
      variant="permanent"
      sx={{
        width,
        flexShrink: 0,
        '& .MuiDrawer-paper': {
          width,
          boxSizing: 'border-box',
          bgcolor: 'background.paper',
          borderRight: '1px solid',
          borderColor: 'divider'
        },
      }}
    >
      <Box sx={{ p: 3, borderBottom: '1px solid', borderColor: 'divider' }}>
        <Typography variant="h6" color="primary" sx={{ fontWeight: 'bold' }}>
          Menu Principal
        </Typography>
      </Box>
      
      <List sx={{ pt: 2 }}>
        {filteredMenuItems.map((item) => (
          <ListItem key={item.text} disablePadding>
            <ListItemButton
              selected={location.pathname === item.path}
              onClick={() => navigate(item.path)}
              sx={{
                borderRadius: 2,
                mx: 1.5,
                mb: 0.5,
                '&.Mui-selected': {
                  bgcolor: 'primary.main',
                  color: 'white',
                  '&:hover': { bgcolor: 'primary.dark' }
                }
              }}
            >
              <ListItemIcon sx={{ 
                color: location.pathname === item.path ? 'white' : 'text.secondary',
                minWidth: 40 
              }}>
                {item.icon}
              </ListItemIcon>
              <ListItemText primary={item.text} />
            </ListItemButton>
          </ListItem>
        ))}
      </List>
    </Drawer>
  );
};

export default Sidebar;
