package com.Smtd.GestionPerteDoc.dtos;

import lombok.Data;
import java.util.List;

@Data
public class UtilisateurResumeDTO {
    private Long id;
    private String nom;
    private String prenom;
    private List<String> roles;

    
    public UtilisateurResumeDTO(Long id, String nom, String prenom, List<String> roles) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.roles = roles;
    }

   
    public UtilisateurResumeDTO() {
    }
}