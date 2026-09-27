package com.rentalapp.repository;

import com.rentalapp.domain.RentPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface RentPaymentRepository extends JpaRepository<RentPayment, Long> {

    long countByTenantId(Long tenantId);

    List<RentPayment> findAllByOrderByDepositDateDesc();

    List<RentPayment> findTop5ByOrderByDepositDateDesc();

    long countByPeriodBetween(LocalDate start, LocalDate end);

    @Query("select coalesce(sum(p.amount), 0) from RentPayment p where p.period between :start and :end")
    BigDecimal sumAmountByPeriodBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
