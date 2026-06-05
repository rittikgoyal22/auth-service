package com.etd.auth_service.dao;

import com.etd.auth_service.entity.Employee;
import com.etd.auth_service.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepo extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    // Direct DELETE to execute immediately — avoids Hibernate batching the delete
    // behind the next INSERT, which would cause a unique constraint violation on employee_id.
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM RefreshToken rt WHERE rt.employee = :employee")
    void deleteByEmployee(@Param("employee") Employee employee);

}
