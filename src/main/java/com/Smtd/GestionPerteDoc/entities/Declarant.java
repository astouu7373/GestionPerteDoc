package com.Smtd.GestionPerteDoc.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.Smtd.GestionPerteDoc.entities.Declaration;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Data
@Table(name = "declarants")
public class Declarant {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "num_nina", unique = true, nullable=true)
    private String numNina;

    @Column(name = "num_carte_identite", unique = true, nullable=true)
    private String numCarteIdentite;

    @Column(name = "num_passeport", unique = true, nullable=true)
    private String numPassePort;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Temporal(TemporalType.DATE)
    @Column(name = "date_naissance")
    private Date dateNaissance;
    
    @Column(name = "lieu_naissance")
    private String lieuNaissance;

    @Column(name = "email", length = 150, unique=true, nullable=true)
    private String email;

    @Column(name = "telephone", nullable = false, length = 20, unique=true)
    private String telephone;

    @Column(name = "adresse", nullable = false, length = 255)
    private String adresse;  

    @OneToMany(mappedBy = "declarant")
    @JsonIgnore
    private List<Declaration> declarations;

    
}
