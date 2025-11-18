package com.Smtd.GestionPerteDoc.security;
import com.Smtd.GestionPerteDoc.entities.Role;
import com.Smtd.GestionPerteDoc.repositories.RoleRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        creerRoleSiNonExistant("ROLE_ADMIN");
        creerRoleSiNonExistant("ROLE_SUPERVISEUR");
        creerRoleSiNonExistant("ROLE_AGENT");
    }

    private void creerRoleSiNonExistant(String libelle) {
        if (!roleRepository.existsByLibelle(libelle)) {
            Role role = new Role();
            role.setLibelle(libelle);
            roleRepository.save(role);
            System.out.println("== ROLE CRÉÉ : " + libelle + " ==");
        }
    }
}
