package com.rentalapp.controller;

import com.rentalapp.domain.RentPayment;
import com.rentalapp.repository.RentPaymentRepository;
import com.rentalapp.repository.RoomRepository;
import com.rentalapp.repository.TenantRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/rent-payments")
public class RentPaymentController {

    private final RentPaymentRepository rentPaymentRepository;
    private final TenantRepository tenantRepository;
    private final RoomRepository roomRepository;

    public RentPaymentController(RentPaymentRepository rentPaymentRepository,
                                  TenantRepository tenantRepository,
                                  RoomRepository roomRepository) {
        this.rentPaymentRepository = rentPaymentRepository;
        this.tenantRepository = tenantRepository;
        this.roomRepository = roomRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("payments", rentPaymentRepository.findAllByOrderByDepositDateDesc());
        return "rentpayment/list";
    }

    @GetMapping("/{id}")
    public String show(@PathVariable Long id, Model model) {
        model.addAttribute("payment", rentPaymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rent payment not found: " + id)));
        return "rentpayment/show";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        RentPayment payment = new RentPayment();
        payment.setDepositDate(LocalDateTime.now());
        model.addAttribute("payment", payment);
        model.addAttribute("tenants", tenantRepository.findAll());
        model.addAttribute("rooms", roomRepository.findAll());
        return "rentpayment/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("payment", rentPaymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rent payment not found: " + id)));
        model.addAttribute("tenants", tenantRepository.findAll());
        model.addAttribute("rooms", roomRepository.findAll());
        return "rentpayment/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("payment") RentPayment payment, BindingResult result,
                          Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("tenants", tenantRepository.findAll());
            model.addAttribute("rooms", roomRepository.findAll());
            return "rentpayment/form";
        }
        RentPayment saved = rentPaymentRepository.save(payment);
        redirectAttributes.addFlashAttribute("success", "Rent payment recorded.");
        return "redirect:/rent-payments/" + saved.getId();
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("payment") RentPayment payment,
                          BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("tenants", tenantRepository.findAll());
            model.addAttribute("rooms", roomRepository.findAll());
            return "rentpayment/form";
        }
        payment.setId(id);
        rentPaymentRepository.save(payment);
        redirectAttributes.addFlashAttribute("success", "Rent payment updated.");
        return "redirect:/rent-payments/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        rentPaymentRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Rent payment deleted.");
        return "redirect:/rent-payments";
    }
}
