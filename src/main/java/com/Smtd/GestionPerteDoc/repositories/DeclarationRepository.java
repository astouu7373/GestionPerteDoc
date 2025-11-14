package com.Smtd.GestionPerteDoc.repositories;

import com.Smtd.GestionPerteDoc.entities.Declaration;
import com.Smtd.GestionPerteDoc.enums.StatutDeclaration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeclarationRepository extends JpaRepository<Declaration, Long> {

    // === RECHERCHE PAR RÉFÉRENCE ===
    @Query("""
        SELECT d FROM Declaration d
        LEFT JOIN FETCH d.declarant
        LEFT JOIN FETCH d.typeDocument
        LEFT JOIN FETCH d.utilisateur
        LEFT JOIN FETCH d.creePar
        LEFT JOIN FETCH d.modifiePar
        LEFT JOIN FETCH d.supprimePar
        WHERE d.numeroReference = :numeroReference AND d.supprime = false
    """)
    Optional<Declaration> findByNumeroReferenceWithDetails(@Param("numeroReference") String numeroReference);

    // === RECHERCHE PAR ID AVEC TOUS LES DÉTAILS ===
    @Query("""
        SELECT d FROM Declaration d
        LEFT JOIN FETCH d.declarant
        LEFT JOIN FETCH d.typeDocument
        LEFT JOIN FETCH d.utilisateur
        LEFT JOIN FETCH d.creePar
        LEFT JOIN FETCH d.modifiePar
        LEFT JOIN FETCH d.supprimePar
        WHERE d.id = :id
    """)
    Optional<Declaration> findByIdWithDetails(@Param("id") Long id);

    // === LISTAGE AVEC TOUS LES DÉTAILS ===
    @Query("""
        SELECT d FROM Declaration d
        LEFT JOIN FETCH d.declarant
        LEFT JOIN FETCH d.typeDocument
        LEFT JOIN FETCH d.utilisateur
        LEFT JOIN FETCH d.creePar
        LEFT JOIN FETCH d.modifiePar
        LEFT JOIN FETCH d.supprimePar
        WHERE d.supprime = false
    """)
    List<Declaration> findAllWithDetails();

    // === DÉCLARATIONS ACTIVES - AGENT (ses propres déclarations) ===
    @Query("""
        SELECT d FROM Declaration d
        LEFT JOIN FETCH d.declarant
        LEFT JOIN FETCH d.typeDocument
        LEFT JOIN FETCH d.utilisateur
        LEFT JOIN FETCH d.creePar
        LEFT JOIN FETCH d.modifiePar
        WHERE d.utilisateur.id = :utilisateurId AND d.supprime = false
    """)
    List<Declaration> findByUtilisateurIdAndSupprimeFalseWithDetails(@Param("utilisateurId") Long utilisateurId);

    // === DÉCLARATIONS ACTIVES - ADMIN/SUPERVISEUR (toutes les déclarations de son poste) ===
    @Query("""
        SELECT d FROM Declaration d
        LEFT JOIN FETCH d.declarant
        LEFT JOIN FETCH d.typeDocument
        LEFT JOIN FETCH d.utilisateur
        LEFT JOIN FETCH d.creePar
        LEFT JOIN FETCH d.modifiePar
        LEFT JOIN FETCH d.supprimePar
        WHERE d.utilisateur.postePolice.id = :postePoliceId AND d.supprime = false
    """)
    List<Declaration> findByUtilisateurPostePoliceIdAndSupprimeFalseWithDetails(@Param("postePoliceId") Long postePoliceId);

    // === DÉCLARATIONS SUPPRIMÉES - AGENT ===
    @Query("""
        SELECT d FROM Declaration d
        LEFT JOIN FETCH d.declarant
        LEFT JOIN FETCH d.typeDocument
        LEFT JOIN FETCH d.utilisateur
        LEFT JOIN FETCH d.creePar
        LEFT JOIN FETCH d.supprimePar
        WHERE d.utilisateur.id = :utilisateurId AND d.supprime = true
    """)
    List<Declaration> findByUtilisateurIdAndSupprimeTrueWithDetails(@Param("utilisateurId") Long utilisateurId);

    // === DÉCLARATIONS SUPPRIMÉES - ADMIN/SUPERVISEUR ===
    @Query("""
        SELECT d FROM Declaration d
        LEFT JOIN FETCH d.declarant
        LEFT JOIN FETCH d.typeDocument
        LEFT JOIN FETCH d.utilisateur
        LEFT JOIN FETCH d.creePar
        LEFT JOIN FETCH d.supprimePar
        WHERE d.utilisateur.postePolice.id = :postePoliceId AND d.supprime = true
    """)
    List<Declaration> findByUtilisateurPostePoliceIdAndSupprimeTrueWithDetails(@Param("postePoliceId") Long postePoliceId);

//    // === MÉTHODES EXISTANTES POUR COMPATIBILITÉ ===
//    @Query("""
//        SELECT d FROM Declaration d
//        LEFT JOIN FETCH d.declarant
//        LEFT JOIN FETCH d.typeDocument
//        WHERE d.creePar.id = :utilisateurId AND d.supprime = false
//    """)
//    List<Declaration> findByCreeParIdAndSupprimeFalse(@Param("utilisateurId") Long utilisateurId);

//    @Query("""
//        SELECT d FROM Declaration d
//        LEFT JOIN FETCH d.declarant
//        LEFT JOIN FETCH d.typeDocument
//        WHERE d.creePar.postePolice.id = :postePoliceId AND d.supprime = false
//    """)
//    List<Declaration> findByCreeParPostePoliceIdAndSupprimeFalse(@Param("postePoliceId") Long postePoliceId);

    @Query("""
        SELECT d FROM Declaration d
        LEFT JOIN FETCH d.declarant
        LEFT JOIN FETCH d.typeDocument
        WHERE d.creePar.id = :utilisateurId AND d.supprime = true
    """)
    List<Declaration> findByUtilisateurIdAndSupprimeTrue(@Param("utilisateurId") Long utilisateurId);

    @Query("""
        SELECT d FROM Declaration d
        LEFT JOIN FETCH d.declarant
        LEFT JOIN FETCH d.typeDocument
        WHERE d.creePar.postePolice.id = :postePoliceId AND d.supprime = true
    """)
    List<Declaration> findByUtilisateur_PostePolice_IdAndSupprimeTrue(@Param("postePoliceId") Long postePoliceId);

    // === STATISTIQUES ===
    long countBySupprimeFalse();
    long countBySupprimeTrue();
    
    long countByStatutAndSupprimeFalse(StatutDeclaration statut);
    long countByStatutAndSupprimeTrue(StatutDeclaration statut);

    // === VÉRIFICATION EXISTENCE ===
    boolean existsByNumeroReference(String numeroReference);

    // === RECHERCHE PAR DÉCLARANT ===
    @Query("""
        SELECT d FROM Declaration d
        LEFT JOIN FETCH d.declarant
        LEFT JOIN FETCH d.typeDocument
        WHERE d.declarant.id = :declarantId AND d.supprime = false
    """)
    List<Declaration> findByDeclarantId(@Param("declarantId") String declarantId);

    // === RECHERCHE PAR TYPE DE DOCUMENT ===
    @Query("""
        SELECT d FROM Declaration d
        LEFT JOIN FETCH d.declarant
        LEFT JOIN FETCH d.typeDocument
        WHERE d.typeDocument.id = :typeDocumentId AND d.supprime = false
    """)
    List<Declaration> findByTypeDocumentId(@Param("typeDocumentId") Long typeDocumentId);

    // === RECHERCHE PAR STATUT ===
    @Query("""
        SELECT d FROM Declaration d
        LEFT JOIN FETCH d.declarant
        LEFT JOIN FETCH d.typeDocument
        WHERE d.statut = :statut AND d.supprime = false
    """)
    List<Declaration> findByStatut(@Param("statut") StatutDeclaration statut);

    // === HISTORIQUE MODIFICATIONS ===
//    @Query("""
//        SELECT d FROM Declaration d
//        LEFT JOIN FETCH d.declarant
//        LEFT JOIN FETCH d.typeDocument
//        LEFT JOIN FETCH d.modifiePar
//        WHERE d.modifiePar IS NOT NULL
//        ORDER BY d.modifieLe DESC
//    """)
//    List<Declaration> findDeclarationsModifiees();

    // === DERNIÈRES DÉCLARATIONS CRÉÉES ===
//    @Query("""
//        SELECT d FROM Declaration d
//        LEFT JOIN FETCH d.declarant
//        LEFT JOIN FETCH d.typeDocument
//        LEFT JOIN FETCH d.creePar
//        WHERE d.supprime = false
//        ORDER BY d.creeLe DESC
//    """)
//    List<Declaration> findTop10ByOrderByCreeLeDesc();

    // === POUR GÉNÉRATION DE RÉFÉRENCE ===
    Declaration findTopByOrderByNumeroReferenceDesc();

    // === MÉTHODE POUR VÉRIFIER LA PERSISTANCE ===
    @Query("SELECT COUNT(d) FROM Declaration d")
    long countAllDeclarations();

//    // === MÉTHODE POUR DEBUGGER LES STATISTIQUES ===
//    @Query("""
//        SELECT d.statut, COUNT(d)
//        FROM Declaration d
//        WHERE d.supprime = false
//        GROUP BY d.statut
//    """)
//    List<Object[]> countByStatutGrouped();

    // === AUTRES MÉTHODES AJOUTÉES ===
    @Query("""
        SELECT d FROM Declaration d
        LEFT JOIN FETCH d.utilisateur
        LEFT JOIN FETCH d.typeDocument
        LEFT JOIN FETCH d.declarant
        WHERE d.utilisateur.id = :utilisateurId
    """)
    List<Declaration> findByUtilisateurIdWithDetails(@Param("utilisateurId") Long utilisateurId);

    List<Declaration> findBySupprimeTrue();

    long countByUtilisateurIdAndSupprimeFalse(Long utilisateurId);

    long countByUtilisateurIdAndStatutAndSupprimeFalse(Long utilisateurId, StatutDeclaration validee);

    long countByUtilisateurIdAndSupprimeTrue(Long utilisateurId);
    @Query("SELECT d FROM Declaration d LEFT JOIN FETCH d.modifiePar WHERE d.id = :id")
    Optional<Declaration> findByIdWithModifiePar(@Param("id") Long id);

	List<Declaration> findByUtilisateurId(Long utilisateurId);
}
