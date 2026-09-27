package com.rentalapp.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;
import org.hibernate.annotations.Synchronize;

import java.time.LocalDate;

/**
 * Read-only mapping over the `current_tenancies` VIEW - never created,
 * altered, inserted into, or deleted from here. Only index/show
 * operations exist, in code and in the UI, matching the original.
 *
 * The view is a LEFT JOIN from `rooms`, so there is one row PER ROOM
 * (including vacant ones), not one row per active tenancy. Vacant rooms
 * come back with null tenant_id/name fields - see {@link #isOccupied()}.
 *
 * @Subselect avoids needing a real @Table mapped to a view name that some
 * dialects/tools are picky about; @Synchronize tells Hibernate which real
 * tables to flush before querying this so reads stay consistent within a
 * transaction.
 */
@Entity
@Immutable
// Plain concatenated string, not a text block (""") - text blocks are Java
// 15+ syntax and this project targets Java 11 for Tomcat 9 / JBoss EAP 7.4
// compatibility. Concatenation of only string literals is still a
// compile-time constant expression, so this remains valid as an annotation
// attribute value.
@Subselect("select "
        + "    r.id as room_id, "
        + "    r.name as name, "
        + "    r.floor as floor, "
        + "    r.status as status, "
        + "    t.id as tenant_id, "
        + "    t.first_names as first_names, "
        + "    t.paternal_surname as paternal_surname, "
        + "    t.maternal_surname as maternal_surname, "
        + "    tc.start_date as start_date "
        + "from rooms r "
        + "left join tenancies tc on tc.room_id = r.id and tc.end_date is null "
        + "left join tenants t on t.id = tc.tenant_id")
@Synchronize({"rooms", "tenancies", "tenants"})
public class CurrentTenancy {

    @Id
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "name")
    private String name;

    @Column(name = "floor")
    private String floor;

    @Column(name = "status")
    private String status;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "first_names")
    private String firstNames;

    @Column(name = "paternal_surname")
    private String paternalSurname;

    @Column(name = "maternal_surname")
    private String maternalSurname;

    @Column(name = "start_date")
    private LocalDate startDate;

    public CurrentTenancy() {
    }

    public Long getRoomId() {
        return roomId;
    }

    public String getName() {
        return name;
    }

    public String getFloor() {
        return floor;
    }

    public String getStatus() {
        return status;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public String getFirstNames() {
        return firstNames;
    }

    public String getPaternalSurname() {
        return paternalSurname;
    }

    public String getMaternalSurname() {
        return maternalSurname;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public boolean isOccupied() {
        return tenantId != null;
    }

    public String getTenantFullName() {
        if (!isOccupied()) {
            return null;
        }
        return firstNames + " " + paternalSurname + " " + maternalSurname;
    }
}
