package com.rentalapp.domain;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Maps 1:1 onto the existing `tenants` table. No email/phone columns exist
 * on this table - three name parts and a national ID only.
 */
@Entity
@Table(name = "tenants")
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "First names are required")
    @Size(max = 45)
    @Column(name = "first_names", length = 45, nullable = false)
    private String firstNames;

    @NotBlank(message = "Paternal surname is required")
    @Size(max = 45)
    @Column(name = "paternal_surname", length = 45, nullable = false)
    private String paternalSurname;

    @NotBlank(message = "Maternal surname is required")
    @Size(max = 45)
    @Column(name = "maternal_surname", length = 45, nullable = false)
    private String maternalSurname;

    @NotBlank(message = "National ID is required")
    @Pattern(regexp = "^[A-Za-z0-9]{1,8}$", message = "National ID must be at most 8 alphanumeric characters")
    @Column(name = "national_id", length = 8, nullable = false, unique = true)
    private String nationalId;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public Tenant() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstNames() {
        return firstNames;
    }

    public void setFirstNames(String firstNames) {
        this.firstNames = firstNames;
    }

    public String getPaternalSurname() {
        return paternalSurname;
    }

    public void setPaternalSurname(String paternalSurname) {
        this.paternalSurname = paternalSurname;
    }

    public String getMaternalSurname() {
        return maternalSurname;
    }

    public void setMaternalSurname(String maternalSurname) {
        this.maternalSurname = maternalSurname;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getFullName() {
        return firstNames + " " + paternalSurname + " " + maternalSurname;
    }
}
