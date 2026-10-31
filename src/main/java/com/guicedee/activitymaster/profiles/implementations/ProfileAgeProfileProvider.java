package com.guicedee.activitymaster.profiles.implementations;

import com.google.inject.Inject;
import com.guicedee.activitymaster.fsdm.client.services.ISystemsService;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.plugins.IAgeProfileProvider;
import com.guicedee.activitymaster.profiles.services.interfaces.IProfileService;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.UUID;

/** Supplies the profile date of birth and residential address country for age restrictions. */
public class ProfileAgeProfileProvider implements IAgeProfileProvider {
    @Inject private ISystemsService<?> systems;
    @Inject private IProfileService<?> profiles;

    @Override
    public Uni<LocalDate> dateOfBirth(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, UUID partyId) {
        return systems.doesSystemExist(session, enterprise, IProfileService.ProfileSystemName)
                .chain(exists -> !exists ? Uni.createFrom().nullItem()
                        : profiles.getProtectedAttribute(session, enterprise, partyId, "DateOfBirth")
                                .map(ProfileAgeProfileProvider::dateOfBirth));
    }

    @Override
    public Uni<String> country(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, UUID partyId) {
        return systems.doesSystemExist(session, enterprise, IProfileService.ProfileSystemName)
                .chain(exists -> !exists ? Uni.createFrom().nullItem()
                        : profiles.getAgeCountry(session, enterprise, partyId));
    }

    static LocalDate dateOfBirth(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            LocalDate date = LocalDate.parse(value.strip());
            return date.isAfter(LocalDate.now(java.time.ZoneOffset.UTC)) ? null : date;
        } catch (DateTimeParseException invalid) {
            return null;
        }
    }

}
