package com.rentalapp.controller;

import com.rentalapp.domain.Room;
import com.rentalapp.repository.RoomRepository;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/rooms")
public class RoomController {

    private final RoomRepository roomRepository;

    public RoomController(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("rooms", roomRepository.findAll());
        return "room/list";
    }

    @GetMapping("/{id}")
    public String show(@PathVariable Long id, Model model) {
        model.addAttribute("room", roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + id)));
        return "room/show";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("room", new Room());
        return "room/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("room", roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + id)));
        return "room/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("room") Room room, BindingResult result,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "room/form";
        }
        Room saved = roomRepository.save(room);
        redirectAttributes.addFlashAttribute("success", "Room saved.");
        return "redirect:/rooms/" + saved.getId();
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("room") Room room,
                          BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "room/form";
        }
        room.setId(id);
        roomRepository.save(room);
        redirectAttributes.addFlashAttribute("success", "Room updated.");
        return "redirect:/rooms/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            roomRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Room deleted.");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error",
                "This room is referenced by existing tenancies or rent payments and cannot be deleted.");
        }
        return "redirect:/rooms";
    }
}
