package com.rentalapp.controller;

import com.rentalapp.repository.CurrentTenancyRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Read-only over the current_tenancies view. Only index/show actions
 * exist - there is no create/edit/delete route, in code or in the UI,
 * matching the original.
 */
@Controller
@RequestMapping("/current-tenancies")
public class CurrentTenancyController {

    private final CurrentTenancyRepository currentTenancyRepository;

    public CurrentTenancyController(CurrentTenancyRepository currentTenancyRepository) {
        this.currentTenancyRepository = currentTenancyRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("rows", currentTenancyRepository.findAll());
        return "currenttenancy/list";
    }

    @GetMapping("/{roomId}")
    public String show(@PathVariable Long roomId, Model model) {
        model.addAttribute("row", currentTenancyRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId)));
        return "currenttenancy/show";
    }
}
