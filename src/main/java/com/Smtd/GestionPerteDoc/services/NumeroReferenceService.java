package com.Smtd.GestionPerteDoc.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.Smtd.GestionPerteDoc.entities.Declaration;
import com.Smtd.GestionPerteDoc.repositories.DeclarationRepository;

@Service
public class NumeroReferenceService {

    @Autowired
    private DeclarationRepository declarationRepository;

    public String generateNumeroReference() {
        Declaration lastDeclaration = declarationRepository.findTopByOrderByNumeroReferenceDesc();
        int nextNumber = 1;

        if (lastDeclaration != null && lastDeclaration.getNumeroReference() != null) {
            String lastRef = lastDeclaration.getNumeroReference(); 
            if (lastRef.startsWith("DECL-BKO-")) {
                String numberPart = lastRef.substring("DECL-BKO-".length());
                try {
                    nextNumber = Integer.parseInt(numberPart) + 1;
                } catch (NumberFormatException e) {
                    nextNumber = 1; 
                }
            }
        }

        return String.format("DECL-BKO-%03d", nextNumber);
    }
}
