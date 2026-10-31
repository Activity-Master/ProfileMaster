package com.guicedee.activitymaster.profiles.services.interfaces;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;
import com.guicedee.activitymaster.profiles.webdto.ComprehensiveProfileDTO;
import com.guicedee.activitymaster.profiles.webdto.ProfileAttributeChoiceDTO;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface IProfileService<J extends IProfileService<J>>
{
	String ProfileSystemName = "Profiles Master";


	/** Stateless variant of {@link #listUsers(Mutiny.StatelessSession, IEnterprise, String...)}. */
	Uni<List<ProfileServiceDTO<?>>> listUsers(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, String... roles);

	/** Stateless variant of {@link #allUsers(Mutiny.StatelessSession, IEnterprise)}. */
	Uni<List<ProfileServiceDTO<?>>> allUsers(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise);

	/**
	 * Finds the involved party corresponding to the provided user profile DTO.
	 */
	Uni<IInvolvedParty<?, ?>> findInvolvedParty(Mutiny.StatelessSession session, ProfileServiceDTO<?> userDTO);

	/**
	 * Finds the involved party corresponding to the provided user profile DTO within the specified system scope.
	 */
	Uni<IInvolvedParty<?, ?>> findInvolvedParty(Mutiny.StatelessSession session, ISystems<?, ?> system, UUID systemToken, ProfileServiceDTO<?> userDTO);

	/**
	 * Finds all roles assigned to the given user profile DTO.
	 */
	Uni<Set<String>> findRoles(Mutiny.StatelessSession session, ProfileServiceDTO<?> userDTO);

	/**
	 * Finds all roles assigned to the given user profile DTO within the specified system scope.
	 */
	Uni<Set<String>> findRoles(Mutiny.StatelessSession session, ISystems<?, ?> system, UUID systemToken, ProfileServiceDTO<?> userDTO);

	/**
	 * Builds a detached-prepped involved party carrying only {@code profileId} as its id.
	 */
	IInvolvedParty<?, ?> preppedParty(UUID profileId);

	/**
	 * Stateless variant of {@link #saveProfile(Mutiny.StatelessSession, IEnterprise, ComprehensiveProfileDTO)} —
	 * provisions the involved party (and its name/classification links) entirely on a
	 * {@link Mutiny.StatelessSession}. Optimised for the new-profile (bulk insert) path: existing
	 * name/classification values are not retired/updated statelessly (the FSDM stateless link writes are
	 * find-or-insert), so use the {@link Mutiny.StatelessSession} overload when in-place field updates are required.
	 */
	Uni<UUID> saveProfile(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, ComprehensiveProfileDTO profile);

    /** Explicitly request grouped grants for eligible protected identification links. */
    Uni<UUID> saveProfileWithGroupedGrants(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, ComprehensiveProfileDTO profile);

    /** Replaces one prevalidated personal name without processing an entire profile DTO. */
    Uni<Boolean> replacePersonalName(Mutiny.StatelessSession session,IEnterprise<?, ?> enterprise,UUID profileId,
            com.guicedee.activitymaster.fsdm.client.services.classifications.types.NameTypes nameType,String expected,String value);

    /** Narrow update with explicitly requested grouped grants for eligible protected links. */
    Uni<Boolean> replacePersonalNameWithGroupedGrants(Mutiny.StatelessSession session,IEnterprise<?, ?> enterprise,UUID profileId,
            com.guicedee.activitymaster.fsdm.client.services.classifications.types.NameTypes nameType,String expected,String value);

    /** Scalar attributes whose canonical value is an encrypted identification link or its legacy classification. */
    static boolean supportsIndependentProtectedAttribute(String name) {
        return name != null && !name.isBlank()
                && !java.util.Set.of("Biography", "HomeLanguage", "SpokenLanguages",
                        "ResidentialAddress", "PostalAddress", "City", "Province", "PostalCode", "Country").contains(name);
    }

    /** Reads one supported protected attribute with the same legacy fallback as the full profile. */
    Uni<String> getProtectedAttribute(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, UUID profileId, String attribute);

    /** Residential address country with the protected/legacy Profile Country fallback, without full profile hydration. */
    Uni<String> getAgeCountry(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, UUID profileId);

    /** Replaces one supported protected attribute, reporting false for an identical value. */
    Uni<Boolean> replaceProtectedAttribute(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, UUID profileId,
            String attribute, String expected, String value);

    /** The same operation with explicitly requested grouped grants on eligible links. */
    Uni<Boolean> replaceProtectedAttributeWithGroupedGrants(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, UUID profileId,
            String attribute, String expected, String value);

    /** Reads one language selection with canonical protected and legacy precedence. */
    Uni<String> getProfileLanguage(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, UUID profileId, String attribute);

    /** Replaces one language selection after checking the expected value and published choices. */
    Uni<Boolean> replaceProfileLanguage(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, UUID profileId,
            String attribute, String expected, String value);

    /** Reads one personal, social or work name using the canonical protected and legacy precedence. */
    Uni<String> getProfileName(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, UUID profileId,
            String realm, com.guicedee.activitymaster.fsdm.client.services.classifications.types.NameTypes nameType);

    /** Replaces one name in its realm after checking the expected value. */
    Uni<Boolean> replaceProfileName(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, UUID profileId,
            String realm, com.guicedee.activitymaster.fsdm.client.services.classifications.types.NameTypes nameType, String expected, String value);

    /** The same realm-name operation with explicitly requested grouped grants. */
    Uni<Boolean> replaceProfileNameWithGroupedGrants(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, UUID profileId,
            String realm, com.guicedee.activitymaster.fsdm.client.services.classifications.types.NameTypes nameType, String expected, String value);

    /** Replaces a complete phone collection and returns its committed transaction view. */
    Uni<java.util.List<com.guicedee.activitymaster.fsdm.client.services.dto.PartyPhoneDTO>> replacePhoneCollection(
            Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, UUID profileId,
            java.util.List<com.guicedee.activitymaster.fsdm.client.services.dto.PartyPhoneDTO> phones);

    /** Replaces a complete address collection and returns its transaction view. */
    Uni<java.util.List<com.guicedee.activitymaster.fsdm.client.services.dto.PartyAddressDTO>> replaceAddressCollection(
            Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, UUID profileId,
            java.util.List<com.guicedee.activitymaster.fsdm.client.services.dto.PartyAddressDTO> addresses);

    /** Reads one personal name with the same protected-value precedence as the full Profile read. */
    Uni<String> getPersonalName(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, UUID profileId,
            com.guicedee.activitymaster.fsdm.client.services.classifications.types.NameTypes nameType);


	/**
	 * Stateless variant of {@link #getProfile(Mutiny.StatelessSession, IEnterprise, UUID)} — reads every stored
	 * name and attribute on a {@link Mutiny.StatelessSession} into a {@link ComprehensiveProfileDTO}.
	 * Because the FSDM party service exposes no stateless find-by-id, the read is performed against a
	 * detached-prepped party keyed by {@code profileId}; a profile with no stored data yields a DTO
	 * carrying only its id and enterprise rather than {@code null}.
	 */
	Uni<ComprehensiveProfileDTO> getProfile(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, UUID profileId);

	/**
	 * Reads the selectable values for each list-valued profile attribute (gender, pronouns, marital status)
	 * from the FSDM: the child classifications of each attribute classification, on a
	 * {@link Mutiny.StatelessSession}. Keys are attribute names; installed defaults keep their declared order
	 * and enterprise-added children follow alphabetically by label. An attribute without children maps to
	 * an empty list.
	 */
	Uni<Map<String, List<ProfileAttributeChoiceDTO>>> getAttributeChoices(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise);

    /** Read one published choice concept using the same canonical checks as the complete list. */
    Uni<List<ProfileAttributeChoiceDTO>> getAttributeChoicesFor(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, String attribute);

    /** Read only requested published choices while retaining the canonical hierarchy and concept checks. */
    Uni<List<ProfileAttributeChoiceDTO>> getAttributeChoicesForValues(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, String attribute, java.util.Set<String> values);
}
