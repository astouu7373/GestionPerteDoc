package com.Smtd.GestionPerteDoc.dtos;

import java.util.Date;

import com.Smtd.GestionPerteDoc.enums.StatutDeclaration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeclarationDTO {
    private Long id;
    private String numeroReference;
    private Long typeDocumentId;
    private String typeDocumentLibelle;
    private String numeroDocument;
    private Date datePerte;
    private String lieuPerte;
    private String circonstances;
    private Date dateDeclaration;
    private StatutDeclaration statut;
    private String utilisateurMatricule;
    private String utilisateurNom;
    private String utilisateurPrenom;
    private DeclarantDTO declarant;
    private String creeParNom;
    private Date creeLe;
    private String modifieParNom;
    private String modifieParPrenom; 
    private String modifieParMatricule;
    private Date modifieLe;
    private String supprimeParNom;
    private Date supprimeLe;
}
