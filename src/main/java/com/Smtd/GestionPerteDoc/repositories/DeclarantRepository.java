package com.Smtd.GestionPerteDoc.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Smtd.GestionPerteDoc.entities.Declarant;

import java.util.Optional;
import java.util.UUID;

public interface DeclarantRepository extends JpaRepository<Declarant, UUID> {
    Optional<Declarant> findByNumPassePort(String numPassePort);
    Optional<Declarant> findByNumNina(String numNina);
    Optional<Declarant> findByNumCarteIdentite(String numCarteIdentite);
	Optional<Declarant> findByEmail(String email);
	Optional<Declarant> findByTelephone(String telephone);
}
