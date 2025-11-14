package com.Smtd.GestionPerteDoc.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;
import com.Smtd.GestionPerteDoc.enums.StatutDeclaration;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Data
@Table(name = "declarations")
public class Declaration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_reference", nullable = false, unique = true, length = 50)
    private String numeroReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_document_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private TypeDocument typeDocument;

    @Column(name = "numero_document", length = 100)
    private String numeroDocument;

    @Temporal(TemporalType.DATE)
    @Column(name = "date_perte")
    private Date datePerte;

    @Column(name = "lieu_perte", length = 255)
    private String lieuPerte;

    @Column(name = "circonstances", length = 500)
    private String circonstances;

    @Temporal(TemporalType.DATE)
    @Column(name = "date_declaration", nullable = false)
    private Date dateDeclaration;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutDeclaration statut = StatutDeclaration.ENREGISTREE;

    @Column(name = "supprime", nullable = false)
    private boolean supprime = false;

    // RELATIONS
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "declarant_id", nullable = false)
    private Declarant declarant;

    // TRACABILITÉ COMPLÈTE
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cree_par_id", nullable = false)
    private Utilisateur creePar;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = true)
    private Utilisateur utilisateur;


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "cree_le", nullable = false)
    private Date creeLe;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "modifie_par_id")
    private Utilisateur modifiePar;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modifie_le")
    private Date modifieLe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supprime_par_id")
    private Utilisateur supprimePar;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "supprime_le")
    private Date supprimeLe;

    public Declaration() {
        this.statut = StatutDeclaration.ENREGISTREE;
        this.supprime = false;
        this.creeLe = new Date();
    }
}