import React, { useState } from 'react';
import {
  Box,
  Tabs,
  Tab,
  Typography,
  Card
} from '@mui/material';
import { useSearchParams } from 'react-router-dom';
import { usePermissions } from '../../hooks/usePermissions';
import DeclarationList from './DeclarationList';

const DeclarationTabs = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const { isAdmin, isSuperviseur, isAgent } = usePermissions();
  
  const currentTab = searchParams.get('tab') || 'mes-declarations';

  const handleTabChange = (event, newValue) => {
    setSearchParams({ tab: newValue });
  };

  // Définition des onglets selon le rôle
  const getTabs = () => {
    if (isAdmin) {
      return [
        { value: 'mes-declarations', label: 'Mes Déclarations Actives' },
        { value: 'toutes-declarations', label: 'Toutes les Déclarations Actives' },
        { value: 'mes-supprimees', label: 'Mes Déclarations Supprimées' },
        { value: 'toutes-supprimees', label: 'Toutes les Déclarations Supprimées' }
      ];
    } else if (isSuperviseur) {
      return [
        { value: 'mes-declarations', label: 'Mes Déclarations Actives' },
        { value: 'toutes-declarations', label: 'Déclarations du Poste Actives' },
        { value: 'mes-supprimees', label: 'Mes Déclarations Supprimées' },
        { value: 'toutes-supprimees', label: 'Déclarations du Poste Supprimées' }
      ];
    } else if (isAgent) {
      return [
        { value: 'mes-declarations', label: 'Mes Déclarations Actives' },
        { value: 'mes-supprimees', label: 'Mes Déclarations Supprimées' }
      ];
    }
    return [];
  };

  const tabs = getTabs();

  const getListType = (tabValue) => {
    switch (tabValue) {
      case 'mes-declarations':
        return { type: 'actives', scope: 'own' };
      case 'toutes-declarations':
        return { type: 'actives', scope: 'all' };
      case 'mes-supprimees':
        return { type: 'supprimees', scope: 'own' };
      case 'toutes-supprimees':
        return { type: 'supprimees', scope: 'all' };
      default:
        return { type: 'actives', scope: 'own' };
    }
  };

  const currentListType = getListType(currentTab);

  return (
    <Box>
      <Card sx={{ mb: 3 }}>
        <Tabs
          value={currentTab}
          onChange={handleTabChange}
          variant="scrollable"
          scrollButtons="auto"
        >
          {tabs.map((tab) => (
            <Tab
              key={tab.value}
              value={tab.value}
              label={tab.label}
            />
          ))}
        </Tabs>
      </Card>

      <DeclarationList
        type={currentListType.type}
        scope={currentListType.scope}
      />
    </Box>
  );
};

export default DeclarationTabs;