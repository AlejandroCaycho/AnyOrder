package com.anyorder.pos.anyorder.modules.tables.repository;

import com.anyorder.pos.anyorder.modules.tables.model.Tables;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TablesRepository extends JpaRepository<Tables, Integer> {
    Optional<Tables> findByNameTable(String nameTable);
    boolean existsByNameTable(String nameTable);
    List<Tables> findByStateTrue();
    List<Tables> findByArea_IdArea(Integer idArea);
    Optional<Tables> findByQrCode(String qrCode);
    Optional<Tables> findByQrToken(String qrToken);
    List<Tables> findByIsOccupied(Boolean isOccupied);
}
