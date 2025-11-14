package com.Smtd.GestionPerteDoc.controllers;

import com.Smtd.GestionPerteDoc.dtos.DTOMapper;
import com.Smtd.GestionPerteDoc.dtos.PostePoliceDTO;
import com.Smtd.GestionPerteDoc.entities.PostePolice;
import com.Smtd.GestionPerteDoc.services.PostePoliceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/postes-police")
@RequiredArgsConstructor
public class PostePoliceController {

    private final PostePoliceService postePoliceService;
    @PostMapping
    public ResponseEntity<PostePoliceDTO> creerPostePolice(@RequestBody PostePolice postePolice) {
        PostePolice nouveauPoste = postePoliceService.creerPostePolice(postePolice);
        return ResponseEntity.ok(DTOMapper.toPostePoliceDTO(nouveauPoste));
    }

    @GetMapping
    public ResponseEntity<List<PostePoliceDTO>> listerTous() {
        List<PostePoliceDTO> dtos = postePoliceService.listerTousPostes()
                .stream()
                .map(DTOMapper::toPostePoliceDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostePoliceDTO> trouverParId(@PathVariable Long id) {
        PostePolice poste = postePoliceService.trouverParId(id);
        return ResponseEntity.ok(DTOMapper.toPostePoliceDTO(poste));
    }
}
