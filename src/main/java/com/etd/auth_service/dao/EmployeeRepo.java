package com.etd.auth_service.dao;

import com.etd.auth_service.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepo extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmailAddress(String emailAddress);

}
