package com.rentalapp.repository;

import com.rentalapp.domain.CurrentTenancy;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Read-only by convention: only findAll()/findById() are ever called from
 * CurrentTenancyController. save()/delete() exist on the interface (Spring
 * Data always generates them) but are never invoked - the underlying
 * @Immutable/@Subselect mapping would reject writes at the Hibernate level
 * anyway.
 */
public interface CurrentTenancyRepository extends JpaRepository<CurrentTenancy, Long> {
}
