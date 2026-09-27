package com.rentalapp.controller;

import com.rentalapp.domain.Tenant;
import com.rentalapp.service.TenantService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping
    public String list(@RequestParam(name = "q", required = false) String q, Model model) {
        model.addAttribute("tenants", tenantService.search(q));
        model.addAttribute("q", q == null ? "" : q);
        return "tenant/list";
    }

    @GetMapping("/{id}")
    public String show(@PathVariable Long id, Model model) {
        Tenant tenant = tenantService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + id));
        model.addAttribute("tenant", tenant);
        return "tenant/show";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("tenant", new Tenant());
        return "tenant/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Tenant tenant = tenantService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + id));
        model.addAttribute("tenant", tenant);
        return "tenant/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("tenant") Tenant tenant, BindingResult result,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "tenant/form";
        }
        Tenant saved = tenantService.save(tenant);
        redirectAttributes.addFlashAttribute("success", "Tenant saved.");
        return "redirect:/tenants/" + saved.getId();
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("tenant") Tenant tenant,
                          BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "tenant/form";
        }
        tenant.setId(id);
        tenantService.save(tenant);
        redirectAttributes.addFlashAttribute("success", "Tenant updated.");
        return "redirect:/tenants/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            tenantService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Tenant deleted.");
            return "redirect:/tenants";
        } catch (TenantService.DeleteBlockedException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/tenants/" + id;
        }
    }
}
