package com.anyorder.pos.anyorder.modules.users.repository;

import com.anyorder.pos.anyorder.modules.users.model.UserLog;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserLogRepository extends JpaRepository<UserLog, Integer> {
    @EntityGraph(attributePaths = {"user"})
    List<UserLog> findAll();

    @EntityGraph(attributePaths = {"user"})
    List<UserLog> findByUser_IdUser(Integer idUser);
    
    @EntityGraph(attributePaths = {"user"})
    List<UserLog> findByState(Integer state);
    
    @EntityGraph(attributePaths = {"user"})
    Optional<UserLog> findByUser_IdUserAndState(Integer idUser, Integer state);
}
