import React from 'react';
import { Box } from '@mui/material';
import Header from './Header';
import Sidebar from './Sidebar';
import { useAuth } from '../../contexts/AuthContext';

const Layout = ({ children }) => {
  const { user } = useAuth();

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', backgroundColor: '#f5f7fa' }}>
      <Sidebar />
      <Box sx={{ display: 'flex', flexDirection: 'column', flexGrow: 1 }}>
        <Header />
        <Box
          component="main"
          sx={{
            flexGrow: 1,
            p: 3,
            transition: 'all 0.3s ease',
          }}
        >
          {children}
        </Box>
      </Box>
    </Box>
  );
};

export default Layout;