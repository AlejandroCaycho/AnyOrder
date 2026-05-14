package com.anyorder.pos.anyorder.modules.presentations.repository;

import com.anyorder.pos.anyorder.modules.presentations.model.Presentation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PresentationRepository extends JpaRepository<Presentation, Integer> {

    List<Presentation> findByStateTrue();

    List<Presentation> findByIdProduct(Integer idProduct);

    List<Presentation> findByIdProductAndStateTrue(Integer idProduct);

    @Query("SELECT p.idPresentation FROM Presentation p WHERE p.idProduct = :idProduct")
    List<Integer> findIdsByProduct(@Param("idProduct") Integer idProduct);

    List<Presentation> findByNameContainingIgnoreCase(String name);
}
