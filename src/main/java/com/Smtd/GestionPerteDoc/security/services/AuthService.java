/*package com.Smtd.GestionPerteDoc.security.services;

import com.Smtd.GestionPerteDoc.entities.Utilisateur;
import com.Smtd.GestionPerteDoc.repositories.UtilisateurRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    private final String jwtSecret = "secretKeySuperSecrete"; // à sécuriser en production
    private final long jwtExpirationMs = 24 * 60 * 60 * 1000; // 1 jour

    public String authenticate(String email, String motDePasse) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (!passwordEncoder.matches(motDePasse, utilisateur.getMotDePasseHash())) {
            throw new RuntimeException("Mot de passe incorrect");
        }

        return generateToken(utilisateur);
    }

    private String generateToken(Utilisateur utilisateur) {
        return Jwts.builder()
                .setSubject(utilisateur.getEmail())
                .claim("role", utilisateur.getRole().getLibelle())
                .claim("id", utilisateur.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }
}
*/



