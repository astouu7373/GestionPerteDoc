package com.Smtd.GestionPerteDoc.controllers;

import java.util.Map;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.Smtd.GestionPerteDoc.dtos.InitialisationRequest;
import com.Smtd.GestionPerteDoc.entities.PostePolice;
import com.Smtd.GestionPerteDoc.entities.Role;
import com.Smtd.GestionPerteDoc.entities.Utilisateur;
import com.Smtd.GestionPerteDoc.repositories.PostePoliceRepository;
import com.Smtd.GestionPerteDoc.repositories.RoleRepository;
import com.Smtd.GestionPerteDoc.repositories.UtilisateurRepository;
import com.Smtd.GestionPerteDoc.security.SystemStateChecker;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemController {

    private final SystemStateChecker systemStateChecker;
    private final PostePoliceRepository postePoliceRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // Vérifier si le système est initialisé
    @GetMapping("/etat")
    public Map<String, Boolean> etatSysteme() {
        return Map.of("initialised", systemStateChecker.isInitialized());
    }

    // Initialisation du système
    @PostMapping("/initialiser")
    public ResponseEntity<?> initialiserSysteme(@RequestBody InitialisationRequest req) {

        if (systemStateChecker.isInitialized()) {
            return ResponseEntity.badRequest().body("Le système est déjà initialisé");
        }

        // 1️⃣ Créer poste principal
        PostePolice poste = new PostePolice();
        poste.setNom(req.getPosteNom());
        poste.setAdresse(req.getAdresse());
        poste.setTelephone(req.getPosteTelephone());
        // Générer codeUnique automatiquement
        poste.setCodeUnique("POSTE-" + System.currentTimeMillis());
        postePoliceRepository.save(poste);

        // 2️⃣ Créer admin actif
        Utilisateur admin = new Utilisateur();
        admin.setNom(req.getNomAdmin());
        admin.setPrenom(req.getPrenomAdmin());
        admin.setEmail(req.getEmail());
        admin.setPostePolice(poste);
        admin.setActif(true);
        admin.setMatricule("ADM-" + System.currentTimeMillis());
        admin.setMotDePasseHash(passwordEncoder.encode(req.getMotDePasse()));

        Role roleAdmin = roleRepository.findByLibelle("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("ROLE_ADMIN introuvable"));
        admin.setRoles(Set.of(roleAdmin));

        utilisateurRepository.save(admin);

        // Marquer système comme initialisé
        systemStateChecker.refresh();

        return ResponseEntity.ok("Initialisation effectuée avec succès.");
    }
}
