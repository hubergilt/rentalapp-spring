package com.rentalapp.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Mirrors `tenancies`. DB-level CHECK constraints reproduced here as
 * validation (also re-checked in TenancyService before save, since Bean
 * Validation alone can't express the cross-field date comparisons cleanly):
 *   - end_date is null OR end_date >= start_date
 *   - deposit_refund_amount is null OR >= 0
 *   - deposit_refund_date is null OR >= start_date
 */
@Entity
@Table(name = "tenancies")
public class Tenancy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Tenant is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @NotNull(message = "Room is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "deposit_refund_date")
    private LocalDate depositRefundDate;

    @DecimalMin(value = "0.0", message = "Deposit refund amount cannot be negative")
    @Column(name = "deposit_refund_amount", precision = 10, scale = 2)
    private BigDecimal depositRefundAmount;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public Tenancy() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDate getDepositRefundDate() {
        return depositRefundDate;
    }

    public void setDepositRefundDate(LocalDate depositRefundDate) {
        this.depositRefundDate = depositRefundDate;
    }

    public BigDecimal getDepositRefundAmount() {
        return depositRefundAmount;
    }

    public void setDepositRefundAmount(BigDecimal depositRefundAmount) {
        this.depositRefundAmount = depositRefundAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Transient
    public boolean isActive() {
        return endDate == null;
    }
}
