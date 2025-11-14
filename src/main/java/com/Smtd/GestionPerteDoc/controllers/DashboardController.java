package com.Smtd.GestionPerteDoc.controllers;

import com.Smtd.GestionPerteDoc.dtos.PosteStatsDTO;
import com.Smtd.GestionPerteDoc.entities.Utilisateur;
import com.Smtd.GestionPerteDoc.security.services.CustomUserDetails;
import com.Smtd.GestionPerteDoc.services.DeclarationService;
import com.Smtd.GestionPerteDoc.services.UtilisateurService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DeclarationService declarationService;
    private final UtilisateurService utilisateurService;

    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            System.out.println(" REQUÊTE STATISTIQUES DASHBOARD");
            
            if (userDetails == null) {
                return ResponseEntity.status(401).body(createErrorResponse("Utilisateur non authentifié"));
            }

           
            PosteStatsDTO stats = declarationService.getPosteStats();
            
          
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            response.put("timestamp", System.currentTimeMillis());
            
            System.out.println(" STATISTIQUES ENVOYÉES AU DASHBOARD: " + stats);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println(" ERREUR STATISTIQUES DASHBOARD: " + e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Erreur lors du calcul des statistiques");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    /**
     * Récupère les stats pour l'utilisateur connecté (agent ou superviseur)
     */
    @GetMapping("/stats/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISEUR','AGENT')")
    public PosteStatsDTO getUserStats(@PathVariable Long userId) {
        Utilisateur user = utilisateurService.trouverParId(userId);
        return declarationService.getUserStats(user.getId());
    }

    /**
     * Récupère les stats globales du poste (accessible seulement à l'admin)
     * Si posteId est null, prend le poste de l'utilisateur admin
     */
    @GetMapping("/stats/poste")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public PosteStatsDTO getPosteStats(@RequestParam Long userId, @RequestParam(required = false) Long posteId) {
        Utilisateur user = utilisateurService.trouverParId(userId);

        if (!user.hasRole("ROLE_ADMIN")) {
            throw new RuntimeException("Accès refusé - seulement admin");
        }

        // Si aucun posteId fourni, on prend le poste de l'admin
        Long posteCible = posteId != null ? posteId : (user.getPostePolice() != null ? user.getPostePolice().getId() : null);
        if (posteCible == null) {
            throw new RuntimeException("Poste non défini pour l'utilisateur admin");
        }

        return declarationService.getPosteStats();
    }


    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}