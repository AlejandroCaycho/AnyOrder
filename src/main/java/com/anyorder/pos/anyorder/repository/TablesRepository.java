package com.anyorder.pos.anyorder.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.anyorder.pos.anyorder.model.Tables;

import java.util.List;
import java.util.Optional;

@Repository
public interface TablesRepository extends JpaRepository<Tables, Integer> {

    Optional<Tables> findByName(String name);

    boolean existsByName(String name);

    Optional<Tables> findByNameIgnoreCase(String name);

    Optional<Tables> findByQrToken(String qrToken);

    boolean existsByQrToken(String qrToken);

    List<Tables> findByStateTrue();

    List<Tables> findByStateFalse();

    List<Tables> findByState(Boolean state);

    List<Tables> findByArea_IdArea(Integer areaId);

    List<Tables> findByIsOccupiedTrue();

    List<Tables> findByIsOccupiedFalse();

    List<Tables> findByIsOccupied(Boolean isOccupied);

    List<Tables> findByNameContainingIgnoreCase(String name);

    List<Tables> findByLocationContainingIgnoreCase(String location);

    @Query("SELECT t FROM Tables t WHERE t.area.idArea = :areaId AND t.state = true ORDER BY t.name ASC")
    List<Tables> findActiveTablesByArea(@Param("areaId") Integer areaId);

    @Query("SELECT t FROM Tables t WHERE t.currentOccupancy < t.capacity AND t.state = true ORDER BY t.name ASC")
    List<Tables> findAvailableTables();

    @Query("SELECT t FROM Tables t WHERE t.currentOccupancy < t.capacity AND t.area.idArea = :areaId AND t.state = true ORDER BY t.name ASC")
    List<Tables> findAvailableTablesByArea(@Param("areaId") Integer areaId);

    @Query("SELECT t FROM Tables t WHERE t.isOccupied = true AND t.state = true ORDER BY t.occupiedSince ASC")
    List<Tables> findOccupiedTablesOrderByTime();

    @Query("SELECT COUNT(t) FROM Tables t WHERE t.area.idArea = :areaId AND t.state = true")
    long countActiveTablesByArea(@Param("areaId") Integer areaId);

    @Query("SELECT COUNT(t) FROM Tables t WHERE t.isOccupied = true AND t.state = true")
    long countOccupiedTables();

    @Query("SELECT COUNT(t) FROM Tables t WHERE t.currentOccupancy < t.capacity AND t.state = true")
    long countAvailableTables();

    long countByStateTrue();

    long countByArea_IdArea(Integer areaId);
}