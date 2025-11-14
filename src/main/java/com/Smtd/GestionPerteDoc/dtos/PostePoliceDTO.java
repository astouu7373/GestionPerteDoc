package com.Smtd.GestionPerteDoc.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostePoliceDTO {
    private Long id;
    private String codeUnique;
    private String nom;
    private String adresse;
    private String telephone;
    private boolean actif;
}
