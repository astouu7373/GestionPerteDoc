package com.Smtd.GestionPerteDoc.controllers;

import com.Smtd.GestionPerteDoc.dtos.DTOMapper;
import com.Smtd.GestionPerteDoc.dtos.UtilisateurDTO;
import com.Smtd.GestionPerteDoc.entities.Utilisateur;
import com.Smtd.GestionPerteDoc.services.UtilisateurService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/utilisateurs")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    // ===================== CRÉATION UTILISATEUR =====================
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISEUR')")
    public ResponseEntity<?> creerUtilisateur(
            @RequestBody Utilisateur utilisateur,
            @RequestParam Long postePoliceId,
            @RequestParam(required = false) List<Long> roleIds) {
        try {
            Utilisateur nouvelUtilisateur = utilisateurService.creerUtilisateurAvecRoles(utilisateur, postePoliceId, roleIds);
            return ResponseEntity.ok(DTOMapper.toUtilisateurDTO(nouvelUtilisateur));
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("erreur", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ===================== ACTIVATION =====================
    @PostMapping("/{id}/activer")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISEUR')")
    public ResponseEntity<?> activerUtilisateur(
            @PathVariable Long id,
            @RequestBody List<Long> roleIds) {
        try {
            Utilisateur utilisateur = utilisateurService.activerEtAttribuerRoleAvecControle(id, roleIds);
            return ResponseEntity.ok(DTOMapper.toUtilisateurDTO(utilisateur));
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("erreur", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ===================== DÉSACTIVATION =====================
    @PostMapping("/{id}/desactiver")
    public ResponseEntity<?> desactiverUtilisateur(@PathVariable Long id) {
        try {
            utilisateurService.desactiverUtilisateur(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Utilisateur désactivé avec succès");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("erreur", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ===================== TRANSFERT RÔLE ADMIN =====================
    @PostMapping("/{ancienAdminId}/transferer-admin/{nouveauAdminId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> transfererRoleAdmin(@PathVariable Long ancienAdminId,
                                                 @PathVariable Long nouveauAdminId) {
        try {
            Utilisateur nouveauAdmin = utilisateurService.transfererRoleAdmin(ancienAdminId, nouveauAdminId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Rôle admin transféré avec succès");
            response.put("nouvelAdmin", DTOMapper.toUtilisateurDTO(nouveauAdmin));
            response.put("ancienAdminId", ancienAdminId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("erreur", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ===================== MISE À JOUR =====================
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISEUR')")
    public ResponseEntity<?> mettreAJourUtilisateur(@PathVariable Long id,
                                                    @RequestBody Utilisateur utilisateur,
                                                    @RequestParam Long postePoliceId,
                                                    @RequestParam List<Long> roleIds) {
        try {
            Utilisateur utilisateurMisAJour = utilisateurService.mettreAJourUtilisateur(id, utilisateur, postePoliceId, roleIds);
            return ResponseEntity.ok(DTOMapper.toUtilisateurDTO(utilisateurMisAJour));
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("erreur", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

 // ===================== SUPPRESSION =====================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISEUR')")
    public ResponseEntity<?> supprimerUtilisateurSoft(@PathVariable Long id) {
        try {
            Utilisateur actuel = utilisateurService.getUtilisateurCourant();
            Utilisateur cible = utilisateurService.trouverParId(id);

            // Interdiction pour un superviseur de supprimer un admin
            if (cible.hasRole("ROLE_ADMIN") && actuel.hasRole("ROLE_SUPERVISEUR")) {
                return ResponseEntity.status(403)
                        .body(Map.of("erreur", "Un superviseur ne peut pas supprimer un utilisateur ADMIN !"));
            }

            // Suppression soft
            utilisateurService.supprimerUtilisateurSoft(id);

            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {
            return ResponseEntity.status(403)
                    .body(Map.of("erreur", e.getMessage()));
        }
    }



    // ===================== LISTING =====================
    @GetMapping("/actifs")
    public ResponseEntity<List<UtilisateurDTO>> listerUtilisateursActifs() {
        List<UtilisateurDTO> utilisateurs = utilisateurService.listerUtilisateursActifs()
                .stream()
                .map(DTOMapper::toUtilisateurDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(utilisateurs);
    }

    @GetMapping("/inactifs")
    public ResponseEntity<List<UtilisateurDTO>> listerUtilisateursInactifs() {
        List<UtilisateurDTO> utilisateurs = utilisateurService.listerUtilisateursInactifs()
                .stream()
                .map(DTOMapper::toUtilisateurDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(utilisateurs);
    }

    // ===================== UTILISATEUR PAR ID =====================
    @GetMapping("/id/{id}")
    public ResponseEntity<?> trouverParId(@PathVariable Long id) {
        try {
            Utilisateur utilisateur = utilisateurService.trouverParId(id);
            return ResponseEntity.ok(DTOMapper.toUtilisateurDTO(utilisateur));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ===================== LISTE TOUS =====================
    @GetMapping
    public ResponseEntity<List<UtilisateurDTO>> listerTousUtilisateurs() {
        List<UtilisateurDTO> utilisateurs = utilisateurService.listerTousUtilisateurs()
                .stream()
                .map(DTOMapper::toUtilisateurDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(utilisateurs);
    }

    // ===================== VÉRIFIE EXISTE ADMIN =====================
    @GetMapping("/existe-admin")
    public ResponseEntity<Map<String, Boolean>> existeAdmin() {
        boolean existe = utilisateurService.existeAdmin();
        Map<String, Boolean> response = new HashMap<>();
        response.put("existe", existe);
        return ResponseEntity.ok(response);
    }

    // ===================== RESET PASSWORD =====================
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetMotDePasse(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("resetToken");
            String nouveauMotDePasse = request.get("nouveauMotDePasse");
            utilisateurService.changerMotDePasseAvecToken(token, nouveauMotDePasse);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Mot de passe réinitialisé avec succès et compte activé !");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> response = new HashMap<>();
            response.put("erreur", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotMotDePasse(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            utilisateurService.envoyerLienReinitialisation(email);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Email de réinitialisation envoyé si le compte existe");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> response = new HashMap<>();
            response.put("erreur", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ===================== LISTE UTILISATEURS SUPPRIMÉS =====================
    @GetMapping("/supprimes")
    public ResponseEntity<List<UtilisateurDTO>> listerUtilisateursSupprimes() {
        List<UtilisateurDTO> utilisateurs = utilisateurService.listerUtilisateursSupprimes()
                .stream()
                .map(DTOMapper::toUtilisateurDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(utilisateurs);
    }
    // --- Restaurer un compte supprimé ---
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISEUR')")
    @PostMapping("/{id}/restaurer")
    public UtilisateurDTO restaurerUtilisateur(
            @PathVariable Long id,
            @RequestBody RestaurerRequest request) {
        return utilisateurService.restaurerUtilisateur(id, request.roleIds);
    }

    public static class RestaurerRequest {
        public List<Long> roleIds;
    }
    @DeleteMapping("/{id}/definitif")
    public ResponseEntity<?> supprimerUtilisateurDefinitif(@PathVariable Long id) {
        utilisateurService.supprimerUtilisateurDefinitif(id);
        return ResponseEntity.ok(Map.of("message", "Utilisateur supprimé définitivement"));
    }


}
