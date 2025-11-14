import React, { createContext, useContext, useState, useEffect } from 'react';
import { authService } from '../services/authService';

const AuthContext = createContext();

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('authToken');
    const userData = localStorage.getItem('userData');
    
    if (token && userData) {
      try {
        const parsedUser = JSON.parse(userData);
        // Ajouter des données par défaut si manquantes
        const userWithDefaults = {
          matricule: 'N/A',
          postePoliceNom: 'Poste principal',
          actif: true,
          ...parsedUser
        };
        setUser(userWithDefaults);
      } catch (error) {
        console.error('Error parsing user data:', error);
        logout();
      }
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    try {
      setLoading(true);
      const response = await authService.login(email, password);
      
      if (response.token && response.user) {
        localStorage.setItem('authToken', response.token);
        
        // Ajouter des données par défaut si manquantes
        const userWithDefaults = {
          matricule: 'N/A',
          postePoliceNom: 'Poste principal',
          actif: true,
          ...response.user
        };
        
        localStorage.setItem('userData', JSON.stringify(userWithDefaults));
        setUser(userWithDefaults);
        return true;
      } else {
        return false;
      }
    } catch (error) {
      console.error('Login error:', error);
      return false;
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userData');
    setUser(null);
  };

  const updateUser = (updatedUser) => {
    setUser(updatedUser);
    localStorage.setItem('userData', JSON.stringify(updatedUser));
  };

  const value = {
    user,
    login,
    logout,
    loading,
    updateUser,
    isAuthenticated: !!user,
    isAdmin: user?.roles?.includes('ROLE_ADMIN') || false,
    isSuperviseur: user?.roles?.includes('ROLE_SUPERVISEUR') || false,
    isAgent: user?.roles?.includes('ROLE_AGENT') || false
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};