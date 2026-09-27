package com.rentalapp.controller;

import com.rentalapp.domain.Room;
import com.rentalapp.repository.RentPaymentRepository;
import com.rentalapp.repository.RoomRepository;
import com.rentalapp.repository.TenancyRepository;
import com.rentalapp.repository.TenantRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Landing page after login - the original Grails app didn't have one
 * (its README describes the login screen as "the gateway into every
 * scaffolded CRUD screen", with no stats page), but a summary view is a
 * natural, low-risk addition on top of that: occupancy at a glance, this
 * month's collections, and a couple of recent-activity feeds, all read
 * straight off the same repositories every other screen already uses.
 */
@Controller
public class DashboardController {

    private final TenantRepository tenantRepository;
    private final RoomRepository roomRepository;
    private final TenancyRepository tenancyRepository;
    private final RentPaymentRepository rentPaymentRepository;

    public DashboardController(TenantRepository tenantRepository,
                                RoomRepository roomRepository,
                                TenancyRepository tenancyRepository,
                                RentPaymentRepository rentPaymentRepository) {
        this.tenantRepository = tenantRepository;
        this.roomRepository = roomRepository;
        this.tenancyRepository = tenancyRepository;
        this.rentPaymentRepository = rentPaymentRepository;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        long totalTenants = tenantRepository.count();

        long totalRooms = roomRepository.count();
        long occupiedRooms = roomRepository.countByStatus(Room.Status.occupied);
        long availableRooms = roomRepository.countByStatus(Room.Status.available);
        long maintenanceRooms = roomRepository.countByStatus(Room.Status.maintenance);

        long activeTenancies = tenancyRepository.countByEndDateIsNull();
        long endedTenancies = tenancyRepository.countByEndDateIsNotNull();

        YearMonth thisMonth = YearMonth.now();
        LocalDate monthStart = thisMonth.atDay(1);
        LocalDate monthEnd = thisMonth.atEndOfMonth();
        long paymentsThisMonth = rentPaymentRepository.countByPeriodBetween(monthStart, monthEnd);
        var totalThisMonth = rentPaymentRepository.sumAmountByPeriodBetween(monthStart, monthEnd);

        model.addAttribute("totalTenants", totalTenants);
        model.addAttribute("totalRooms", totalRooms);
        model.addAttribute("occupiedRooms", occupiedRooms);
        model.addAttribute("availableRooms", availableRooms);
        model.addAttribute("maintenanceRooms", maintenanceRooms);
        model.addAttribute("activeTenancies", activeTenancies);
        model.addAttribute("endedTenancies", endedTenancies);
        model.addAttribute("paymentsThisMonth", paymentsThisMonth);
        model.addAttribute("totalThisMonth", totalThisMonth);
        model.addAttribute("monthLabel", thisMonth);

        model.addAttribute("recentPayments", rentPaymentRepository.findTop5ByOrderByDepositDateDesc());
        model.addAttribute("recentTenancies", tenancyRepository.findTop5ByOrderByStartDateDesc());

        return "dashboard";
    }
}
