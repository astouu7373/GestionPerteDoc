package com.Smtd.GestionPerteDoc.services;

import com.Smtd.GestionPerteDoc.dtos.PosteStatsDTO;
import com.Smtd.GestionPerteDoc.entities.Declarant;
import com.Smtd.GestionPerteDoc.entities.Declaration;
import com.Smtd.GestionPerteDoc.entities.TypeDocument;
import com.Smtd.GestionPerteDoc.entities.Utilisateur;
import com.Smtd.GestionPerteDoc.enums.StatutDeclaration;
import com.Smtd.GestionPerteDoc.repositories.DeclarantRepository;
import com.Smtd.GestionPerteDoc.repositories.DeclarationRepository;
import com.Smtd.GestionPerteDoc.repositories.TypeDocumentRepository;
import com.Smtd.GestionPerteDoc.repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DeclarationService {

    private final DeclarationRepository declarationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final DeclarantRepository declarantRepository;
    private final TypeDocumentRepository typeDocumentRepository;
    private final DeclarantService declarantService;
    private final NumeroReferenceService numeroReferenceService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UtilisateurService utilisateurService; 

 // === CRÉATION DÉCLARATION ===
    public Declaration creerDeclaration(Declaration declaration, Utilisateur utilisateurConnecte) {
        if (!verifierPermissionsCreation(utilisateurConnecte)) {
            throw new RuntimeException("Permission refusée - Rôles autorisés: ADMIN, SUPERVISEUR, AGENT.");
        }

        if (declaration.getDeclarant() == null) 
            throw new RuntimeException("Le déclarant est obligatoire");

        Declarant declarant = declaration.getDeclarant();
        if (declarant.getNom() == null || declarant.getNom().trim().isEmpty())
            throw new RuntimeException("Le nom du déclarant est obligatoire");
        if (declarant.getPrenom() == null || declarant.getPrenom().trim().isEmpty())
            throw new RuntimeException("Le prénom du déclarant est obligatoire");
        if (declarant.getTelephone() == null || declarant.getTelephone().trim().isEmpty())
            throw new RuntimeException("Le téléphone du déclarant est obligatoire");

        // --- Utilisation du déclarant existant ou création ---
        Declarant declarantFinal = declarantService.trouverOuCreerDeclarant(declarant);
        declaration.setDeclarant(declarantFinal);

        TypeDocument typeDocument = typeDocumentRepository.findById(declaration.getTypeDocument().getId())
                .orElseThrow(() -> new RuntimeException("Type de document non trouvé avec ID: " + declaration.getTypeDocument().getId()));
        declaration.setTypeDocument(typeDocument);

        declaration.setUtilisateur(utilisateurConnecte);
        declaration.setCreePar(utilisateurConnecte);
        declaration.setCreeLe(new Date());
        declaration.setDateDeclaration(new Date());
        declaration.setSupprime(false);

        if (declaration.getStatut() == null) 
            declaration.setStatut(StatutDeclaration.ENREGISTREE);
        if (declaration.getNumeroReference() == null || declaration.getNumeroReference().isEmpty())
            declaration.setNumeroReference(numeroReferenceService.generateNumeroReference());

        Declaration saved = declarationRepository.save(declaration);
        declarationRepository.flush();

        // === ENVOI EMAIL AVEC PDF ===
        try {
            byte[] pdfBytes = PdfGeneratorService.generateDeclarationPdf(saved);
            if (saved.getDeclarant().getEmail() != null && !saved.getDeclarant().getEmail().isEmpty()) {
                emailService.envoyerEmailAvecPdf(saved.getDeclarant().getEmail(), pdfBytes, saved.getNumeroReference());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return declarationRepository.findById(saved.getId())
                .orElseThrow(() -> new RuntimeException("Échec de la persistance - Déclaration non retrouvée après sauvegarde"));
    }


    // === MODIFICATION DÉCLARATION ===
    public Declaration modifierDeclaration(Long declarationId, Declaration nouvelleDeclaration, Utilisateur utilisateurConnecte) {
        Declaration declaration = declarationRepository.findByIdWithModifiePar(declarationId)
                .orElseThrow(() -> new RuntimeException("Déclaration non trouvée"));

        if (nouvelleDeclaration.getLieuPerte() != null) declaration.setLieuPerte(nouvelleDeclaration.getLieuPerte());
        if (nouvelleDeclaration.getDatePerte() != null) declaration.setDatePerte(nouvelleDeclaration.getDatePerte());
        if (nouvelleDeclaration.getCirconstances() != null) declaration.setCirconstances(nouvelleDeclaration.getCirconstances());
        if (nouvelleDeclaration.getNumeroDocument() != null) declaration.setNumeroDocument(nouvelleDeclaration.getNumeroDocument());
        if (nouvelleDeclaration.getTypeDocument() != null) declaration.setTypeDocument(nouvelleDeclaration.getTypeDocument());
        if (nouvelleDeclaration.getStatut() != null) declaration.setStatut(nouvelleDeclaration.getStatut());

        if (nouvelleDeclaration.getDeclarant() != null) {
            Declarant declarantMisAJour = nouvelleDeclaration.getDeclarant();
            if (declaration.getDeclarant() != null) {
                declarantMisAJour.setId(declaration.getDeclarant().getId());
                declarantService.mettreAJourDeclarant(declarantMisAJour);
            } else {
                Declarant savedDeclarant = declarantService.creerDeclarant(declarantMisAJour);
                declaration.setDeclarant(savedDeclarant);
            }
        }

        declaration.setModifiePar(utilisateurConnecte);
        declaration.setModifieLe(new Date());

        Declaration saved = declarationRepository.save(declaration);
        declarationRepository.flush();

        return saved;
    }

    // === SUPPRESSION LOGIQUE ===
    public Declaration supprimerDeclaration(Long declarationId, Utilisateur utilisateurConnecte) {
        Declaration declaration = declarationRepository.findById(declarationId)
                .orElseThrow(() -> new RuntimeException("Déclaration non trouvée"));

        declaration.setSupprime(true);
        declaration.setSupprimePar(utilisateurConnecte);
        declaration.setSupprimeLe(new Date());

        Declaration saved = declarationRepository.save(declaration);
        declarationRepository.flush();

        return saved;
    }

    // === RESTAURATION ===
    public Declaration restaurerDeclaration(Long declarationId, Utilisateur utilisateurConnecte) {
        Declaration declaration = declarationRepository.findById(declarationId)
                .orElseThrow(() -> new RuntimeException("Déclaration non trouvée"));

        if (!declaration.isSupprime()) throw new RuntimeException("Cette déclaration n'est pas supprimée");

        declaration.setSupprime(false);
        declaration.setModifiePar(utilisateurConnecte);
        declaration.setModifieLe(new Date());

        return declarationRepository.save(declaration);
    }

    // === SUPPRESSION DÉFINITIVE ===
    public void supprimerDefinitivement(Long id) {
        Declaration declaration = declarationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Déclaration introuvable"));
        declarationRepository.delete(declaration);
    }

    // === RECHERCHE ===
    public Declaration trouverParId(Long id) {
        return declarationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Déclaration non trouvée"));
    }

    public Declaration rechercherParNumeroReference(String numeroReference) {
        return declarationRepository.findByNumeroReferenceWithDetails(numeroReference)
                .orElseThrow(() -> new RuntimeException("Déclaration introuvable: " + numeroReference));
    }

    // === LISTE DÉCLARATIONS ===
    public List<Declaration> listerToutesDeclarations() {
        return declarationRepository.findAllWithDetails();
    }

    public List<Declaration> listerDeclarationsActivesParUtilisateur(Long utilisateurId) {
        return declarationRepository.findByUtilisateurIdAndSupprimeFalseWithDetails(utilisateurId);
    }

    public List<Declaration> listerDeclarationsSupprimeesParUtilisateur(Long utilisateurId) {
        return declarationRepository.findByUtilisateurIdAndSupprimeTrueWithDetails(utilisateurId);
    }

    public List<Declaration> listerDeclarationsActivesParPoste(Long posteId) {
        return declarationRepository.findByUtilisateurPostePoliceIdAndSupprimeFalseWithDetails(posteId);
    }

    public List<Declaration> listerDeclarationsSupprimeesParPoste(Long posteId) {
        return declarationRepository.findByUtilisateurPostePoliceIdAndSupprimeTrueWithDetails(posteId);
    }

    public List<Declaration> listerDeclarationsSupprimees() {
        List<Declaration> supprimees = declarationRepository.findBySupprimeTrue();
        return supprimees != null ? supprimees : new ArrayList<>();
    }

    // === STATISTIQUES ===
    public PosteStatsDTO getPosteStats() {
        PosteStatsDTO stats = new PosteStatsDTO();
        long totalDeclarations = declarationRepository.countBySupprimeFalse();
        long declarationsValidees = declarationRepository.countByStatutAndSupprimeFalse(StatutDeclaration.VALIDEE);
        long declarationsEnregistrees = declarationRepository.countByStatutAndSupprimeFalse(StatutDeclaration.ENREGISTREE);
        long declarationsRejetees = declarationRepository.countByStatutAndSupprimeFalse(StatutDeclaration.REJETEE);
        long declarationsBrouillons = declarationRepository.countByStatutAndSupprimeFalse(StatutDeclaration.BROUILLON);
        long declarationsSupprimees = declarationRepository.countBySupprimeTrue();

        long totalCalculated = declarationsValidees + declarationsEnregistrees + declarationsRejetees + declarationsBrouillons;
        if (totalDeclarations != totalCalculated) totalDeclarations = totalCalculated;

        stats.setTotalDeclarations(totalDeclarations);
        stats.setDeclarationsValidees(declarationsValidees);
        stats.setDeclarationsEnregistrees(declarationsEnregistrees);
        stats.setDeclarationsRejetees(declarationsRejetees);
        stats.setDeclarationsBrouillons(declarationsBrouillons);
        stats.setDeclarationsSupprimees(declarationsSupprimees);

//        double tauxTraitement = totalDeclarations > 0 ? ((double) declarationsValidees / totalDeclarations) * 100 : 0;
//        stats.setTauxTraitement(Math.round(tauxTraitement * 100.0) / 100.0);

        return stats;
    }

    public PosteStatsDTO getUserStats(Long utilisateurId) {
        PosteStatsDTO stats = new PosteStatsDTO();
        long totalDeclarations = declarationRepository.countByUtilisateurIdAndSupprimeFalse(utilisateurId);
        long declarationsValidees = declarationRepository.countByUtilisateurIdAndStatutAndSupprimeFalse(utilisateurId, StatutDeclaration.VALIDEE);
        long declarationsEnregistrees = declarationRepository.countByUtilisateurIdAndStatutAndSupprimeFalse(utilisateurId, StatutDeclaration.ENREGISTREE);
        long declarationsRejetees = declarationRepository.countByUtilisateurIdAndStatutAndSupprimeFalse(utilisateurId, StatutDeclaration.REJETEE);
        long declarationsBrouillons = declarationRepository.countByUtilisateurIdAndStatutAndSupprimeFalse(utilisateurId, StatutDeclaration.BROUILLON);
        long declarationsSupprimees = declarationRepository.countByUtilisateurIdAndSupprimeTrue(utilisateurId);

        stats.setTotalDeclarations(totalDeclarations);
        stats.setDeclarationsValidees(declarationsValidees);
        stats.setDeclarationsEnregistrees(declarationsEnregistrees);
        stats.setDeclarationsRejetees(declarationsRejetees);
        stats.setDeclarationsBrouillons(declarationsBrouillons);
        stats.setDeclarationsSupprimees(declarationsSupprimees);

//        double tauxTraitement = totalDeclarations > 0 ? ((double) declarationsValidees / totalDeclarations) * 100 : 0;
//        stats.setTauxTraitement(Math.round(tauxTraitement * 100.0) / 100.0);

        return stats;
    }

    public Declarant rechercherParIdentifiant(String email, String numNina, String numPassePort, String numCarteIdentite) {
        if (email != null && !email.isBlank()) {
            return declarantRepository.findByEmail(email).orElse(null);
        } else if (numNina != null && !numNina.isBlank()) {
            return declarantRepository.findByNumNina(numNina).orElse(null);
        } else if (numPassePort != null && !numPassePort.isBlank()) {
            return declarantRepository.findByNumPassePort(numPassePort).orElse(null);
        } else if (numCarteIdentite != null && !numCarteIdentite.isBlank()) {
            return declarantRepository.findByNumCarteIdentite(numCarteIdentite).orElse(null);
        } else {
            return null;
        }
    }
 
    // === CHANGER STATUT ===
    public Declaration changerStatut(Long declarationId, StatutDeclaration nouveauStatut, Utilisateur utilisateurConnecte) {
        Declaration declaration = declarationRepository.findById(declarationId)
                .orElseThrow(() -> new RuntimeException("Déclaration non trouvée"));

        declaration.setStatut(nouveauStatut);
        declaration.setModifiePar(utilisateurConnecte);
        declaration.setModifieLe(new Date());

        declarationRepository.save(declaration);
        return declarationRepository.findByIdWithDetails(declaration.getId())
                .orElseThrow(() -> new RuntimeException("Déclaration introuvable après mise à jour"));

    }

    // === PERMISSIONS ===
    public boolean verifierPermissionsCreation(Utilisateur utilisateur) {
        return utilisateur.hasRole("ROLE_ADMIN") ||
               utilisateur.hasRole("ROLE_SUPERVISEUR") ||
               utilisateur.hasRole("ROLE_AGENT");
    }

    public boolean peutModifierDeclaration(Long declarationId, Utilisateur utilisateur) {
        Declaration declaration = declarationRepository.findById(declarationId)
                .orElseThrow(() -> new RuntimeException("Déclaration non trouvée"));

        return utilisateur.hasRole("ROLE_ADMIN") ||
               utilisateur.hasRole("ROLE_SUPERVISEUR") ||
               declaration.getUtilisateur().getId().equals(utilisateur.getId()) ||
               declaration.getCreePar().getId().equals(utilisateur.getId());
    }

    public boolean verifierSuppression(Long declarationId) {
        Declaration declaration = declarationRepository.findById(declarationId).orElse(null);
        return declaration != null && declaration.isSupprime();
    }

    public Declaration findById(Long id) {
        return declarationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Déclaration non trouvée avec l'ID " + id));
    }

    public Declaration enregistrerDeclaration(Declaration declaration) {
        Utilisateur utilisateurCourant = utilisateurService.getUtilisateurCourant();
      declaration.setCreePar(utilisateurCourant);
        declaration.setCreeLe(new Date());
      return declarationRepository.save(declaration);
   }
}
