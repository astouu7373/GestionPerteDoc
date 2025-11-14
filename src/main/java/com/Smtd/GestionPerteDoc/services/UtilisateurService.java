package com.Smtd.GestionPerteDoc.services;

import com.Smtd.GestionPerteDoc.dtos.UtilisateurDTO;

import com.Smtd.GestionPerteDoc.dtos.DTOMapper;
import com.Smtd.GestionPerteDoc.entities.Declaration;
import com.Smtd.GestionPerteDoc.entities.PostePolice;
import com.Smtd.GestionPerteDoc.entities.Role;
import com.Smtd.GestionPerteDoc.entities.Utilisateur;
import com.Smtd.GestionPerteDoc.repositories.PostePoliceRepository;
import com.Smtd.GestionPerteDoc.repositories.RoleRepository;
import com.Smtd.GestionPerteDoc.repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final PostePoliceRepository postePoliceRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // ===================== MOT DE PASSE TEMPORAIRE =====================
    public String genererMotDePasseTemporaire() {
        return RandomStringUtils.randomAlphanumeric(8);
    }

    // ===================== GENERER RESET TOKEN =====================
    public String genererResetToken() {
        return UUID.randomUUID().toString();
    }

    // ===================== CREATION UTILISATEUR SÉCURISÉE =====================
    public Utilisateur creerUtilisateurAvecRoles(Utilisateur utilisateur, Long postePoliceId, List<Long> roleIds) {
        Utilisateur createur = getUtilisateurCourant();
        if (!createur.hasRole("ROLE_ADMIN") && !createur.hasRole("ROLE_SUPERVISEUR")) {
            throw new RuntimeException("Seul un administrateur ou superviseur peut créer un compte utilisateur");
        }

        // ===================== Assigner poste =====================
        PostePolice poste = getOrCreatePostePolice(postePoliceId);
        utilisateur.setPostePolice(poste);

        // ===================== Assigner rôles =====================
        Set<Role> roles = new HashSet<>();
        if (roleIds != null && !roleIds.isEmpty()) {
            roles = roleIds.stream()
                    .map(id -> roleRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("Rôle non trouvé ID: " + id)))
                    .collect(Collectors.toSet());
        }
        utilisateur.setRoles(roles);

        // ===================== Générer mot de passe temporaire =====================
        String motDePasseTemp = genererMotDePasseTemporaire();
        utilisateur.setMotDePasseHash(passwordEncoder.encode(motDePasseTemp));

        // ===================== Valider les champs obligatoires =====================
        validerDonneesUtilisateur(utilisateur);
        verifierDoublons(utilisateur);

        // ===================== Déterminer activation et matricule =====================
        long totalUsers = utilisateurRepository.count();
        if (totalUsers == 0) {
            // Premier utilisateur = admin actif
            utilisateur.setActif(true);
            if (roles.isEmpty()) {
                Role adminRole = roleRepository.findByLibelle("ROLE_ADMIN")
                        .orElseThrow(() -> new RuntimeException("Rôle ROLE_ADMIN introuvable"));
                utilisateur.setRoles(Set.of(adminRole));
                roles = utilisateur.getRoles();
            }
        } else {
            // Utilisateur standard = inactif
            utilisateur.setActif(false);
        }

        // ===================== Générer matricule définitif selon les rôles =====================
        genererMatriculeDefinitif(utilisateur, roles);

        // ===================== Générer resetToken =====================
        String resetToken = UUID.randomUUID().toString();
        utilisateur.setResetToken(resetToken);
        utilisateur.setResetPasswordExpires(LocalDateTime.now().plusHours(48));

        // ===================== Envoyer email =====================
        try {
            emailService.envoyerEmailMotDePasseTemporaire(
                    utilisateur.getEmail(),
                    motDePasseTemp,
                    resetToken
            );
        } catch (Exception e) {
            throw new RuntimeException("Erreur envoi email: " + e.getMessage());
        }

        // ===================== Sauvegarder =====================
        return utilisateurRepository.save(utilisateur);
    }

    // ===================== CREATION INTERNE UTILISATEUR =====================
    private Utilisateur creerUtilisateur(Utilisateur utilisateur, Long postePoliceId) {
        validerDonneesUtilisateur(utilisateur);
        verifierDoublons(utilisateur);
        PostePolice poste = getOrCreatePostePolice(postePoliceId);
        utilisateur.setPostePolice(poste);

        long totalUsers = utilisateurRepository.count();
        if (totalUsers == 0) {
            utilisateur.setActif(true);
            Role adminRole = roleRepository.findByLibelle("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("Rôle ROLE_ADMIN introuvable"));
            utilisateur.setRoles(Set.of(adminRole));
            genererMatriculeDefinitif(utilisateur, utilisateur.getRoles());
        } else {
            utilisateur.setActif(false);
            genererMatriculeDefinitif(utilisateur, utilisateur.getRoles());
        }

        return utilisateurRepository.save(utilisateur);
    }

    // ===================== ACTIVATION ET ATTRIBUTION RÔLES =====================
    public Utilisateur activerEtAttribuerRoleAvecControle(Long utilisateurId, List<Long> roleIds) {
        Utilisateur createur = getUtilisateurCourant();
        if (!createur.hasRole("ROLE_ADMIN") && !createur.hasRole("ROLE_SUPERVISEUR")) {
            throw new RuntimeException("Seul un administrateur ou superviseur peut attribuer des rôles");
        }

        Set<Role> roles = new HashSet<>();
        for (Long roleId : roleIds) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new RuntimeException("Rôle non trouvé ID: " + roleId));
            if ("ROLE_ADMIN".equals(role.getLibelle()) && !createur.hasRole("ROLE_ADMIN")) {
                throw new RuntimeException("Seul l'Admin peut attribuer le rôle ADMIN");
            }
            roles.add(role);
        }

        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé ID: " + utilisateurId));
        utilisateur.setActif(true);
        utilisateur.setRoles(roles);
        genererMatriculeDefinitif(utilisateur, roles);

        return utilisateurRepository.save(utilisateur);
    }

    // ===================== CHANGEMENT MOT DE PASSE AVEC RESET TOKEN =====================
    public void changerMotDePasseAvecToken(String resetToken, String nouveauMotDePasse) {
        Utilisateur utilisateur = utilisateurRepository.findByResetToken(resetToken)
                .orElseThrow(() -> new RuntimeException("Token invalide ou expiré"));

        utilisateur.setMotDePasseHash(passwordEncoder.encode(nouveauMotDePasse));
        utilisateur.setActif(true);
        utilisateur.setResetToken(null);
        utilisateurRepository.save(utilisateur);
    }

    // ===================== DÉSACTIVATION =====================
    public void desactiverUtilisateur(Long utilisateurId) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé ID: " + utilisateurId));

        if (utilisateur.hasRole("ROLE_ADMIN")) {
            long adminsActifs = utilisateurRepository.countByRolesLibelleAndActifTrue("ROLE_ADMIN");
            if (adminsActifs <= 1) {
                throw new RuntimeException("Impossible de désactiver le dernier administrateur");
            }
        }

        utilisateur.setActif(false);
        utilisateurRepository.save(utilisateur);
    }

    // ===================== ENVOYER LIEN RÉINITIALISATION =====================
    public void envoyerLienReinitialisation(String email) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Aucun compte trouvé avec cet email"));

        String token = UUID.randomUUID().toString();
        utilisateur.setResetToken(token);
        utilisateur.setResetPasswordExpires(LocalDateTime.now().plusHours(48));
        utilisateurRepository.save(utilisateur);

        try {
            emailService.envoyerEmailMotDePasseTemporaire(
                    utilisateur.getEmail(),
                    null,
                    token
            );
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email de réinitialisation");
        }
    }

    // ===================== TRANSFERT RÔLE ADMIN =====================
    public Utilisateur transfererRoleAdmin(Long ancienAdminId, Long nouveauAdminId) {
        if (ancienAdminId.equals(nouveauAdminId)) {
            throw new RuntimeException("Impossible de transférer le rôle admin à soi-même");
        }

        Utilisateur ancienAdmin = utilisateurRepository.findById(ancienAdminId)
                .orElseThrow(() -> new RuntimeException("Ancien admin non trouvé ID: " + ancienAdminId));
        if (!ancienAdmin.hasRole("ROLE_ADMIN")) {
            throw new RuntimeException("L'utilisateur n'est pas administrateur");
        }

        Utilisateur nouveauAdmin = utilisateurRepository.findById(nouveauAdminId)
                .orElseThrow(() -> new RuntimeException("Nouvel admin non trouvé ID: " + nouveauAdminId));
        if (nouveauAdmin.hasRole("ROLE_ADMIN")) {
            throw new RuntimeException("L'utilisateur est déjà administrateur");
        }

        Role roleAdmin = roleRepository.findByLibelle("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("Rôle ROLE_ADMIN non trouvé"));

        ancienAdmin.getRoles().removeIf(role -> "ROLE_ADMIN".equals(role.getLibelle()));
        nouveauAdmin.getRoles().add(roleAdmin);

        genererMatriculeDefinitif(ancienAdmin, ancienAdmin.getRoles());
        genererMatriculeDefinitif(nouveauAdmin, nouveauAdmin.getRoles());

        utilisateurRepository.save(ancienAdmin);
        return utilisateurRepository.save(nouveauAdmin);
    }

    // ===================== MISE À JOUR UTILISATEUR =====================
    public Utilisateur mettreAJourUtilisateur(Long utilisateurId, Utilisateur utilisateurDetails, Long postePoliceId, List<Long> roleIds) {
        Utilisateur existant = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé ID: " + utilisateurId));

        PostePolice poste = postePoliceRepository.findById(postePoliceId)
                .orElseThrow(() -> new RuntimeException("Poste non trouvé ID: " + postePoliceId));

        Set<Role> roles = new HashSet<>();
        for (Long roleId : roleIds) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new RuntimeException("Rôle non trouvé ID: " + roleId));
            roles.add(role);
        }

        verifierSuperviseurUniquePourUpdate(postePoliceId, roles, utilisateurId);
        mettreAJourChampsUtilisateur(existant, utilisateurDetails, poste, roles);

        return utilisateurRepository.save(existant);
    }

    // ===================== RECHERCHE / LISTING =====================
    public List<Utilisateur> listerUtilisateursParRole(String roleLibelle) {
        return utilisateurRepository.findByRolesLibelle(roleLibelle);
    }

    public List<Utilisateur> listerAgentsParPoste(Long postePoliceId) {
        return utilisateurRepository.findByPostePoliceIdAndRolesLibelle(postePoliceId, "ROLE_AGENT");
    }

    public Utilisateur trouverSuperviseurParPoste(Long postePoliceId) {
        return utilisateurRepository.findByPostePoliceIdAndRolesLibelle(postePoliceId, "ROLE_SUPERVISEUR")
                .stream().findFirst().orElse(null);
    }

    public List<Utilisateur> listerUtilisateursActifs() {
        return utilisateurRepository.findByActifTrueAndIsDeletedFalse();
    }
    public List<Utilisateur> listerUtilisateursInactifs() {
        return utilisateurRepository.findByActifFalseAndIsDeletedFalse();
    }
    public List<Utilisateur> listerUtilisateursSupprimes() {
        return utilisateurRepository.findByIsDeletedTrue();
    }

    public Utilisateur trouverParId(Long utilisateurId) {
        return utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé ID: " + utilisateurId));
    }

    public List<Utilisateur> listerTousUtilisateurs() {
        return utilisateurRepository.findAll();
    }

 // ===================== SOFT DELETE =====================
    @Transactional
    public void supprimerUtilisateurSoft(Long utilisateurId) {
        // Récupérer l'utilisateur connecté
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Utilisateur actuel;

        if (principal instanceof UserDetails userDetails) {
            actuel = utilisateurRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Utilisateur actuel introuvable"));
        } else {
            throw new RuntimeException("Impossible de récupérer l'utilisateur connecté");
        }

        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Interdiction de suppression de soi-même
        if (utilisateur.getId().equals(actuel.getId())) {
            throw new RuntimeException("Vous ne pouvez pas supprimer votre propre compte !");
        }

        // Vérification : empêcher un SUPERVISEUR de supprimer un ADMIN
        if (utilisateur.hasRole("ROLE_ADMIN") && actuel.hasRole("ROLE_SUPERVISEUR")) {
            throw new RuntimeException("Un superviseur ne peut pas supprimer un utilisateur ADMIN !");
        }

        // Vérification : empêcher la suppression du dernier ADMIN
        if (utilisateur.hasRole("ROLE_ADMIN")) {
            long adminsActifs = utilisateurRepository.countByRolesLibelleAndActifTrue("ROLE_ADMIN");
            if (adminsActifs <= 1) {
                throw new RuntimeException("Impossible de supprimer le dernier administrateur actif !");
            }
        }

        // Marquer comme supprimé et désactiver
        utilisateur.setIsDeleted(true);
        utilisateur.setActif(false);

        utilisateurRepository.save(utilisateur);
    }

    public Utilisateur findByEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email: " + email));
    }

    // ===================== MÉTHODES PRIVÉES =====================
    private void validerDonneesUtilisateur(Utilisateur utilisateur) {
        if (utilisateur.getNom() == null || utilisateur.getNom().trim().isEmpty()) {
            throw new RuntimeException("Le nom est obligatoire");
        }
        if (utilisateur.getPrenom() == null || utilisateur.getPrenom().trim().isEmpty()) {
            throw new RuntimeException("Le prénom est obligatoire");
        }
        if (utilisateur.getEmail() == null || utilisateur.getEmail().trim().isEmpty()) {
            throw new RuntimeException("L'email est obligatoire");
        }
        if (utilisateur.getMotDePasseHash() == null || utilisateur.getMotDePasseHash().trim().isEmpty()) {
            throw new RuntimeException("Le mot de passe est obligatoire");
        }
    }

    private void verifierDoublons(Utilisateur utilisateur) {
        if (utilisateurRepository.existsByEmail(utilisateur.getEmail())) {
            throw new RuntimeException("Email déjà utilisé: " + utilisateur.getEmail());
        }
    }

    private PostePolice getOrCreatePostePolice(Long postePoliceId) {
        List<PostePolice> postesExistants = postePoliceRepository.findAll();
        if (!postesExistants.isEmpty()) return postesExistants.get(0);
        if (postePoliceId != null) return postePoliceRepository.findById(postePoliceId).orElse(creerPostePoliceParDefaut());
        return creerPostePoliceParDefaut();
    }

    private PostePolice creerPostePoliceParDefaut() {
        PostePolice poste = new PostePolice();
        poste.setNom("Poste De Principal- Commissariat Central");
        poste.setAdresse("Adresse principale");
        poste.setTelephone("+223 20 21 22 22");
        poste.setCodeUnique("PP" + System.currentTimeMillis());
        poste.setActif(true);
        return postePoliceRepository.save(poste);
    }

    private void verifierSuperviseurUniquePourUpdate(Long postePoliceId, Set<Role> roles, Long utilisateurId) {
        boolean estSuperviseur = roles.stream().anyMatch(r -> "ROLE_SUPERVISEUR".equals(r.getLibelle()));
        if (estSuperviseur) {
            List<Utilisateur> superviseursExistants = utilisateurRepository.findByPostePoliceIdAndRolesLibelle(postePoliceId, "ROLE_SUPERVISEUR");
            if (!superviseursExistants.isEmpty() && !superviseursExistants.get(0).getId().equals(utilisateurId)) {
                throw new RuntimeException("Un superviseur existe déjà pour ce poste");
            }
        }
    }

    private void genererMatriculeDefinitif(Utilisateur utilisateur, Set<Role> roles) {
        String prefix = "USR-";
        if (roles.stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getLibelle()))) prefix = "ADM-";
        else if (roles.stream().anyMatch(r -> "ROLE_SUPERVISEUR".equals(r.getLibelle()))) prefix = "SUP-";
        else if (roles.stream().anyMatch(r -> "ROLE_AGENT".equals(r.getLibelle()))) prefix = "AGT-";
        utilisateur.setMatricule(genererMatriculeUnique(prefix));
    }

    private String genererMatriculeUnique(String prefix) {
        int maxTentatives = 1000;
        for (int i = 0; i < maxTentatives; i++) {
            String randomPart = RandomStringUtils.randomNumeric(4);
            String matriculePropose = prefix + randomPart;
            if (!utilisateurRepository.existsByMatricule(matriculePropose)) {
                return matriculePropose;
            }
        }
        return prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void mettreAJourChampsUtilisateur(Utilisateur existant, Utilisateur details, PostePolice poste, Set<Role> roles) {
        existant.setNom(details.getNom());
        existant.setPrenom(details.getPrenom());
        existant.setEmail(details.getEmail());
        existant.setPostePolice(poste);
        existant.setRoles(roles);
        existant.setActif(details.isActif());
        if (details.getMotDePasseHash() != null && !details.getMotDePasseHash().isEmpty()) {
            existant.setMotDePasseHash(passwordEncoder.encode(details.getMotDePasseHash()));
        }
    }
    @Transactional
    public UtilisateurDTO restaurerUtilisateur(Long utilisateurId, List<Long> roleIds) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (!utilisateur.isDeleted()) {
            throw new RuntimeException("L'utilisateur n'est pas supprimé");
        }

        // Réactiver le compte
        utilisateur.setIsDeleted(false);
        utilisateur.setActif(true);

        // Remettre les rôles si fournis
        if (roleIds != null && !roleIds.isEmpty()) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(roleIds));
            utilisateur.setRoles(roles);
        }

        utilisateurRepository.save(utilisateur);

        // Retourner le DTO via le mapper
        return DTOMapper.toUtilisateurDTO(utilisateur);
    }



    public Utilisateur getUtilisateurCourant() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Aucun utilisateur authentifié");
        }
        Object principal = authentication.getPrincipal();
        String email = (principal instanceof UserDetails) ? ((UserDetails) principal).getUsername() : principal.toString();
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé : " + email));
    }

    public boolean existeAdmin() {
        return utilisateurRepository.countByRolesLibelle("ROLE_ADMIN") > 0;
    }
}
