package com.Smtd.GestionPerteDoc.services;

import com.Smtd.GestionPerteDoc.entities.Role;
import com.Smtd.GestionPerteDoc.repositories.RoleRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public Role creerRole(Role role) {
        return roleRepository.save(role);
    }

    public List<Role> listerTousRoles() {
        return roleRepository.findAll();
    }

    public Role trouverParId(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé"));
    }

    public Role trouverParLibelle(String  libelle) {
        return roleRepository.findByLibelle(libelle)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé"));
    }
} 
