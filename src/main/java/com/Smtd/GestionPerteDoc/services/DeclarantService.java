package com.Smtd.GestionPerteDoc.services;

import com.Smtd.GestionPerteDoc.entities.Declarant;
import com.Smtd.GestionPerteDoc.entities.Utilisateur;
import com.Smtd.GestionPerteDoc.repositories.DeclarantRepository;
import com.Smtd.GestionPerteDoc.repositories.UtilisateurRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class DeclarantService {

    private final DeclarantRepository declarantRepository;
    private final UtilisateurRepository utilisateurRepository;

    // ===================== CRÉATION =====================

    /**
     * Crée un déclarant avec vérification d'unicité complète.
     */
    @Transactional
    public Declarant creerDeclarant(Declarant declarant) {
        try {
            // Normalisation des champs
            normaliserChamps(declarant);

            // Vérification de l'unicité email, identifiants et téléphone
            if (declarant.getEmail() != null) verifierEmailUniqueGlobal(declarant);
            verifierIdentifiantsUniques(declarant);
            if (declarant.getTelephone() != null) verifierTelephoneUnique(declarant.getTelephone(), declarant.getId());

            // Sauvegarde en base
            return declarantRepository.save(declarant);

        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Violation de contrainte d'unicité: " + e.getMessage());
        }
    }

    /**
     * Trouve un déclarant existant par identifiants ou le crée si inexistant.
     */
    @Transactional
    public Declarant trouverOuCreerDeclarant(Declarant declarantData) {
        // Normalisation
        normaliserChamps(declarantData);

        //  Recherche par identifiants officiels (NINA, CNI, passeport)
        Optional<Declarant> declarantExistant = trouverDeclarantParIdentifiants(declarantData);
        if (declarantExistant.isPresent()) return declarantExistant.get();

        //  Recherche par email
        if (declarantData.getEmail() != null && !declarantData.getEmail().isBlank()) {
            Optional<Declarant> declarantParEmail = declarantRepository.findByEmail(declarantData.getEmail());
            if (declarantParEmail.isPresent()) return declarantParEmail.get();
        }

        //  Vérifications d'unicité avant création
        if (declarantData.getEmail() != null) verifierEmailUniqueGlobal(declarantData);
        verifierIdentifiantsUniques(declarantData);
        if (declarantData.getTelephone() != null) verifierTelephoneUnique(declarantData.getTelephone(), declarantData.getId());

        //  Création du nouveau déclarant
        Declarant nouveauDeclarant = new Declarant();
        copierChamps(nouveauDeclarant, declarantData);

        return declarantRepository.save(nouveauDeclarant);
    }

    // ===================== MISE À JOUR =====================

    /**
     * Met à jour un déclarant existant avec vérifications d'unicité.
     */
    @Transactional
    public Declarant mettreAJourDeclarant(Declarant declarant) {
        if (declarant.getId() == null) {
            throw new RuntimeException("L'ID du déclarant est requis pour la mise à jour");
        }

        Declarant existing = declarantRepository.findById(declarant.getId())
                .orElseThrow(() -> new RuntimeException("Déclarant non trouvé"));

        // Normalisation et vérifications d'unicité
        normaliserChamps(declarant);
        if (declarant.getEmail() != null) verifierEmailUniqueGlobal(declarant);
        verifierIdentifiantsUniques(declarant);
        if (declarant.getTelephone() != null) verifierTelephoneUnique(declarant.getTelephone(), declarant.getId());

        // Copie des champs vers l'objet existant
        copierChamps(existing, declarant);

        return declarantRepository.save(existing);
    }

    // ===================== SUPPRESSION =====================

    /**
     * Supprime un déclarant par son ID.
     */
    @Transactional
    public void supprimerDeclarant(UUID id) {
        Declarant declarant = declarantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Déclarant non trouvé"));
        declarantRepository.delete(declarant);
    }

    // ===================== LECTURE / LISTE =====================

    public List<Declarant> listerTousDeclarants() {
        return declarantRepository.findAll();
    }

    public Declarant trouverParId(UUID id) {
        return declarantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Déclarant non trouvé"));
    }

    // ===================== RECHERCHE PAR IDENTIFIANTS (PRIVÉ) =====================

    private Optional<Declarant> trouverDeclarantParIdentifiants(Declarant declarant) {
        Optional<Declarant> result = Optional.empty();

        if (declarant.getNumNina() != null) {
            result = declarantRepository.findByNumNina(declarant.getNumNina());
        }
        if (result.isEmpty() && declarant.getNumCarteIdentite() != null) {
            result = declarantRepository.findByNumCarteIdentite(declarant.getNumCarteIdentite());
        }
        if (result.isEmpty() && declarant.getNumPassePort() != null) {
            result = declarantRepository.findByNumPassePort(declarant.getNumPassePort());
        }

        return result;
    }

    // ===================== VALIDATIONS (PRIVÉ) =====================

    private void verifierEmailUniqueGlobal(Declarant declarant) {
        Optional<Declarant> declarantAvecEmail = declarantRepository.findByEmail(declarant.getEmail());
        if (declarantAvecEmail.isPresent() && !declarantAvecEmail.get().getId().equals(declarant.getId())) {
            throw new RuntimeException("Un déclarant avec cet email existe déjà !");
        }

        Optional<Utilisateur> utilisateurAvecEmail = utilisateurRepository.findByEmail(declarant.getEmail());
        if (utilisateurAvecEmail.isPresent()) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà !");
        }
    }

    private void verifierIdentifiantsUniques(Declarant declarant) {
        verifierIdentifiant(declarant.getNumNina(), "NINA", declarant.getId(), declarantRepository::findByNumNina);
        verifierIdentifiant(declarant.getNumCarteIdentite(), "numéro de carte d'identité", declarant.getId(), declarantRepository::findByNumCarteIdentite);
        verifierIdentifiant(declarant.getNumPassePort(), "passeport", declarant.getId(), declarantRepository::findByNumPassePort);
    }

    private void verifierIdentifiant(String valeur, String type, UUID idCourant, Function<String, Optional<Declarant>> finder) {
        if (valeur != null) {
            Optional<Declarant> exist = finder.apply(valeur);
            if (exist.isPresent() && !exist.get().getId().equals(idCourant)) {
                throw new RuntimeException("Un déclarant avec ce " + type + " existe déjà !");
            }
        }
    }

    private void verifierTelephoneUnique(String telephone, UUID idCourant) {
        if (telephone != null) {
            Optional<Declarant> exist = declarantRepository.findByTelephone(telephone);
            if (exist.isPresent() && !exist.get().getId().equals(idCourant)) {
                throw new RuntimeException("Un déclarant avec ce téléphone existe déjà !");
            }
        }
    }

    // ===================== UTILITAIRES (PRIVÉ) =====================

    /**
     * Normalise les champs d'un déclarant (chaînes vides -> null)
     */
    private void normaliserChamps(Declarant declarant) {
        declarant.setNumNina(emptyToNull(declarant.getNumNina()));
        declarant.setNumCarteIdentite(emptyToNull(declarant.getNumCarteIdentite()));
        declarant.setNumPassePort(emptyToNull(declarant.getNumPassePort()));
        declarant.setEmail(emptyToNull(declarant.getEmail()));
        declarant.setTelephone(emptyToNull(declarant.getTelephone()));
        declarant.setNom(emptyToNull(declarant.getNom()));
        declarant.setPrenom(emptyToNull(declarant.getPrenom()));
        declarant.setAdresse(emptyToNull(declarant.getAdresse()));
        declarant.setLieuNaissance(emptyToNull(declarant.getLieuNaissance()));
    }

    /**
     * Copie tous les champs d'un déclarant source vers un déclarant cible.
     */
    private void copierChamps(Declarant cible, Declarant source) {
        cible.setNom(source.getNom());
        cible.setPrenom(source.getPrenom());
        cible.setDateNaissance(source.getDateNaissance());
        cible.setNumNina(source.getNumNina());
        cible.setNumCarteIdentite(source.getNumCarteIdentite());
        cible.setNumPassePort(source.getNumPassePort());
        cible.setEmail(source.getEmail());
        cible.setTelephone(source.getTelephone());
        cible.setAdresse(source.getAdresse());
        cible.setLieuNaissance(source.getLieuNaissance());
    }

    /**
     * Convertit une chaîne vide en null.
     */
    private String emptyToNull(String value) {
        return (value == null || value.trim().isEmpty()) ? null : value.trim();
    }
}
