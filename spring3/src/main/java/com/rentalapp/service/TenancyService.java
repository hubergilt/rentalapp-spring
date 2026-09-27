package com.rentalapp.service;

import com.rentalapp.domain.Tenancy;
import com.rentalapp.repository.SecurityDepositRepository;
import com.rentalapp.repository.TenancyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class TenancyService {

    private final TenancyRepository tenancyRepository;
    private final SecurityDepositRepository securityDepositRepository;

    public TenancyService(TenancyRepository tenancyRepository,
                           SecurityDepositRepository securityDepositRepository) {
        this.tenancyRepository = tenancyRepository;
        this.securityDepositRepository = securityDepositRepository;
    }

    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) { super(message); }
    }

    public static class DeleteBlockedException extends RuntimeException {
        public DeleteBlockedException(String message) { super(message); }
    }

    public List<Tenancy> findByStatus(String status) {
        return tenancyRepository.findByStatus(status == null || status.isBlank() ? "all" : status);
    }

    public Optional<Tenancy> findById(Long id) {
        return tenancyRepository.findById(id);
    }

    /**
     * Re-checks, in code, the same three CHECK constraints the DB enforces
     * (chk_tenancies_dates, chk_tenancies_deposit_refund_amount,
     * chk_tenancies_deposit_refund_date), so the user gets a friendly
     * message on the form instead of a raw SQL constraint-violation error.
     */
    @Transactional
    public Tenancy save(Tenancy tenancy) {
        if (tenancy.getEndDate() != null && tenancy.getEndDate().isBefore(tenancy.getStartDate())) {
            throw new ValidationException("End date cannot be before the start date.");
        }
        if (tenancy.getDepositRefundAmount() != null
                && tenancy.getDepositRefundAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Deposit refund amount cannot be negative.");
        }
        if (tenancy.getDepositRefundDate() != null
                && tenancy.getDepositRefundDate().isBefore(tenancy.getStartDate())) {
            throw new ValidationException("Deposit refund date cannot be before the start date.");
        }
        return tenancyRepository.save(tenancy);
    }

    /**
     * Mirrors fk_security_deposits_tenancy_id's ON DELETE RESTRICT: a
     * tenancy with installments on file cannot be deleted.
     */
    @Transactional
    public void delete(Long id) {
        long depositCount = securityDepositRepository.countByTenancyId(id);
        if (depositCount > 0) {
            throw new DeleteBlockedException(
                "This tenancy still has " + depositCount + " security deposit installment(s) on file and cannot be deleted.");
        }
        tenancyRepository.deleteById(id);
    }
}
