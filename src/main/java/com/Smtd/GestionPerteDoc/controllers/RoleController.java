package com.Smtd.GestionPerteDoc.controllers;

import com.Smtd.GestionPerteDoc.dtos.DTOMapper;
import com.Smtd.GestionPerteDoc.dtos.RoleDTO;
import com.Smtd.GestionPerteDoc.entities.Role;
import com.Smtd.GestionPerteDoc.services.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<RoleDTO> creerRole(@RequestBody Role role) {
        Role nouveauRole = roleService.creerRole(role);
        return ResponseEntity.ok(DTOMapper.toRoleDTO(nouveauRole));
    }

    @GetMapping
    public ResponseEntity<List<RoleDTO>> listerTousRoles() {
        List<RoleDTO> dtos = roleService.listerTousRoles()
                                        .stream()
                                        .map(DTOMapper::toRoleDTO)
                                        .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleDTO> trouverParId(@PathVariable Long id) {
        Role role = roleService.trouverParId(id);
        return ResponseEntity.ok(DTOMapper.toRoleDTO(role));
    }

    @GetMapping("/libelle/{libelle}")
    public ResponseEntity<RoleDTO> trouverParLibelle(@PathVariable String libelle) {
        Role role = roleService.trouverParLibelle(libelle);
        return ResponseEntity.ok(DTOMapper.toRoleDTO(role));
    }
}
