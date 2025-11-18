package com.Smtd.GestionPerteDoc.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Getter
@Setter
@ToString(exclude = {"roles", "declarations"})
@Table(name = "utilisateurs")
public class Utilisateur {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "matricule", nullable = false, unique = true, length = 50)
    private String matricule;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "mot_de_passe_hash", nullable = false, length = 255)
    private String motDePasseHash;

    @Column(name = "actif", nullable = false)
    private boolean actif = true;

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "reset_password_expires")
    private LocalDateTime resetPasswordExpires;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poste_id", nullable = false)
    private PostePolice postePolice;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "utilisateur_role",
        joinColumns = @JoinColumn(name = "utilisateur_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Declaration> declarations;

    // Getters et Setters pour les propriétés non couvertes par Lombok
    public boolean isDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(boolean isdeleted) {
        this.isDeleted = isdeleted;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public LocalDateTime getResetPasswordExpires() {
        return resetPasswordExpires;
    }

    public void setResetPasswordExpires(LocalDateTime resetPasswordExpires) {
        this.resetPasswordExpires = resetPasswordExpires;
    }

    public String getRolesLibelles() {
        return roles.stream()
                .map(Role::getLibelle)
                .collect(Collectors.joining(", "));
    }

    public boolean hasRole(String roleLibelle) {
        return roles.stream()
                .anyMatch(role -> role.getLibelle().equals(roleLibelle));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Utilisateur)) return false;
        return id != null && id.equals(((Utilisateur) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
