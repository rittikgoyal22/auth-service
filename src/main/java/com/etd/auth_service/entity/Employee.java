package com.etd.auth_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Read-only view of the employees table — auth-service never inserts employees.
// account-management owns the full entity and schema.
@Entity
@Table(name = "employees")
@Getter
@NoArgsConstructor
public class Employee {

    @Id
    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "email_address")
    private String emailAddress;

    @Column(name = "password")
    private String password;

    @Column(name = "role")
    private String role;

    @Column(name = "access_granted")
    private Boolean accessGranted;

}
