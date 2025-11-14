package com.Smtd.GestionPerteDoc.services;

import com.Smtd.GestionPerteDoc.entities.PostePolice;
import com.Smtd.GestionPerteDoc.repositories.PostePoliceRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostePoliceService {

    private final PostePoliceRepository postePoliceRepository;

    public PostePolice creerPostePolice(PostePolice postePolice) {
        try {
            // Validation
            if (postePolice.getNom() == null || postePolice.getNom().trim().isEmpty()) {
                throw new RuntimeException("Le nom du poste est obligatoire");
            }
            
            // Vérifier les doublons
            if (postePoliceRepository.existsByNom(postePolice.getNom())) {
                throw new RuntimeException("Un poste avec ce nom existe déjà");
            }

            // Générer le code unique temporaire
            String tempCode = "POSTE-TEMP-" + System.currentTimeMillis();
            postePolice.setCodeUnique(tempCode);

            // Sauvegarder pour obtenir l'ID
            PostePolice saved = postePoliceRepository.save(postePolice);

            // Générer le code unique définitif basé sur l'ID
            String definitiveCode = "POSTE-" + String.format("%03d", saved.getId());
            saved.setCodeUnique(definitiveCode);

            return postePoliceRepository.save(saved);
            
        } catch (Exception e) {
            throw new RuntimeException("Erreur création poste police: " + e.getMessage());
        }
    }

    public List<PostePolice> listerTousPostes() {
        return postePoliceRepository.findAll();
    }

    public PostePolice trouverParId(Long id) {
        return postePoliceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Poste de police non trouvé"));
    }
}
