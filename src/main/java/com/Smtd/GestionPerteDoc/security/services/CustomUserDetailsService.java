package com.Smtd.GestionPerteDoc.security.services;

import com.Smtd.GestionPerteDoc.entities.Utilisateur;
import com.Smtd.GestionPerteDoc.repositories.UtilisateurRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        if (!utilisateur.isActif() || utilisateur.isDeleted()) {
            throw new UsernameNotFoundException("Compte désactivé ou supprimé");
        }

        return new CustomUserDetails(utilisateur);
    }
}
