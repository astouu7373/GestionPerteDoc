import React from 'react';
import { Box, Typography, Button } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import Logo from '../assets/logo.png';
import Commissariat from '../assets/commissariat.png';

const HomePage = () => {
  const navigate = useNavigate();

  const handleLoginClick = () => {
    navigate('/login');
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundImage: `url(${Commissariat})`,
        backgroundSize: 'cover',
        backgroundPosition: 'center',
        backgroundRepeat: 'no-repeat',
        position: 'relative',
        color: 'white',
        textAlign: 'center',
      }}
    >
      <Box sx={{ position: 'absolute', top: 20, left: 20 }}>
        <img src={Logo} alt="Logo" style={{ height: 60 }} />
      </Box>

      <Box
        sx={{
          p: 3,
          backgroundColor: 'rgba(0, 0, 0, 0.4)',
          borderRadius: 2,
        }}
      >
        <Typography variant="h3" gutterBottom sx={{ fontWeight: 'bold', textShadow: '2px 2px 6px rgba(0,0,0,0.7)' }}>
          Bienvenue dans le Système de Gestion de Perte de Documents Officiels
        </Typography>
        <Typography variant="h3" gutterBottom sx={{ fontWeight: 'bold', textShadow: '2px 2px 6px rgba(0,0,0,0.7)' }}>
          SYGEPEDOF
        </Typography>
        <Typography variant="h6" gutterBottom sx={{ mb: 4, textShadow: '1px 1px 4px rgba(0,0,0,0.7)' }}>
          Veuillez vous connecter pour accéder aux fonctionnalités
        </Typography>
        <Button
          variant="contained"
          color="primary"
          size="large"
          onClick={handleLoginClick}
          sx={{
            mt: 2,
            px: 6,
            py: 1.5,
            fontSize: '1.1rem',
            fontWeight: 'bold',
            boxShadow: '0 4px 12px rgba(0,0,0,0.25)',
            '&:hover': { transform: 'translateY(-3px)', boxShadow: '0 6px 20px rgba(0,0,0,0.35)' },
          }}
        >
          Se connecter
        </Button>
      </Box>
    </Box>
  );
};

export default HomePage;
