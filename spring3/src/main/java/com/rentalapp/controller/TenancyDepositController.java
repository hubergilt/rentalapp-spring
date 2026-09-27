package com.rentalapp.controller;

import com.rentalapp.domain.SecurityDeposit;
import com.rentalapp.domain.Tenancy;
import com.rentalapp.repository.SecurityDepositRepository;
import com.rentalapp.repository.TenancyRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Backs the inline, AJAX-refreshed deposit installments panel on a
 * tenancy's show page: add or remove an installment without leaving the
 * page or doing a full reload. Every endpoint re-renders just the
 * `fragments/deposit-panel :: panel` fragment, which the page swaps in via
 * a small fetch() call (see static/js/deposits.js).
 */
@Controller
@RequestMapping("/tenancies/{tenancyId}/deposits")
public class TenancyDepositController {

    private final TenancyRepository tenancyRepository;
    private final SecurityDepositRepository securityDepositRepository;

    public TenancyDepositController(TenancyRepository tenancyRepository,
                                     SecurityDepositRepository securityDepositRepository) {
        this.tenancyRepository = tenancyRepository;
        this.securityDepositRepository = securityDepositRepository;
    }

    @GetMapping
    public String panel(@PathVariable Long tenancyId, Model model) {
        loadPanel(tenancyId, model);
        return "fragments/deposit-panel :: panel";
    }

    @PostMapping
    public String add(@PathVariable Long tenancyId,
                       @RequestParam BigDecimal amount,
                       @RequestParam(required = false) LocalDate paidDate,
                       @RequestParam(required = false) String remarks,
                       Model model) {
        Tenancy tenancy = tenancyRepository.findById(tenancyId)
                .orElseThrow(() -> new IllegalArgumentException("Tenancy not found: " + tenancyId));
        SecurityDeposit deposit = new SecurityDeposit();
        deposit.setTenancy(tenancy);
        deposit.setAmount(amount);
        deposit.setPaidDate(paidDate != null ? paidDate : LocalDate.now());
        deposit.setRemarks(remarks);
        securityDepositRepository.save(deposit);
        loadPanel(tenancyId, model);
        return "fragments/deposit-panel :: panel";
    }

    @PostMapping("/{depositId}/delete")
    public String remove(@PathVariable Long tenancyId, @PathVariable Long depositId, Model model) {
        securityDepositRepository.deleteById(depositId);
        loadPanel(tenancyId, model);
        return "fragments/deposit-panel :: panel";
    }

    private void loadPanel(Long tenancyId, Model model) {
        Tenancy tenancy = tenancyRepository.findById(tenancyId)
                .orElseThrow(() -> new IllegalArgumentException("Tenancy not found: " + tenancyId));
        model.addAttribute("tenancy", tenancy);
        model.addAttribute("deposits", securityDepositRepository.findByTenancyIdOrderByPaidDateAsc(tenancyId));
        model.addAttribute("newDeposit", new SecurityDeposit());
    }
}
