package com.Smtd.GestionPerteDoc.dtos;

import lombok.Data;
import java.util.List;

@Data
public class UtilisateurDTO {
    private Long id;
    private String matricule;
    private String nom;
    private String prenom;
    private String email;
    private boolean actif;
    private List<String> roles;
    private Long postePoliceId;
    private String postePoliceNom;

    public UtilisateurDTO() {}

    public UtilisateurDTO(Long id, String matricule, String nom, String prenom, String email, 
                         boolean actif, List<String> roles, Long postePoliceId, String postePoliceNom) {
        this.id = id;
        this.matricule = matricule;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.actif = actif;
        this.roles = roles;
        this.postePoliceId = postePoliceId;
        this.postePoliceNom = postePoliceNom;
    }
}