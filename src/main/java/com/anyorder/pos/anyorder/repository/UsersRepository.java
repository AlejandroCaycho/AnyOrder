package com.anyorder.pos.anyorder.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.anyorder.pos.anyorder.model.Users;

@Repository
public interface UsersRepository extends JpaRepository<Users, Integer> {
    
    Optional<Users> findByEmail(String email);
    
    Optional<Users> findByDocumentNumber(String documentNumber);
    
    boolean existsByEmail(String email);
    
    boolean existsByDocumentNumber(String documentNumber);
    
    List<Users> findByState(Boolean state);
    
    List<Users> findByRole_IdRole(Integer idRole);
    
    List<Users> findByArea_IdArea(Integer idArea);
    
    @Query("SELECT u FROM Users u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.surnames) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Users> searchByNameOrSurnames(@Param("search") String search);
    
    @Query("SELECT u FROM Users u WHERE u.email = :email AND u.idUser <> :userId")
    Optional<Users> findByEmailAndNotUserId(@Param("email") String email, @Param("userId") Integer userId);
    
    @Query("SELECT u FROM Users u WHERE u.documentNumber = :documentNumber AND u.idUser <> :userId")
    Optional<Users> findByDocumentNumberAndNotUserId(@Param("documentNumber") String documentNumber, @Param("userId") Integer userId);
}
