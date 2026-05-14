package com.anyorder.pos.anyorder.modules.users.repository;

import com.anyorder.pos.anyorder.modules.users.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByDocumentNumber(String documentNumber);
    
    List<User> findByStateTrue();
    
    boolean existsByEmail(String email);
    
    boolean existsByDocumentNumber(String documentNumber);
    
    List<User> findByRole_IdRole(Integer idRole);
    
    List<User> findByArea_IdArea(Integer idArea);

    @Query("SELECT u FROM User u WHERE u.registrationDate BETWEEN :start AND :end")
    List<User> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
