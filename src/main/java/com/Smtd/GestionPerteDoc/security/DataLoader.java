package com.Smtd.GestionPerteDoc.security;
import com.Smtd.GestionPerteDoc.entities.PostePolice;
import com.Smtd.GestionPerteDoc.entities.Role;
import com.Smtd.GestionPerteDoc.repositories.PostePoliceRepository;
import com.Smtd.GestionPerteDoc.repositories.RoleRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PostePoliceRepository postePoliceRepository;

    @Override
    public void run(String... args) throws Exception {
        // Créer les rôles s'ils n'existent pas
        creerRoleSiNonExistant("ROLE_ADMIN", "Administrateur système");
        creerRoleSiNonExistant("ROLE_SUPERVISEUR", "Superviseur de poste");
        creerRoleSiNonExistant("ROLE_AGENT", "Agent de police");

        // Créer un poste de police par défaut s'il n'existe pas
        if (postePoliceRepository.count() == 0) {
            PostePolice postePrincipal = new PostePolice();
            postePrincipal.setNom("Poste De Police Principal - Commissariat Central");
            postePrincipal.setAdresse("Boulevard ");
            postePrincipal.setTelephone("+223 20 21 22 22");
            postePrincipal.setCodeUnique("PP001");
            postePoliceRepository.save(postePrincipal);
            System.out.println("=== POSTE DE POLICE PAR DÉFAUT CRÉÉ ===");
        }
    }

    private void creerRoleSiNonExistant(String libelle, String description) {
        if (!roleRepository.existsByLibelle(libelle)) {
            Role role = new Role();
            role.setLibelle(libelle);
            roleRepository.save(role);
            System.out.println("=== RÔLE CRÉÉ: " + libelle + " ===");
        }
    }
}