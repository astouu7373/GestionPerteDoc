package com.Smtd.GestionPerteDoc.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.Smtd.GestionPerteDoc.entities.TypeDocument;

import java.util.List;
import java.util.Optional;

@Repository
public interface TypeDocumentRepository extends JpaRepository<TypeDocument, Long> {

    Optional<TypeDocument> findByCodeTypeDocument(String codeTypeDocument);
    
    Optional<TypeDocument> findByLibelleTypeDocument(String libelleTypeDocument);
    
    @Query("SELECT t FROM TypeDocument t WHERE LOWER(t.libelleTypeDocument) = LOWER(:libelle)")
    Optional<TypeDocument> findByLibelleTypeDocumentIgnoreCase(@Param("libelle") String libelle);
    
    boolean existsByLibelleTypeDocument(String libelleTypeDocument);
    
    boolean existsByCodeTypeDocument(String codeTypeDocument);
    
    @Query("SELECT t FROM TypeDocument t WHERE t.libelleTypeDocument IS NOT NULL")
    List<TypeDocument> findAllActifs();
    
    @Query("SELECT t FROM TypeDocument t WHERE LOWER(t.libelleTypeDocument) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<TypeDocument> findByKeywordInLibelle(@Param("keyword") String keyword);
   

}