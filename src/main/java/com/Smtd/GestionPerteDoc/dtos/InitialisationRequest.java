package com.Smtd.GestionPerteDoc.dtos;

import lombok.Data;

@Data
public class InitialisationRequest {

    // Poste
    private String posteNom;
    private String adresse;
    private String posteTelephone;

    // Admin
    private String nomAdmin;
    private String prenomAdmin;
    private String email;
    private String motDePasse;
}
