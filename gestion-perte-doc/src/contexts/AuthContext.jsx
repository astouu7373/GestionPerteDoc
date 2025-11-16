import React, { createContext, useContext, useState, useEffect } from 'react';
import { authService } from '../services/authService';

const AuthContext = createContext();

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within an AuthProvider');
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [initialized, setInitialized] = useState(false);

  // Toujours démarrer déconnecté
  useEffect(() => {
    setUser(null);  
    setLoading(false);
    setInitialized(true);
  }, []);

  const login = async (email, password) => {
    try {
      setLoading(true);
      const response = await authService.login(email, password);
      if (response.token && response.user) {
        localStorage.setItem('authToken', response.token);

        const userWithDefaults = {
          matricule: 'N/A',
          postePoliceNom: 'Poste principal',
          actif: true,
          ...response.user
        };

        localStorage.setItem('userData', JSON.stringify(userWithDefaults));
        setUser(userWithDefaults);
        return true;
      }
      return false;
    } catch (err) {
      console.error(err);
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
