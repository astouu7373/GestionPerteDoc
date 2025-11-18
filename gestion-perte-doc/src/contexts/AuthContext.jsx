import React, { createContext, useContext, useState, useEffect } from 'react';
import { authService } from '../services/authService';
import { systemService } from '../services/systemService';

const AuthContext = createContext();

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within an AuthProvider');
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [initialized, setInitialized] = useState(null);

  // Au démarrage : vérifier auth + initialisation système
  useEffect(() => {
    const checkSystem = async () => {
      try {
        const systemState = await systemService.etatSysteme();
        setInitialized(systemState.initialised);
      } catch (err) {
        console.error('Erreur vérification système', err);
        setInitialized(false);
      }
      setUser(null);
      setLoading(false);
    };

    checkSystem();
  }, []);

  const login = async (email, password) => {
    setLoading(true);
    try {
      const response = await authService.login(email, password);
      if (response.token && response.user) {
        localStorage.setItem('authToken', response.token);
        localStorage.setItem('userData', JSON.stringify(response.user));
        setUser(response.user);
        return true;
      }
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

  return (
    <AuthContext.Provider value={{
      user,
      login,
      logout,
      loading,
      initialized,
      updateUser,
      isAuthenticated: !!user,
      isAdmin: user?.roles?.includes('ROLE_ADMIN') || false,
      isSuperviseur: user?.roles?.includes('ROLE_SUPERVISEUR') || false,
      isAgent: user?.roles?.includes('ROLE_AGENT') || false
    }}>
      {children}
    </AuthContext.Provider>
  );
};
