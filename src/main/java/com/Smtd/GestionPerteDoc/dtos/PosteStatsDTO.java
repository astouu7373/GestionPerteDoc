package com.Smtd.GestionPerteDoc.dtos;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PosteStatsDTO {
    private long totalDeclarations;
    private long declarationsValidees;
    private long declarationsEnregistrees;
    private long declarationsBrouillons;
    private long declarationsRejetees;
    private long declarationsSupprimees;
    private double tauxTraitement;
   

    // Méthodes supplémentaires utiles
    @Override
    public String toString() {
       
		return "PosteStatsDTO{" +
                "totalDeclarations=" + totalDeclarations +
                ", declarationsValidees=" + declarationsValidees +
                ", declarationsEnregistrees=" + declarationsEnregistrees +
                ", declarationsBrouillons=" + declarationsBrouillons+
                ", declarationsRejetees=" + declarationsRejetees+
                ", declarationsSupprimees=" +declarationsSupprimees+
                ", tauxTraitement=" + tauxTraitement +
                '}';
    }
}