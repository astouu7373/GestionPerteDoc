package com.Smtd.GestionPerteDoc.controllers;

import com.Smtd.GestionPerteDoc.entities.TypeDocument;
import com.Smtd.GestionPerteDoc.services.TypeDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/types-document")
@RequiredArgsConstructor
public class TypeDocumentController {

    private final TypeDocumentService typeDocumentService;

    @PostMapping
    public ResponseEntity<?> creerTypeDocument(@RequestBody Map<String, Object> requestBody) {
        try {
            String libelle = requestBody.get("libelleTypeDocument") != null ?
                    requestBody.get("libelleTypeDocument").toString() :
                    requestBody.get("libelle") != null ? requestBody.get("libelle").toString() : null;

            if (libelle == null || libelle.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("erreur", "Le libellé ne peut pas être vide"));
            }

            TypeDocument typeDocument = new TypeDocument();
            typeDocument.setLibelleTypeDocument(libelle.trim());

            TypeDocument saved = typeDocumentService.creerTypeDocument(typeDocument);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<TypeDocument>> listerTousTypesDocument() {
        return ResponseEntity.ok(typeDocumentService.listerTousTypesDocument());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> trouverParId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(typeDocumentService.trouverParId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISEUR')")
    public ResponseEntity<?> mettreAJourTypeDocument(@PathVariable Long id, @RequestBody TypeDocument typeDocument) {
        try {
            TypeDocument updated = typeDocumentService.mettreAJourTypeDocument(id, typeDocument);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> supprimerTypeDocument(@PathVariable Long id) {
        try {
            typeDocumentService.supprimerTypeDocument(id);
            return ResponseEntity.ok(Map.of("message", "Type de document supprimé avec succès"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }
}
