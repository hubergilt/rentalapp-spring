package com.rentalapp.service;

import com.rentalapp.domain.Tenant;
import com.rentalapp.repository.RentPaymentRepository;
import com.rentalapp.repository.TenancyRepository;
import com.rentalapp.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class TenantService {

    private final TenantRepository tenantRepository;
    private final TenancyRepository tenancyRepository;
    private final RentPaymentRepository rentPaymentRepository;

    public TenantService(TenantRepository tenantRepository,
                          TenancyRepository tenancyRepository,
                          RentPaymentRepository rentPaymentRepository) {
        this.tenantRepository = tenantRepository;
        this.tenancyRepository = tenancyRepository;
        this.rentPaymentRepository = rentPaymentRepository;
    }

    public List<Tenant> findAll() {
        return tenantRepository.findAll();
    }

    public List<Tenant> search(String query) {
        if (query == null || query.isBlank()) {
            return tenantRepository.findAll();
        }
        return tenantRepository.search(query.trim());
    }

    public Optional<Tenant> findById(Long id) {
        return tenantRepository.findById(id);
    }

    @Transactional
    public Tenant save(Tenant tenant) {
        return tenantRepository.save(tenant);
    }

    /**
     * Mirrors the DB's ON DELETE RESTRICT on rent_payments.tenant_id and
     * tenancies.tenant_id: refuses to delete a tenant who still has
     * tenancies or rent payments on file, with a friendly message instead
     * of surfacing the raw FK violation.
     */
    public static class DeleteBlockedException extends RuntimeException {
        public DeleteBlockedException(String message) {
            super(message);
        }
    }

    @Transactional
    public void delete(Long id) {
        long tenancyCount = tenancyRepository.countByTenantId(id);
        if (tenancyCount > 0) {
            throw new DeleteBlockedException(
                "This tenant still has " + tenancyCount + " tenancy record(s) on file and cannot be deleted.");
        }
        long paymentCount = rentPaymentRepository.countByTenantId(id);
        if (paymentCount > 0) {
            throw new DeleteBlockedException(
                "This tenant still has " + paymentCount + " rent payment record(s) on file and cannot be deleted.");
        }
        tenantRepository.deleteById(id);
    }
}
