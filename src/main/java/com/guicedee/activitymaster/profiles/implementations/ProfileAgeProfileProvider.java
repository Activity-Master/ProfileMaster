package com.guicedee.activitymaster.profiles.implementations;

import com.google.inject.Inject;
import com.guicedee.activitymaster.fsdm.client.services.ISystemsService;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.dto.PartyAddressDTO;
import com.guicedee.activitymaster.fsdm.db.entities.geography.Geography;
import com.guicedee.activitymaster.fsdm.plugins.IAgeProfileProvider;
import com.guicedee.activitymaster.profiles.services.interfaces.IProfileService;
import com.guicedee.activitymaster.profiles.webdto.ComprehensiveProfileDTO;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.UUID;

/** Supplies the profile date of birth and residential address country for age restrictions. */
public class ProfileAgeProfileProvider implements IAgeProfileProvider {
    @Inject private ISystemsService<?> systems;
    @Inject private IProfileService<?> profiles;

    @Override
    public Uni<AgeProfile> find(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, UUID partyId) {
        return systems.doesSystemExist(session, enterprise, IProfileService.ProfileSystemName)
                .chain(exists -> !exists ? Uni.createFrom().item(AgeProfile.EMPTY)
                        : profiles.getProfile(session, enterprise, partyId)
                        .chain(profile -> country(session, profile)
                                .map(country -> new AgeProfile(dateOfBirth(profile.getDateOfBirth()), country))));
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

    private Uni<String> country(Mutiny.StatelessSession session, ComprehensiveProfileDTO profile) {
        UUID geography = null;
        if (profile.getAddresses() != null)
            for (PartyAddressDTO address : profile.getAddresses())
                if ("Residential".equals(address.purpose()) && address.geographies().get("Country") != null) {
                    geography = address.geographies().get("Country");
                    break;
                }
        if (geography == null) return Uni.createFrom().item(code(profile.getCountry()));
        return session.get(Geography.class, geography).map(row -> row == null ? null : code(row.getName()));
    }

    private static String code(String value) {
        return value != null && value.matches("[A-Za-z]{2}") ? value.toUpperCase(Locale.ROOT) : null;
    }
}
