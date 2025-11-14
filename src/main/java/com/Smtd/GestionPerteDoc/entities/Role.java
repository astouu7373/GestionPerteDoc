package com.Smtd.GestionPerteDoc.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Getter
@Setter
@ToString(exclude = "utilisateurs")
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "libelle", nullable = false, unique = true, length = 100)
    private String libelle;

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Utilisateur> utilisateurs = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role)) return false;
        return id != null && id.equals(((Role) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}