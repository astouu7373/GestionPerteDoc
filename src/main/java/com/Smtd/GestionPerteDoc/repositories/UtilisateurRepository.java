package com.Smtd.GestionPerteDoc.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.Smtd.GestionPerteDoc.entities.Role;
import com.Smtd.GestionPerteDoc.entities.TypeDocument;
import com.Smtd.GestionPerteDoc.entities.Utilisateur;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    boolean existsByEmail(String email);
    boolean existsByMatricule(String matricule);
    Optional<Utilisateur> findByEmail(String email);
    List<Utilisateur> findByActifTrue();
    List<Utilisateur> findByActifFalse();
    long countByMatriculeStartingWith(String prefix);
    
    @Query("SELECT u FROM Utilisateur u JOIN u.roles r WHERE r.libelle = :roleLibelle")
    List<Utilisateur> findByRolesLibelle(@Param("roleLibelle") String roleLibelle);
    
    @Query("SELECT u FROM Utilisateur u JOIN u.roles r WHERE u.postePolice.id = :postePoliceId AND r.libelle = :roleLibelle")
    List<Utilisateur> findByPostePoliceIdAndRolesLibelle(@Param("postePoliceId") Long postePoliceId, 
                                                        @Param("roleLibelle") String roleLibelle);
    
    @Query("SELECT COUNT(u) > 0 FROM Utilisateur u JOIN u.roles r WHERE u.postePolice.id = :postePoliceId AND r.libelle = 'SUPERVISEUR'")
    boolean existsByPostePoliceIdAndRolesLibelle(@Param("postePoliceId") Long postePoliceId);
    
    @Query("SELECT COUNT(u) FROM Utilisateur u JOIN u.roles r WHERE r.libelle = :roleLibelle AND u.actif = true")
    long countByRolesLibelleAndActifTrue(@Param("roleLibelle") String roleLibelle);

    long countByRolesLibelle(String libelle);
    Optional<Utilisateur> findByResetToken(String resetToken);
	List<Utilisateur> findByActifTrueAndIsDeletedFalse();
	List<Utilisateur> findByIsDeletedTrue();
	List<Utilisateur> findByActifFalseAndIsDeletedFalse();
	Optional<TypeDocument> findByMatricule(String username);
   

}
