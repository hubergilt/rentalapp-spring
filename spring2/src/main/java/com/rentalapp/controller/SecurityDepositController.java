package com.rentalapp.controller;

import com.rentalapp.domain.SecurityDeposit;
import com.rentalapp.repository.SecurityDepositRepository;
import com.rentalapp.repository.TenancyRepository;
import javax.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/security-deposits")
public class SecurityDepositController {

    private final SecurityDepositRepository securityDepositRepository;
    private final TenancyRepository tenancyRepository;

    public SecurityDepositController(SecurityDepositRepository securityDepositRepository,
                                      TenancyRepository tenancyRepository) {
        this.securityDepositRepository = securityDepositRepository;
        this.tenancyRepository = tenancyRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("deposits", securityDepositRepository.findAll());
        return "securitydeposit/list";
    }

    @GetMapping("/{id}")
    public String show(@PathVariable Long id, Model model) {
        model.addAttribute("deposit", securityDepositRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Security deposit not found: " + id)));
        return "securitydeposit/show";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("deposit", new SecurityDeposit());
        model.addAttribute("tenancies", tenancyRepository.findAll());
        return "securitydeposit/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("deposit", securityDepositRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Security deposit not found: " + id)));
        model.addAttribute("tenancies", tenancyRepository.findAll());
        return "securitydeposit/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("deposit") SecurityDeposit deposit, BindingResult result,
                          Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("tenancies", tenancyRepository.findAll());
            return "securitydeposit/form";
        }
        SecurityDeposit saved = securityDepositRepository.save(deposit);
        redirectAttributes.addFlashAttribute("success", "Security deposit recorded.");
        return "redirect:/security-deposits/" + saved.getId();
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("deposit") SecurityDeposit deposit,
                          BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("tenancies", tenancyRepository.findAll());
            return "securitydeposit/form";
        }
        deposit.setId(id);
        securityDepositRepository.save(deposit);
        redirectAttributes.addFlashAttribute("success", "Security deposit updated.");
        return "redirect:/security-deposits/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        securityDepositRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Security deposit deleted.");
        return "redirect:/security-deposits";
    }
}
