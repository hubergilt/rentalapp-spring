package com.rentalapp.repository;

import com.rentalapp.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TenantRepository extends JpaRepository<Tenant, Long> {

    Tenant findByNationalId(String nationalId);

    @Query("""
        select t from Tenant t
        where lower(t.firstNames) like lower(concat('%', :q, '%'))
           or lower(t.paternalSurname) like lower(concat('%', :q, '%'))
           or lower(t.maternalSurname) like lower(concat('%', :q, '%'))
           or lower(t.nationalId) like lower(concat('%', :q, '%'))
        order by t.paternalSurname, t.maternalSurname, t.firstNames
        """)
    List<Tenant> search(@Param("q") String query);
}
