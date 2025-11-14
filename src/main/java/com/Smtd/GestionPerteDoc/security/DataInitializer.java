/*package com.Smtd.GestionPerteDoc.security;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.Smtd.GestionPerteDoc.entities.Utilisateur;
import com.Smtd.GestionPerteDoc.repositories.UtilisateurRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner hashExistingPasswords() {
        return args -> {
            List<Utilisateur> users = utilisateurRepository.findAll();
            for (Utilisateur u : users) {
                // Hash uniquement si le mot de passe n'est pas déjà hashé (commence par $2a$ pour BCrypt)
                if (!u.getMotDePasseHash().startsWith("$2a$")) {
                    u.setMotDePasseHash(passwordEncoder.encode(u.getMotDePasseHash()));
                }
            }
            utilisateurRepository.saveAll(users);
        };
    }

}*/
//package com;






