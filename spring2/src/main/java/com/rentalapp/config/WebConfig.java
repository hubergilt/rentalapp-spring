package com.rentalapp.config;

import com.rentalapp.domain.Room;
import com.rentalapp.domain.Tenancy;
import com.rentalapp.domain.Tenant;
import com.rentalapp.repository.RoomRepository;
import com.rentalapp.repository.TenancyRepository;
import com.rentalapp.repository.TenantRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * A <select th:field="*{tenant}"> submits just the id as a string. Spring
 * MVC needs an explicit Converter<String, Entity> to turn that back into
 * the managed entity before validation/save - without this, form binding
 * for every *-to-one relationship (Tenancy.tenant, Tenancy.room,
 * SecurityDeposit.tenancy, RentPayment.tenant/room) would fail.
 *
 * Registered via the 3-arg addConverter(Class<S>, Class<T>, Converter)
 * overload - NOT the 1-arg generic addConverter(Converter<S,T>) overload.
 * The 1-arg overload uses reflection to infer S/T from the converter
 * object's own generic signature, which only works for named classes;
 * lambdas compile to synthetic classes with no reifiable generic
 * signature, so Spring throws "Unable to determine source type <S> and
 * target type <T>" at startup. Passing the Class objects explicitly
 * sidesteps that reflection entirely and works fine with lambdas.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final TenantRepository tenantRepository;
    private final RoomRepository roomRepository;
    private final TenancyRepository tenancyRepository;

    public WebConfig(TenantRepository tenantRepository,
                      RoomRepository roomRepository,
                      TenancyRepository tenancyRepository) {
        this.tenantRepository = tenantRepository;
        this.roomRepository = roomRepository;
        this.tenancyRepository = tenancyRepository;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, Tenant.class,
                (Converter<String, Tenant>) source -> parseId(source, tenantRepository::findById));
        registry.addConverter(String.class, Room.class,
                (Converter<String, Room>) source -> parseId(source, roomRepository::findById));
        registry.addConverter(String.class, Tenancy.class,
                (Converter<String, Tenancy>) source -> parseId(source, tenancyRepository::findById));
    }

    private <T> T parseId(String source, java.util.function.Function<Long, java.util.Optional<T>> lookup) {
        if (source == null || source.isBlank()) {
            return null;
        }
        return lookup.apply(Long.valueOf(source)).orElse(null);
    }
}
