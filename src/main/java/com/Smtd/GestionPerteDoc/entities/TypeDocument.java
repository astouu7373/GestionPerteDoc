package com.Smtd.GestionPerteDoc.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "types_document")
public class TypeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_type_document", unique = true, nullable = false)
    private String codeTypeDocument;

    @Column(name = "libelle_type_document", nullable = false)
    private String libelleTypeDocument;
}
