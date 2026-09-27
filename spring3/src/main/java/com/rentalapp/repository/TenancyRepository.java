package com.rentalapp.repository;

import com.rentalapp.domain.Tenancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TenancyRepository extends JpaRepository<Tenancy, Long> {

    long countByTenantId(Long tenantId);

    long countByEndDateIsNull();

    long countByEndDateIsNotNull();

    List<Tenancy> findTop5ByOrderByStartDateDesc();

    @Query("""
        select tc from Tenancy tc
        where (:status = 'active' and tc.endDate is null)
           or (:status = 'ended' and tc.endDate is not null)
           or (:status = 'all')
        order by tc.startDate desc
        """)
    List<Tenancy> findByStatus(@Param("status") String status);

    List<Tenancy> findByRoomId(Long roomId);

    List<Tenancy> findByTenantId(Long tenantId);
}
