package com.Smtd.GestionPerteDoc.dtos;

import com.Smtd.GestionPerteDoc.dtos.PostePoliceDTO;
import com.Smtd.GestionPerteDoc.dtos.RoleDTO;
import com.Smtd.GestionPerteDoc.dtos.UtilisateurDTO;
import com.Smtd.GestionPerteDoc.dtos.UtilisateurResumeDTO;
import com.Smtd.GestionPerteDoc.entities.Declarant;
import com.Smtd.GestionPerteDoc.entities.PostePolice;
import com.Smtd.GestionPerteDoc.entities.Role;
import com.Smtd.GestionPerteDoc.entities.Utilisateur;
import com.Smtd.GestionPerteDoc.entities.Declaration;

import java.util.List;
import java.util.stream.Collectors;

public class DTOMapper {

    // Utilisateur -> UtilisateurDTO (ManyToMany)
    public static UtilisateurDTO toUtilisateurDTO(Utilisateur utilisateur) {
        List<String> rolesLibelles = utilisateur.getRoles().stream()
                .map(Role::getLibelle)
                .collect(Collectors.toList());

        return new UtilisateurDTO(
            utilisateur.getId(),
            utilisateur.getMatricule(),
            utilisateur.getNom(),
            utilisateur.getPrenom(),
            utilisateur.getEmail(),
            utilisateur.isActif(),
            rolesLibelles, 
            utilisateur.getPostePolice().getId(),
            utilisateur.getPostePolice().getNom()
        );
    }

    public static DeclarationDTO toDeclarationDTO(Declaration declaration) {
        if (declaration == null) return null;

        DeclarantDTO declarantDTO = null;
        if (declaration.getDeclarant() != null) {
            declarantDTO = new DeclarantDTO(
                declaration.getDeclarant().getNom(),
                declaration.getDeclarant().getPrenom(),
                declaration.getDeclarant().getTelephone(),
                declaration.getDeclarant().getNumNina(),
                declaration.getDeclarant().getNumPassePort(),
                declaration.getDeclarant().getNumCarteIdentite(),
                declaration.getDeclarant().getEmail(),
                declaration.getDeclarant().getAdresse(),
                declaration.getDeclarant().getDateNaissance(),
                declaration.getDeclarant().getLieuNaissance()
            );  
        }

        return new DeclarationDTO(
        	    declaration.getId(),
        	    declaration.getNumeroReference(),
        	    declaration.getTypeDocument() != null ? declaration.getTypeDocument().getId() : null,
        	    declaration.getTypeDocument() != null ? declaration.getTypeDocument().getLibelleTypeDocument() : null,
        	    declaration.getNumeroDocument(),
        	    declaration.getDatePerte(),
        	    declaration.getLieuPerte(),
        	    declaration.getCirconstances(),
        	    declaration.getDateDeclaration(),
        	    declaration.getStatut(),
        	    declaration.getUtilisateur() != null ? declaration.getUtilisateur().getMatricule() : null,
        	    declaration.getUtilisateur() != null ? declaration.getUtilisateur().getNom() : null,
        	    declaration.getUtilisateur() != null ? declaration.getUtilisateur().getPrenom() : null,
        	    declarantDTO,
        	    declaration.getCreePar() != null ? declaration.getCreePar().getNom() : null,
        	    declaration.getCreeLe(),
        	    declaration.getModifiePar() != null ? declaration.getModifiePar().getNom() : null,
        	    declaration.getModifiePar() != null ? declaration.getModifiePar().getPrenom() : null, 
        	    declaration.getModifiePar() != null ? declaration.getModifiePar().getMatricule() : null,
        	    declaration.getModifieLe(),
        	    declaration.getSupprimePar() != null ? declaration.getSupprimePar().getNom() : null,
        	    declaration.getSupprimeLe()
        	);

    }


    // PostePolice -> PostePoliceDTO
    public static PostePoliceDTO toPostePoliceDTO(PostePolice postePolice) {
        return new PostePoliceDTO(
            postePolice.getId(),
            postePolice.getCodeUnique(),
            postePolice.getNom(),
            postePolice.getAdresse(),
            postePolice.getTelephone(),
            postePolice.isActif()
        );
    }

    public static DeclarantDTO toDeclarantDTO(Declarant declarant) {
        return new DeclarantDTO(
            declarant.getNom(),
            declarant.getPrenom(),
            declarant.getTelephone(),
            declarant.getNumNina(),
            declarant.getNumPassePort(),
            declarant.getNumCarteIdentite(),
            declarant.getEmail(),
            declarant.getAdresse(),
            declarant.getDateNaissance(),
            declarant.getLieuNaissance()
        );
    }

    public static RoleDTO toRoleDTO(Role role) {
        if (role == null) return null;
        return new RoleDTO(
            role.getId(),
            role.getLibelle()
        );
    }

    // Résumé Utilisateur (ManyToMany)
    public static UtilisateurResumeDTO toUtilisateurSummary(Utilisateur utilisateur) {
        List<String> rolesLibelles = utilisateur.getRoles().stream()
                .map(Role::getLibelle)
                .collect(Collectors.toList());

        return new UtilisateurResumeDTO(
                utilisateur.getId(),
                utilisateur.getNom(),
                utilisateur.getPrenom(),
                rolesLibelles 
        ); 
    }
}