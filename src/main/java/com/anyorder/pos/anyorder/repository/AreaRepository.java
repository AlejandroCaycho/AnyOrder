package com.anyorder.pos.anyorder.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.anyorder.pos.anyorder.model.Area;

import java.util.List;
import java.util.Optional;

@Repository
public interface AreaRepository extends JpaRepository<Area, Integer> {

    Optional<Area> findByNameArea(String nameArea);

    boolean existsByNameArea(String nameArea);

    Optional<Area> findByNameAreaIgnoreCase(String nameArea);

    List<Area> findByStateTrue();

    List<Area> findByStateFalse();

    List<Area> findByState(Boolean state);

    List<Area> findByNameAreaContainingIgnoreCase(String nameArea);

    long countByStateTrue();
}