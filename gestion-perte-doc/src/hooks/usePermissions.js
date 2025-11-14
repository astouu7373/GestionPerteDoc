import { useAuth } from '../contexts/AuthContext';

export const usePermissions = () => {
  const { user, isAdmin, isSuperviseur, isAgent } = useAuth();

  const canViewAllDeclarations = isAdmin || isSuperviseur;
  const canManageUsers = isAdmin;
  const canManageTypesDocument = isAdmin || isSuperviseur || isAgent;
  const canDeletePermanently = isAdmin;
  const canCreateDeclaration = isAdmin || isSuperviseur || isAgent;
  const canEditAnyDeclaration = isAdmin || isSuperviseur || isAgent;
  const canRestoreDeclaration = isAdmin || isSuperviseur || isAgent;

  const getUserDeclarationsView = () => {
    if (isAdmin || isSuperviseur) return 'all';
    if (isAgent) return 'own'; 
    return 'none';
  };

  return {
    user,
    isAdmin,
    isSuperviseur,
    isAgent,
    canViewAllDeclarations,
    canManageUsers,
    canManageTypesDocument,
    canDeletePermanently,
    canCreateDeclaration,
    canEditAnyDeclaration,
    canRestoreDeclaration,
    getUserDeclarationsView
  };
};
