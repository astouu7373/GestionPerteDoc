package com.Smtd.GestionPerteDoc.controllers;

import com.Smtd.GestionPerteDoc.dtos.DeclarantDTO;
import com.Smtd.GestionPerteDoc.dtos.DTOMapper;
import com.Smtd.GestionPerteDoc.entities.Declarant;
import com.Smtd.GestionPerteDoc.services.DeclarantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/declarants")
@RequiredArgsConstructor
public class DeclarantController {

    private final DeclarantService declarantService;

    // Créer un déclarant
    @PostMapping
    public ResponseEntity<DeclarantDTO> creerDeclarant(@RequestBody Declarant declarant) {
        Declarant nouveau = declarantService.creerDeclarant(declarant);
        return ResponseEntity.ok(DTOMapper.toDeclarantDTO(nouveau));
    }

    // Récupérer par id
    @GetMapping("/{id}")
    public ResponseEntity<DeclarantDTO> trouverParId(@PathVariable UUID id) {
        Declarant declarant = declarantService.trouverParId(id);
        return ResponseEntity.ok(DTOMapper.toDeclarantDTO(declarant));
    }

    // Lister tous
    @GetMapping
    public ResponseEntity<List<DeclarantDTO>> listerTous() {
        List<DeclarantDTO> declarants = declarantService.listerTousDeclarants()
                .stream()
                .map(DTOMapper::toDeclarantDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(declarants);
    }

    // Supprimer
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable UUID id) {
        declarantService.supprimerDeclarant(id);
        return ResponseEntity.noContent().build();
    }
}
