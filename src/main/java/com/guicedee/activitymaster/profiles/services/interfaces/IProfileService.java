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
	
	
	
	Uni<Void> clearCache();

	/** Stateless variant of {@link #listUsers(Mutiny.StatelessSession, IEnterprise, String...)}. */
	Uni<List<ProfileServiceDTO<?>>> listUsers(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, String... roles);

	/** Stateless variant of {@link #allUsers(Mutiny.StatelessSession, IEnterprise)}. */
	Uni<List<ProfileServiceDTO<?>>> allUsers(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise);

	/**
	 * Stateless variant of {@link #saveProfile(Mutiny.StatelessSession, IEnterprise, ComprehensiveProfileDTO)} —
	 * provisions the involved party (and its name/classification links) entirely on a
	 * {@link Mutiny.StatelessSession}. Optimised for the new-profile (bulk insert) path: existing
	 * name/classification values are not retired/updated statelessly (the FSDM stateless link writes are
	 * find-or-insert), so use the {@link Mutiny.StatelessSession} overload when in-place field updates are required.
	 */
	Uni<UUID> saveProfile(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, ComprehensiveProfileDTO profile);

	/**
	 * Stateless variant of {@link #getProfile(Mutiny.StatelessSession, IEnterprise, UUID)} — reads every stored
	 * name and attribute on a {@link Mutiny.StatelessSession} into a {@link ComprehensiveProfileDTO}.
	 * Because the FSDM party service exposes no stateless find-by-id, the read is performed against a
	 * detached-prepped party keyed by {@code profileId}; a profile with no stored data yields a DTO
	 * carrying only its id and enterprise rather than {@code null}.
	 */
	Uni<ComprehensiveProfileDTO> getProfile(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, UUID profileId);
}
