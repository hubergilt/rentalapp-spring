package com.rentalapp.repository;

import com.rentalapp.domain.SecurityDeposit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SecurityDepositRepository extends JpaRepository<SecurityDeposit, Long> {

    List<SecurityDeposit> findByTenancyIdOrderByPaidDateAsc(Long tenancyId);

    long countByTenancyId(Long tenancyId);
}
