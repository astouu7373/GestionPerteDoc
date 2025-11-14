package com.Smtd.GestionPerteDoc.controllers;

import com.Smtd.GestionPerteDoc.dtos.AuthRequest;
import com.Smtd.GestionPerteDoc.entities.Utilisateur;
import com.Smtd.GestionPerteDoc.services.UtilisateurService;
import com.Smtd.GestionPerteDoc.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UtilisateurService utilisateurService;

    // === LOGIN ===
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails.getUsername());

            Utilisateur utilisateur = utilisateurService.findByEmail(request.getEmail());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("message", "Connexion réussie");
            response.put("user", Map.of(
                    "id", utilisateur.getId(),
                    "email", utilisateur.getEmail(),
                    "nom", utilisateur.getNom(),
                    "prenom", utilisateur.getPrenom(),
                    "matricule", utilisateur.getMatricule(),
                    "actif", utilisateur.isActif(),
                    "roles", utilisateur.getRoles().stream()
                            .map(role -> role.getLibelle())
                            .collect(Collectors.toList())
            ));

            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("erreur", "Email ou mot de passe invalide");
            return ResponseEntity.status(401).body(errorResponse);
        }
    }

    // === SUPPRIMÉ: register / auto-inscription ===
}
