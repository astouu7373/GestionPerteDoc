package com.Smtd.GestionPerteDoc.controllers;

import com.Smtd.GestionPerteDoc.dtos.DeclarationDTO;
import com.Smtd.GestionPerteDoc.dtos.DTOMapper;
import com.Smtd.GestionPerteDoc.dtos.PosteStatsDTO;
import com.Smtd.GestionPerteDoc.dtos.RechercherDeclarantRequest;
import com.Smtd.GestionPerteDoc.entities.Declarant;
import com.Smtd.GestionPerteDoc.entities.Declaration;
import com.Smtd.GestionPerteDoc.entities.TypeDocument;
import com.Smtd.GestionPerteDoc.entities.Utilisateur;
import com.Smtd.GestionPerteDoc.enums.StatutDeclaration;
import com.Smtd.GestionPerteDoc.repositories.DeclarationRepository;
import com.Smtd.GestionPerteDoc.repositories.TypeDocumentRepository;
import com.Smtd.GestionPerteDoc.security.services.CustomUserDetails;
import com.Smtd.GestionPerteDoc.services.DeclarantService;
import com.Smtd.GestionPerteDoc.services.DeclarationService;
import com.Smtd.GestionPerteDoc.services.PdfGeneratorService;
import com.Smtd.GestionPerteDoc.services.UtilisateurService;
import com.Smtd.GestionPerteDoc.services.EmailService;
import com.Smtd.GestionPerteDoc.services.NumeroReferenceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/declarations")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
@Slf4j
public class DeclarationController {

