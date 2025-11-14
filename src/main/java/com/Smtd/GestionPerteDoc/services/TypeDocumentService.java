package com.Smtd.GestionPerteDoc.services;

import com.Smtd.GestionPerteDoc.entities.TypeDocument;
import com.Smtd.GestionPerteDoc.repositories.TypeDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TypeDocumentService {

    private final TypeDocumentRepository typeDocumentRepository;

    @Transactional
    public TypeDocument creerTypeDocument(TypeDocument typeDocument) {
        try {
            if (typeDocument.getLibelleTypeDocument() == null || typeDocument.getLibelleTypeDocument().trim().isEmpty()) {
                throw new RuntimeException("Le libellé du type de document est requis");
            }

            String libelleNettoye = typeDocument.getLibelleTypeDocument().trim();
            typeDocument.setLibelleTypeDocument(libelleNettoye);

            
            if (typeDocumentRepository.findByLibelleTypeDocument(libelleNettoye).isPresent()) {
                throw new RuntimeException("Un type de document avec ce libellé existe déjà: '" + libelleNettoye + "'");
            }

            
            if (typeDocument.getCodeTypeDocument() == null || typeDocument.getCodeTypeDocument().isEmpty()) {
                typeDocument.setCodeTypeDocument(genererCodeTypeDocument(libelleNettoye));
            }

           
            if (typeDocumentRepository.findByCodeTypeDocument(typeDocument.getCodeTypeDocument()).isPresent()) {
                throw new RuntimeException("Un type de document avec ce code existe déjà: '" + typeDocument.getCodeTypeDocument() + "'");
            }

            return typeDocumentRepository.save(typeDocument);

        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Erreur de contrainte d'unicité dans la base de données");
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la création du type de document: " + e.getMessage());
        }
    }

    private String genererCodeTypeDocument(String nomTypeDocument) {
        String code = nomTypeDocument.toUpperCase()
                .replaceAll("[^A-Z0-9\\s]", "")
                .replaceAll("\\s+", "_");

        if (code.length() > 20) code = code.substring(0, 20);
        if (code.isEmpty()) code = "DOC_" + System.currentTimeMillis();
        return code;
    }

    public List<TypeDocument> listerTousTypesDocument() {
        return typeDocumentRepository.findAll();
    }

    public TypeDocument trouverParId(Long id) {
        return typeDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Type de document non trouvé avec l'ID: " + id));
    }

    public Optional<TypeDocument> trouverParLibelle(String libelle) {
        return typeDocumentRepository.findByLibelleTypeDocument(libelle);
    }

    @Transactional
    public TypeDocument mettreAJourTypeDocument(Long id, TypeDocument typeDocument) {
        TypeDocument existing = typeDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Type de document non trouvé"));

       
        if(typeDocument.getLibelleTypeDocument() != null) {
            String nouveauLibelle = typeDocument.getLibelleTypeDocument().trim();
            if(!nouveauLibelle.equals(existing.getLibelleTypeDocument())) {
             
                if (typeDocumentRepository.findByLibelleTypeDocument(nouveauLibelle).isPresent()) {
                    throw new RuntimeException("Un type de document avec ce libellé existe déjà: '" + nouveauLibelle + "'");
                }
                existing.setLibelleTypeDocument(nouveauLibelle);
            }
        }
        
        // Vérifier si le code a changé et s'il est unique
        if(typeDocument.getCodeTypeDocument() != null && !typeDocument.getCodeTypeDocument().isEmpty()) {
            String nouveauCode = typeDocument.getCodeTypeDocument().trim();
            if(!nouveauCode.equals(existing.getCodeTypeDocument())) {
                // Vérifier unicité du nouveau code
                if (typeDocumentRepository.findByCodeTypeDocument(nouveauCode).isPresent()) {
                    throw new RuntimeException("Un type de document avec ce code existe déjà: '" + nouveauCode + "'");
                }
                existing.setCodeTypeDocument(nouveauCode);
            }
        }

        return typeDocumentRepository.save(existing);
    }

    @Transactional
    public void supprimerTypeDocument(Long id) {
        TypeDocument existing = typeDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Type de document non trouvé"));
        typeDocumentRepository.delete(existing);
    }
}
