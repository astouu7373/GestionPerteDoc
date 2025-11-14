package com.Smtd.GestionPerteDoc.dtos;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeclarantDTO {  
    private String nom;
    private String prenom;
    private String telephone;
    private String numNina;
    private String numPasseport;
    private String numCarteIdentite;
    private String email;
    private String adresse;
    private Date dateNaissance;
    private String lieuNaissance;
}