    private final DeclarationService declarationService;
    private final EmailService emailService;
    private final DeclarantService declarantService;
    private final TypeDocumentRepository typeDocumentRepository;
    private final NumeroReferenceService numeroReferenceService;
    private final DeclarationRepository declarationRepository;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISEUR','AGENT')")
    public ResponseEntity<?> creerDeclaration(@RequestBody Declaration declaration,
                                              @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401)
                    .body(creerReponseErreur("Utilisateur non authentifié"));
        }

        try {
            Utilisateur utilisateurConnecte = userDetails.getUtilisateur();

            // ===== Trouver ou créer le déclarant =====
            Declarant declarantFinal = declarantService.trouverOuCreerDeclarant(declaration.getDeclarant());
            declaration.setDeclarant(declarantFinal);

            // ===== Vérifier le type de document =====
            TypeDocument typeDocument = typeDocumentRepository.findById(declaration.getTypeDocument().getId())
                    .orElseThrow(() -> new RuntimeException(
                            "Type de document non trouvé avec ID: " + declaration.getTypeDocument().getId()));
            declaration.setTypeDocument(typeDocument);

            // ===== Initialiser les champs de la déclaration =====
            declaration.setUtilisateur(utilisateurConnecte);
            declaration.setCreePar(utilisateurConnecte);
            declaration.setCreeLe(new Date());
            declaration.setDateDeclaration(new Date());
            declaration.setSupprime(false);

            if (declaration.getStatut() == null) declaration.setStatut(StatutDeclaration.ENREGISTREE);
            if (declaration.getNumeroReference() == null || declaration.getNumeroReference().isEmpty()) {
                declaration.setNumeroReference(numeroReferenceService.generateNumeroReference());
            }

            // ===== Sauvegarder la déclaration =====
            Declaration saved = declarationRepository.save(declaration);
            declarationRepository.flush();

            // ===== ENVOI EMAIL AVEC PDF =====
            try {
                if (saved.getDeclarant() != null && saved.getDeclarant().getEmail() != null &&
                    !saved.getDeclarant().getEmail().isEmpty()) {

                    byte[] pdfBytes = PdfGeneratorService.generateDeclarationPdf(saved);
                    log.info("Envoi d'email à : {}", saved.getDeclarant().getEmail());
                    emailService.envoyerEmailAvecPdf(
                            saved.getDeclarant().getEmail(),
                            pdfBytes,
                            saved.getNumeroReference()
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
                
            }

            // ===== Réponse =====
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Déclaration créée avec succès et PDF envoyé par email");
            response.put("declaration", DTOMapper.toDeclarationDTO(saved));

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(creerReponseErreur(e.getMessage()));
        }
    }

    // ---------------- Modification ----------------
    @PutMapping("/{id}")
    public ResponseEntity<?> modifierDeclaration(@PathVariable Long id,
                                                 @RequestBody Declaration declaration,
                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) return ResponseEntity.status(401).body(creerReponseErreur("Utilisateur non authentifié"));

        try {
            Utilisateur utilisateurConnecte = userDetails.getUtilisateur();
            if (!declarationService.peutModifierDeclaration(id, utilisateurConnecte))
                return ResponseEntity.status(403).body(creerReponseErreur("Vous n'avez pas les permissions pour modifier cette déclaration"));

            Declaration declarationModifiee = declarationService.modifierDeclaration(id, declaration, utilisateurConnecte);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Déclaration modifiée avec succès");
            response.put("declaration", DTOMapper.toDeclarationDTO(declarationModifiee));

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(creerReponseErreur(e.getMessage()));
        }
    }

 // ---------------- Recherche déclarant ----------------
    @PostMapping("/rechercher-declarant")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISEUR','AGENT')")
    public ResponseEntity<?> rechercherDeclarant(@RequestBody RechercherDeclarantRequest request) {
        if ((request.getEmail() == null || request.getEmail().isBlank()) &&
            (request.getNumNina() == null || request.getNumNina().isBlank()) &&
            (request.getNumPassePort() == null || request.getNumPassePort().isBlank()) &&
            (request.getNumCarteIdentite() == null || request.getNumCarteIdentite().isBlank())) {
            return ResponseEntity.badRequest().body("Veuillez fournir au moins un identifiant.");
        }

        Declarant declarant = declarationService.rechercherParIdentifiant(
            request.getEmail(),
            request.getNumNina(),
            request.getNumPassePort(),
            request.getNumCarteIdentite()
        );

        if (declarant == null) {
            return ResponseEntity.status(404).body("Aucun déclarant trouvé avec ces informations.");
        }

        return ResponseEntity.ok(declarant);
    }

    // ---------------- Suppression logique ----------------
    @DeleteMapping("/{id}")
    public ResponseEntity<?> supprimerDeclaration(@PathVariable Long id,
                                                  @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) return ResponseEntity.status(401).body(creerReponseErreur("Utilisateur non authentifié"));

        try {
            Utilisateur utilisateurConnecte = userDetails.getUtilisateur();
            if (!declarationService.peutModifierDeclaration(id, utilisateurConnecte))
                return ResponseEntity.status(403).body(creerReponseErreur("Vous n'avez pas les permissions pour supprimer cette déclaration"));

            Declaration declarationSupprimee = declarationService.supprimerDeclaration(id, utilisateurConnecte);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Déclaration supprimée avec succès");
            response.put("declaration", DTOMapper.toDeclarationDTO(declarationSupprimee));

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(creerReponseErreur(e.getMessage()));
        }
    }

    // ---------------- Restauration ----------------
    @PatchMapping("/{id}/restaurer")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISEUR','AGENT')")
    public ResponseEntity<?> restaurerDeclaration(@PathVariable Long id,
                                                  @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) return ResponseEntity.status(401).body(creerReponseErreur("Utilisateur non authentifié"));

        try {
            Utilisateur utilisateurConnecte = userDetails.getUtilisateur();
            Declaration declaration = declarationService.restaurerDeclaration(id, utilisateurConnecte);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Déclaration restaurée avec succès");
            response.put("declaration", DTOMapper.toDeclarationDTO(declaration));

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(creerReponseErreur(e.getMessage()));
        }
    }

    // ---------------- Suppression définitive ----------------
    @DeleteMapping("/{id}/definitif")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> supprimerDefinitivement(@PathVariable Long id) {
        try {
            declarationService.supprimerDefinitivement(id);
            return ResponseEntity.ok(Map.of("message", "Déclaration supprimée définitivement"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    // ---------------- Mes déclarations actives ----------------
    @GetMapping("/actives")
    public ResponseEntity<?> listerActives(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).body(creerReponseErreur("Utilisateur non authentifié"));

        Utilisateur utilisateurConnecte = userDetails.getUtilisateur();
        List<DeclarationDTO> declarations = declarationService.listerDeclarationsActivesParUtilisateur(utilisateurConnecte.getId())
                .stream().map(DTOMapper::toDeclarationDTO).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("total", declarations.size(), "declarations", declarations));
    }

    // ---------------- Mes déclarations supprimées ----------------
    @GetMapping("/supprimees")
    public ResponseEntity<?> listerSupprimees(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).body(creerReponseErreur("Utilisateur non authentifié"));

        Utilisateur utilisateurConnecte = userDetails.getUtilisateur();
        List<DeclarationDTO> declarations = declarationService.listerDeclarationsSupprimeesParUtilisateur(utilisateurConnecte.getId())
                .stream().map(DTOMapper::toDeclarationDTO).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("total", declarations.size(), "declarations", declarations));
    }

    // ---------------- Déclarations du poste actives ----------------
    @GetMapping("/actives/poste")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISEUR')")
    public ResponseEntity<?> listerActivesPoste(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).body(creerReponseErreur("Utilisateur non authentifié"));

        Utilisateur utilisateurConnecte = userDetails.getUtilisateur();
        List<DeclarationDTO> declarations = declarationService.listerDeclarationsActivesParPoste(utilisateurConnecte.getPostePolice().getId())
                .stream().map(DTOMapper::toDeclarationDTO).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("total", declarations.size(), "declarations", declarations));
    }

    // ---------------- Déclarations du poste supprimées ----------------
    @GetMapping("/supprimees/poste")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISEUR')")
    public ResponseEntity<?> listerSupprimeesPoste(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).body(creerReponseErreur("Utilisateur non authentifié"));

        Utilisateur utilisateurConnecte = userDetails.getUtilisateur();
        List<DeclarationDTO> declarations = declarationService.listerDeclarationsSupprimeesParPoste(utilisateurConnecte.getPostePolice().getId())
                .stream().map(DTOMapper::toDeclarationDTO).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("total", declarations.size(), "declarations", declarations));
    }

    // ---------------- Statistiques ----------------
    @GetMapping("/poste/stats")
    public ResponseEntity<?> getPosteStats(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).body(creerReponseErreur("Utilisateur non authentifié"));

        PosteStatsDTO stats = declarationService.getPosteStats();
        return ResponseEntity.ok(stats);
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<?> changerStatut(
            @PathVariable Long id,
            @RequestParam StatutDeclaration statut,
            @AuthenticationPrincipal Utilisateur utilisateurConnecte
    ) {
        try {
            Declaration updated = declarationService.changerStatut(id, statut, utilisateurConnecte);
            return ResponseEntity.ok(DTOMapper.toDeclarationDTO(updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    // ---------------- Recherche ----------------
    @GetMapping("/reference/{numeroReference}")
    public ResponseEntity<?> trouverParNumeroReference(@PathVariable String numeroReference) {
        try {
            Declaration declaration = declarationService.rechercherParNumeroReference(numeroReference);
            return ResponseEntity.ok(DTOMapper.toDeclarationDTO(declaration));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(creerReponseErreur(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> trouverParId(@PathVariable Long id) {
        try {
            Declaration declaration = declarationService.trouverParId(id);
            return ResponseEntity.ok(DTOMapper.toDeclarationDTO(declaration));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generatePdf(@PathVariable Long id) throws Exception {
        Declaration declaration = declarationService.findById(id);
        if (declaration == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] pdfBytes = PdfGeneratorService.generateDeclarationPdf(declaration);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=declaration-" + id + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    // ---------------- Utilitaire ----------------
    private Map<String,String> creerReponseErreur(String message) {
        Map<String,String> erreur = new HashMap<>();
        erreur.put("erreur", message);
        return erreur;
    }

//    // --- Créer / Enregistrer une déclaration ---
//    @PostMapping("/enregistrer")
//    public ResponseEntity<Declaration> enregistrerDeclaration(@RequestBody Declaration declaration) {
//        Declaration savedDeclaration = declarationService.enregistrerDeclaration(declaration);
//        return ResponseEntity.ok(savedDeclaration);
//    }
}
