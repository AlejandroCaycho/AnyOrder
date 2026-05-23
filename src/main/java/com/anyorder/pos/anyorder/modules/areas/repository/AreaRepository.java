package com.anyorder.pos.anyorder.modules.areas.repository;

import com.anyorder.pos.anyorder.modules.areas.model.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AreaRepository extends JpaRepository<Area, Integer> {
    
    Optional<Area> findByNameArea(String nameArea);
    
    boolean existsByNameArea(String nameArea);
    
    List<Area> findByStateTrue();
    
    List<Area> findByNameAreaContainingIgnoreCase(String nameArea);
    
    long countByStateTrue();

    @Query("SELECT COUNT(u) FROM User u WHERE u.area.idArea = :idArea")
    long countUsersByArea(@Param("idArea") Integer idArea);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.area.idArea = :idArea")
    long countProductsByArea(@Param("idArea") Integer idArea);

    @Query("SELECT COUNT(t) FROM Tables t WHERE t.area.idArea = :idArea")
    long countTablesByArea(@Param("idArea") Integer idArea);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("UPDATE Tables t SET t.state = false WHERE t.area.idArea = :idArea")
    void deactivateTablesByArea(@Param("idArea") Integer idArea);
}
