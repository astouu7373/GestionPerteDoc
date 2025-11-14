package com.Smtd.GestionPerteDoc.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Data
@Table(name = "postes_police")
public class PostePolice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_unique", nullable = false, unique = true, length = 50)
    private String codeUnique;

    @Column(name = "nom", nullable = false, length = 150)
    private String nom;

    @Column(name = "adresse", nullable = false, length = 255)
    private String adresse;

    @Column(name = "telephone", length = 20)
    private String telephone;

    @Column(name = "actif", nullable = false)
    private boolean actif= true;

    @OneToMany(mappedBy = "postePolice")
    @JsonIgnore
    private List<Utilisateur> utilisateurs;	
}
