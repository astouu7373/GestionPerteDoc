package com.Smtd.GestionPerteDoc.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Smtd.GestionPerteDoc.entities.PostePolice;

import java.util.Optional;

public interface PostePoliceRepository extends JpaRepository<PostePolice, Long> {
    Optional<PostePolice> findByCodeUnique(String codeUnique);

	boolean existsByNom(String nom);
}
