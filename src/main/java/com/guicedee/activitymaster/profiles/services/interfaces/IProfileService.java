package com.guicedee.activitymaster.profiles.services.interfaces;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;
import com.guicedee.activitymaster.profiles.webdto.ComprehensiveProfileDTO;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.List;
import java.util.UUID;

public interface IProfileService<J extends IProfileService<J>>
{
	String ProfileSystemName = "Profiles Master";
	
	Uni<List<ProfileServiceDTO<?>>> listUsers(Mutiny.Session session, IEnterprise<?, ?> enterprise, String... roles);
	
	Uni<List<ProfileServiceDTO<?>>> allUsers(Mutiny.Session session, IEnterprise<?, ?> enterprise);
	
	Uni<Void> clearCache();

	/** Stateless variant of {@link #listUsers(Mutiny.Session, IEnterprise, String...)}. */
	Uni<List<ProfileServiceDTO<?>>> listUsers(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, String... roles);

	/** Stateless variant of {@link #allUsers(Mutiny.Session, IEnterprise)}. */
	Uni<List<ProfileServiceDTO<?>>> allUsers(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise);

	/**
	 * Persists an individual's comprehensive profile against their involved party, creating the party
	 * when {@link ComprehensiveProfileDTO#getProfileId()} resolves to an unknown/null id. Names are
	 * stored through the FSDM name-type mechanism and all other attributes (occupation, contact,
	 * demographics, etc.) as classification values.
	 *
	 * @param session    the reactive session
	 * @param enterprise the owning enterprise
	 * @param profile    the comprehensive profile to store
	 * @return a {@link Uni} emitting the profile id (the backing involved-party id)
	 */
	Uni<UUID> saveProfile(Mutiny.Session session, IEnterprise<?, ?> enterprise, ComprehensiveProfileDTO profile);

	/**
	 * Reads back the comprehensive profile for the given profile id, hydrating every stored name and
	 * attribute into a fully-populated {@link ComprehensiveProfileDTO}.
	 *
	 * @param session    the reactive session
	 * @param enterprise the owning enterprise
	 * @param profileId  the profile id (the backing involved-party id)
	 * @return a {@link Uni} emitting the hydrated profile, or {@code null} when none exists
	 */
	Uni<ComprehensiveProfileDTO> getProfile(Mutiny.Session session, IEnterprise<?, ?> enterprise, UUID profileId);

	/**
	 * Stateless variant of {@link #saveProfile(Mutiny.Session, IEnterprise, ComprehensiveProfileDTO)} —
	 * provisions the involved party (and its name/classification links) entirely on a
	 * {@link Mutiny.StatelessSession}. Optimised for the new-profile (bulk insert) path: existing
	 * name/classification values are not retired/updated statelessly (the FSDM stateless link writes are
	 * find-or-insert), so use the {@link Mutiny.Session} overload when in-place field updates are required.
	 */
	Uni<UUID> saveProfile(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, ComprehensiveProfileDTO profile);

	/**
	 * Stateless variant of {@link #getProfile(Mutiny.Session, IEnterprise, UUID)} — reads every stored
	 * name and attribute on a {@link Mutiny.StatelessSession} into a {@link ComprehensiveProfileDTO}.
	 * Because the FSDM party service exposes no stateless find-by-id, the read is performed against a
	 * detached-prepped party keyed by {@code profileId}; a profile with no stored data yields a DTO
	 * carrying only its id and enterprise rather than {@code null}.
	 */
	Uni<ComprehensiveProfileDTO> getProfile(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, UUID profileId);
}
