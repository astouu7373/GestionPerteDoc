import React from 'react'; 
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import Layout from '../components/layout/Layout';

// Pages publiques
import Login from '../pages/Auth/Login';
import HomePage from '../pages/HomePage';
import Register from '../pages/Auth/Register';
import ResetPassword from '../pages/Auth/ResetPassword';
import ForgotPassword from '../pages/Auth/ForgotPassword';

// Tableaux de bord
import AdminDashboard from '../pages/Dashboard/admin/AdminDashboard';
import SuperviseurDashboard from '../pages/Dashboard/SuperviseurDashboard';
import AgentDashboard from '../pages/Dashboard/AgentDashboard';

// Pages déclarations
import DeclarationTabs from '../pages/Declarations/DeclarationTabs';
import DeclarationForm from '../pages/Declarations/DeclarationForm';
import DeclarationSearch from '../pages/Declarations/DeclarationSearch';
import DeclarationDetail from '../pages/Declarations/DeclarationDetail';

// Pages administration
import UserList from '../pages/Utilisateurs/UserList';
import TypeDocumentList from '../pages/TypesDocument/TypeDocumentList';
import Profile from '../pages/Utilisateurs/Profile';
import GestionUtilisateurs from '../pages/Dashboard/admin/GestionUtilisateurs';

// Routes protégées
const ProtectedRoute = ({ children, requiredRoles }) => {
  const { isAuthenticated, user, loading, initialized } = useAuth();

  if (loading || !initialized) {
    return <div style={{ display:'flex', justifyContent:'center', alignItems:'center', height:'100vh' }}>Chargement...</div>;
  }

  if (!isAuthenticated) return <Navigate to="/login" replace />;

  if (requiredRoles && !requiredRoles.some(role => user.roles?.includes(role))) {
    return <Navigate to="/" replace />;
  }

  return children;
};

// Routes publiques
const PublicRoute = ({ children }) => {
  const { loading, initialized } = useAuth();

  if (loading || !initialized) {
    return <div style={{ display:'flex', justifyContent:'center', alignItems:'center', height:'100vh' }}>Chargement...</div>;
  }

  return children;
};

// Redirection selon rôle
const DashboardRouter = () => {
  const { user } = useAuth();

  if (user?.roles?.includes('ROLE_ADMIN')) return <AdminDashboard />;
  if (user?.roles?.includes('ROLE_SUPERVISEUR')) return <SuperviseurDashboard />;
  if (user?.roles?.includes('ROLE_AGENT')) return <AgentDashboard />;

  return <AdminDashboard />; // par défaut
};

const AppRoutes = () => (
  <Routes>
    {/* HomePage accessible à tous */}
    <Route path="/" element={<HomePage />} />
    <Route path="/dashboard" element={<ProtectedRoute><Layout><DashboardRouter /></Layout></ProtectedRoute>} />

    {/* Routes publiques */}
    <Route path="/login" element={<PublicRoute><Login /></PublicRoute>} />
    <Route path="/register" element={<PublicRoute><Register /></PublicRoute>} />
    <Route path="/reset-password/:token" element={<PublicRoute><ResetPassword /></PublicRoute>} />
    <Route path="/forgot-password" element={<PublicRoute><ForgotPassword /></PublicRoute>} />

    {/* Routes protégées - Déclarations */}
    <Route path="/declarations" element={<ProtectedRoute><Layout><DeclarationTabs /></Layout></ProtectedRoute>} />
    <Route path="/declarations/nouvelle" element={<ProtectedRoute><Layout><DeclarationForm /></Layout></ProtectedRoute>} />
    <Route path="/declarations/:id" element={<ProtectedRoute><Layout><DeclarationDetail /></Layout></ProtectedRoute>} />
    <Route path="/declarations/:id/modifier" element={<ProtectedRoute><Layout><DeclarationForm /></Layout></ProtectedRoute>} />
    <Route path="/recherche-declaration" element={<ProtectedRoute><Layout><DeclarationSearch /></Layout></ProtectedRoute>} />

    {/* Gestion utilisateurs (Admin ou Superviseur) */}
    <Route path="/gestion-utilisateurs" element={<ProtectedRoute requiredRoles={['ROLE_ADMIN','ROLE_SUPERVISEUR']}><Layout><GestionUtilisateurs /></Layout></ProtectedRoute>} />
    <Route path="/utilisateurs" element={<ProtectedRoute requiredRoles={['ROLE_ADMIN','ROLE_SUPERVISEUR']}><Layout><UserList /></Layout></ProtectedRoute>} />

    {/* Autres pages */}
    <Route path="/types-document" element={<ProtectedRoute><Layout><TypeDocumentList /></Layout></ProtectedRoute>} />
    <Route path="/profile" element={<ProtectedRoute><Layout><Profile /></Layout></ProtectedRoute>} />

    {/* Page 404 */}
    <Route path="*" element={<Navigate to="/" replace />} />
  </Routes>
);

export default AppRoutes;
