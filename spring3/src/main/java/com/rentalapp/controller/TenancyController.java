package com.rentalapp.controller;

import com.rentalapp.domain.Tenancy;
import com.rentalapp.repository.RoomRepository;
import com.rentalapp.repository.SecurityDepositRepository;
import com.rentalapp.repository.TenancyRepository;
import com.rentalapp.repository.TenantRepository;
import com.rentalapp.service.TenancyService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/tenancies")
public class TenancyController {

    private final TenancyService tenancyService;
    private final TenancyRepository tenancyRepository;
    private final TenantRepository tenantRepository;
    private final RoomRepository roomRepository;
    private final SecurityDepositRepository securityDepositRepository;

    public TenancyController(TenancyService tenancyService,
                              TenancyRepository tenancyRepository,
                              TenantRepository tenantRepository,
                              RoomRepository roomRepository,
                              SecurityDepositRepository securityDepositRepository) {
        this.tenancyService = tenancyService;
        this.tenancyRepository = tenancyRepository;
        this.tenantRepository = tenantRepository;
        this.roomRepository = roomRepository;
        this.securityDepositRepository = securityDepositRepository;
    }

    @GetMapping
    public String list(@RequestParam(name = "status", required = false, defaultValue = "all") String status,
                        @RequestParam(name = "roomId", required = false) Long roomId,
                        @RequestParam(name = "tenantId", required = false) Long tenantId,
                        Model model) {
        List<Tenancy> tenancies = tenancyService.findByStatus(status);
        if (roomId != null) {
            tenancies = tenancies.stream().filter(t -> t.getRoom().getId().equals(roomId)).toList();
        }
        if (tenantId != null) {
            tenancies = tenancies.stream().filter(t -> t.getTenant().getId().equals(tenantId)).toList();
        }
        model.addAttribute("tenancies", tenancies);
        model.addAttribute("status", status);
        model.addAttribute("roomId", roomId);
        model.addAttribute("tenantId", tenantId);
        model.addAttribute("rooms", roomRepository.findAll());
        model.addAttribute("tenants", tenantRepository.findAll());
        return "tenancy/list";
    }

    @GetMapping("/{id}")
    public String show(@PathVariable Long id, Model model) {
        Tenancy tenancy = tenancyService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenancy not found: " + id));
        model.addAttribute("tenancy", tenancy);
        model.addAttribute("deposits", securityDepositRepository.findByTenancyIdOrderByPaidDateAsc(id));
        return "tenancy/show";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("tenancy", new Tenancy());
        model.addAttribute("rooms", roomRepository.findAll());
        model.addAttribute("tenants", tenantRepository.findAll());
        return "tenancy/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Tenancy tenancy = tenancyService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenancy not found: " + id));
        model.addAttribute("tenancy", tenancy);
        model.addAttribute("rooms", roomRepository.findAll());
        model.addAttribute("tenants", tenantRepository.findAll());
        return "tenancy/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("tenancy") Tenancy tenancy, BindingResult result,
                          Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("rooms", roomRepository.findAll());
            model.addAttribute("tenants", tenantRepository.findAll());
            return "tenancy/form";
        }
        try {
            Tenancy saved = tenancyService.save(tenancy);
            redirectAttributes.addFlashAttribute("success", "Tenancy saved.");
            return "redirect:/tenancies/" + saved.getId();
        } catch (TenancyService.ValidationException e) {
            model.addAttribute("rooms", roomRepository.findAll());
            model.addAttribute("tenants", tenantRepository.findAll());
            model.addAttribute("error", e.getMessage());
            return "tenancy/form";
        }
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("tenancy") Tenancy tenancy,
                          BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("rooms", roomRepository.findAll());
            model.addAttribute("tenants", tenantRepository.findAll());
            return "tenancy/form";
        }
        tenancy.setId(id);
        try {
            tenancyService.save(tenancy);
            redirectAttributes.addFlashAttribute("success", "Tenancy updated.");
            return "redirect:/tenancies/" + id;
        } catch (TenancyService.ValidationException e) {
            model.addAttribute("rooms", roomRepository.findAll());
            model.addAttribute("tenants", tenantRepository.findAll());
            model.addAttribute("error", e.getMessage());
            return "tenancy/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            tenancyService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Tenancy deleted.");
            return "redirect:/tenancies";
        } catch (TenancyService.DeleteBlockedException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/tenancies/" + id;
        }
    }
}
