package com.guicedee.activitymaster.profiles;

import com.google.inject.Inject;
import com.guicedee.activitymaster.fsdm.client.services.IActivityMasterService;
import com.guicedee.activitymaster.fsdm.client.services.IInvolvedPartyService;
import com.guicedee.activitymaster.fsdm.client.services.IPasswordsService;
import com.guicedee.activitymaster.fsdm.client.services.IRelationshipValue;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedPartyNameType;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.classifications.types.NameTypes;
import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;
import com.guicedee.activitymaster.profiles.services.interfaces.IProfileService;
import com.guicedee.activitymaster.profiles.webdto.ComprehensiveProfileDTO;
import com.guicedee.client.utils.Pair;
import io.smallrye.mutiny.Uni;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import static com.guicedee.activitymaster.fsdm.client.services.IActivityMasterService.getISystem;
import static com.guicedee.activitymaster.fsdm.client.services.IActivityMasterService.getISystemToken;
import static com.guicedee.activitymaster.fsdm.client.services.classifications.DefaultClassifications.NoClassification;
import static com.guicedee.activitymaster.profiles.enumerations.ProfileIdentificationTypes.IdentificationTypeWebClientUUID;

public class ProfileService
		implements IProfileService<ProfileService>
{
	private static final Logger log = LogManager.getLogger(ProfileService.class);
	
	@Inject
	private IPasswordsService<?> passwordsService;

	@Inject
	private com.guicedee.activitymaster.profiles.services.interfaces.IRolesService<?> rolesService;

	@Inject
	private IInvolvedPartyService<?> involvedPartyService;


	@Override
	public Uni<List<ProfileServiceDTO<?>>> listUsers(Mutiny.Session session, IEnterprise<?, ?> enterprise, String... roles)
	{
		return allUsers(session, enterprise)
			.map(users -> {
				List<ProfileServiceDTO<?>> filtered = new ArrayList<>();
				for (ProfileServiceDTO<?> user : users)
				{
					for (String role : roles)
					{
						if (user.findRoles(session).contains(role))
						{
							filtered.add(user);
						}
					}
				}
				return filtered;
			})
			.onFailure().invoke(error -> log.error("Error listing users: {}", error.getMessage(), error));
	}

	//@CacheResult(cacheName = "UserProfiles")
	@Override
	
	public Uni<List<ProfileServiceDTO<?>>> allUsers(Mutiny.Session session, IEnterprise<?, ?> enterprise)
	{
		// Get system and token using reactive helper methods
		return getISystem(session, ProfileSystemName, enterprise)
			.chain(system -> {
				return getISystemToken(session, ProfileSystemName, enterprise)
					.chain(token -> {
						// Now use the system and token to get all users
						return passwordsService.getAllUsers(session, system, token)
							.chain(allIds -> {
								List<Uni<ProfileServiceDTO<?>>> profileDtoUnis = new ArrayList<>();
								
								for (IInvolvedParty<?, ?> allId : allIds) {
									ProfileServiceDTO<?> profileServiceDTO = new ProfileServiceDTO<>();
									profileServiceDTO.setInvolvedParty(allId);
									
									// Get system and token for each involved party
									Uni<ProfileServiceDTO<?>> dtoUni = getISystem(session, ProfileSystemName, enterprise)
										.chain(innerSystem -> {
											return getISystemToken(session, ProfileSystemName, enterprise)
												.chain(innerToken -> {
													return allId.findInvolvedPartyIdentificationType(
                                                                    session, NoClassification,
														IdentificationTypeWebClientUUID, 
														null,
														innerSystem, 
														true, 
														true, 
														innerToken
													)
													.map(idType -> {
														if (idType != null) {
															profileServiceDTO.setWebClientUUID(idType.getValueAsUUID());
														}
														return profileServiceDTO;
													});
												});
										});
									
									profileDtoUnis.add(dtoUni);
								}
								
								// Handle empty list case
								if (profileDtoUnis.isEmpty()) {
									return Uni.createFrom().item(Collections.<ProfileServiceDTO<?>>emptyList());
								}
								
								// Process all DTOs in parallel
								return Uni.join().all(profileDtoUnis).andCollectFailures();
							});
					});
			})
			.onFailure().invoke(error -> log.error("Error getting all users: {}", error.getMessage(), error));
	}

	//@CacheRemove(cacheName = "UserProfiles")
	@Override
	public Uni<Void> clearCache()
	{
		// Since the original method was empty, we just return a completed Uni
		return Uni.createFrom().voidItem();
	}

	// ---- Stateless twins ----

	@Override
	public Uni<List<ProfileServiceDTO<?>>> allUsers(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise)
	{
		return getISystem(session, ProfileSystemName, enterprise)
			.chain(system -> getISystemToken(session, ProfileSystemName, enterprise)
				.chain(token -> passwordsService.getAllUsers(session, system, token)
					.chain(allIds -> {
						Uni<List<ProfileServiceDTO<?>>> chain = Uni.createFrom().item(new ArrayList<>());
						for (IInvolvedParty<?, ?> allId : allIds) {
							chain = chain.chain(acc -> allId.findInvolvedPartyIdentificationType(session, NoClassification.toString(),
											IdentificationTypeWebClientUUID.toString(), null, system, true, true, token)
									.onFailure().recoverWithItem(() -> null)
									.map(idType -> {
										ProfileServiceDTO<?> dto = new ProfileServiceDTO<>();
										dto.setInvolvedParty(allId);
										if (idType != null) { dto.setWebClientUUID(idType.getValueAsUUID()); }
										acc.add(dto);
										return acc;
									}));
						}
						return chain;
					})))
			.onFailure().invoke(error -> log.error("Error getting all users (stateless): {}", error.getMessage(), error));
	}

	@Override
	public Uni<List<ProfileServiceDTO<?>>> listUsers(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, String... roles)
	{
		return getISystem(session, ProfileSystemName, enterprise)
			.chain(system -> allUsers(session, enterprise).chain(users -> {
				Uni<List<ProfileServiceDTO<?>>> chain = Uni.createFrom().item(new ArrayList<>());
				for (ProfileServiceDTO<?> user : users) {
					chain = chain.chain(acc -> rolesService.getRoles(session, user.getInvolvedParty(), system).map(have -> {
						for (String role : roles) { if (have.contains(role)) { acc.add(user); break; } }
						return acc;
					}));
				}
				return chain;
			}));
	}

	// ---- Comprehensive profile storage ----

	@Override
	public Uni<UUID> saveProfile(Mutiny.Session session, IEnterprise<?, ?> enterprise, ComprehensiveProfileDTO profile)
	{
		final UUID profileId = profile.getProfileId() != null ? profile.getProfileId() : UUID.randomUUID();
		return getISystem(session, ProfileSystemName, enterprise)
			.chain(system -> getISystemToken(session, ProfileSystemName, enterprise)
				.chain(token -> resolveOrCreateParty(session, system, profileId, token)
					.chain(party -> persistNames(session, party, profile, system, token)
						.chain(() -> persistAttributes(session, party, profile, system, token))
						.replaceWith(profileId))))
			.onFailure().invoke(error -> log.error("Error saving profile {}: {}", profileId, error.getMessage(), error));
	}

	@Override
	public Uni<ComprehensiveProfileDTO> getProfile(Mutiny.Session session, IEnterprise<?, ?> enterprise, UUID profileId)
	{
		return getISystem(session, ProfileSystemName, enterprise)
			.chain(system -> getISystemToken(session, ProfileSystemName, enterprise)
				.chain(token -> involvedPartyService.find(session, profileId)
					.onFailure().recoverWithItem(() -> null)
					.chain(party -> {
						if (party == null)
						{
							return Uni.createFrom().<ComprehensiveProfileDTO>nullItem();
						}
						ComprehensiveProfileDTO dto = new ComprehensiveProfileDTO();
						dto.setProfileId(profileId);
						dto.setEnterpriseName(enterprise.getName());
						return party.findClassificationValues(session, system, token)
							.invoke(dto::applyAttributeValues)
							.chain(ignored -> hydrateNames(session, party, dto, system, token))
							.replaceWith(dto);
					})))
			.onFailure().invoke(error -> log.error("Error reading profile {}: {}", profileId, error.getMessage(), error));
	}

	private Uni<IInvolvedParty<?, ?>> resolveOrCreateParty(Mutiny.Session session, ISystems<?, ?> system, UUID profileId, UUID token)
	{
		Pair<String, String> idTypes = new Pair<>(IdentificationTypeWebClientUUID.toString(), profileId.toString());
		return involvedPartyService.find(session, profileId)
			.onItem().ifNull().switchTo(() -> involvedPartyService.create(session, system, profileId, idTypes, true, token))
			.onFailure().recoverWithUni(() -> involvedPartyService.create(session, system, profileId, idTypes, true, token));
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private Uni<Void> persistNames(Mutiny.Session session, IInvolvedParty<?, ?> party, ComprehensiveProfileDTO profile, ISystems<?, ?> system, UUID token)
	{
		IInvolvedParty raw = party;
		Uni<Void> chain = Uni.createFrom().voidItem();
		for (Map.Entry<NameTypes, String> entry : profile.toNameValues().entrySet())
		{
			final NameTypes nameType = entry.getKey();
			final String value = entry.getValue();
			chain = chain.chain(() -> raw.addOrUpdateInvolvedPartyNameType(session,
					NoClassification.classificationValue(), nameType.toString(), value, value, system, token)
				.replaceWithVoid());
		}
		return chain;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private Uni<Void> persistAttributes(Mutiny.Session session, IInvolvedParty<?, ?> party, ComprehensiveProfileDTO profile, ISystems<?, ?> system, UUID token)
	{
		IInvolvedParty raw = party;
		Uni<Void> chain = Uni.createFrom().voidItem();
		for (Map.Entry<String, String> entry : profile.toAttributeValues().entrySet())
		{
			final String classificationName = entry.getKey();
			final String value = entry.getValue();
			chain = chain.chain(() -> raw.addOrUpdateClassification(session, classificationName, value, system, token)
				.replaceWithVoid());
		}
		return chain;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private Uni<Void> hydrateNames(Mutiny.Session session, IInvolvedParty<?, ?> party, ComprehensiveProfileDTO dto, ISystems<?, ?> system, UUID token)
	{
		IInvolvedParty raw = party;
		Uni<Void> chain = Uni.createFrom().voidItem();
		for (NameTypes nameType : NameTypes.values())
		{
			chain = chain.chain(() -> ((Uni<IRelationshipValue<?, IInvolvedPartyNameType<?, ?>, ?>>) (Uni<?>) raw.findInvolvedPartyNameType(session,
					NoClassification.classificationValue(), nameType.toString(), null, system, true, true, token))
				.map(relationship -> relationship == null ? null : relationship.getValue())
				.onFailure().recoverWithItem(() -> null)
				.invoke(value -> {
					if (value != null)
					{
						dto.applyName(nameType, value);
					}
				})
				.replaceWithVoid());
		}
		return chain;
	}

	// ---- Comprehensive profile storage (stateless twins) ----

	@Override
	public Uni<UUID> saveProfile(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, ComprehensiveProfileDTO profile)
	{
		final UUID profileId = profile.getProfileId() != null ? profile.getProfileId() : UUID.randomUUID();
		return getISystem(session, ProfileSystemName, enterprise)
			.chain(system -> getISystemToken(session, ProfileSystemName, enterprise)
				.chain(token -> resolveOrCreateParty(session, system, profileId, token)
					.chain(party -> persistNames(session, party, profile, system, token)
						.chain(() -> persistAttributes(session, party, profile, system, token))
						.replaceWith(profileId))))
			.onFailure().invoke(error -> log.error("Error saving profile {} (stateless): {}", profileId, error.getMessage(), error));
	}

	@Override
	public Uni<ComprehensiveProfileDTO> getProfile(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, UUID profileId)
	{
		return getISystem(session, ProfileSystemName, enterprise)
			.chain(system -> getISystemToken(session, ProfileSystemName, enterprise)
				.chain(token -> {
					IInvolvedParty<?, ?> party = preppedParty(profileId);
					ComprehensiveProfileDTO dto = new ComprehensiveProfileDTO();
					dto.setProfileId(profileId);
					dto.setEnterpriseName(enterprise.getName());
					return party.findClassificationValues(session, system, token)
						.invoke(dto::applyAttributeValues)
						.chain(ignored -> hydrateNames(session, party, dto, system, token))
						.replaceWith(dto);
				}))
			.onFailure().invoke(error -> log.error("Error reading profile {} (stateless): {}", profileId, error.getMessage(), error));
	}

	/**
	 * Builds a detached-prepped involved party carrying only {@code profileId} as its id. The FSDM
	 * link capabilities ({@code findLink((J) this, …)}) filter by the primary's id, so a prepped party
	 * is sufficient to read/write the profile's name and classification links without a stateless
	 * find-by-id (which the party service does not expose).
	 */
	private IInvolvedParty<?, ?> preppedParty(UUID profileId)
	{
		IInvolvedParty<?, ?> prepped = involvedPartyService.get();
		prepped.setId(profileId);
		return prepped;
	}

	private Uni<IInvolvedParty<?, ?>> resolveOrCreateParty(Mutiny.StatelessSession session, ISystems<?, ?> system, UUID profileId, UUID token)
	{
		Pair<String, String> idTypes = new Pair<>(IdentificationTypeWebClientUUID.toString(), profileId.toString());
		// New profile: stateless insert. Existing profile: the insert fails on the PK, so fall back to a
		// detached-prepped reference (the row already exists) to attach further links to.
		return involvedPartyService.create(session, system, profileId, idTypes, true, token)
			.onFailure().recoverWithItem(() -> preppedParty(profileId));
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private Uni<Void> persistNames(Mutiny.StatelessSession session, IInvolvedParty<?, ?> party, ComprehensiveProfileDTO profile, ISystems<?, ?> system, UUID token)
	{
		IInvolvedParty raw = party;
		Uni<Void> chain = Uni.createFrom().voidItem();
		for (Map.Entry<NameTypes, String> entry : profile.toNameValues().entrySet())
		{
			final NameTypes nameType = entry.getKey();
			final String value = entry.getValue();
			chain = chain.chain(() -> raw.addOrReuseInvolvedPartyNameType(session,
					NoClassification.classificationValue(), nameType.toString(), value, system, token));
		}
		return chain;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private Uni<Void> persistAttributes(Mutiny.StatelessSession session, IInvolvedParty<?, ?> party, ComprehensiveProfileDTO profile, ISystems<?, ?> system, UUID token)
	{
		IInvolvedParty raw = party;
		Uni<Void> chain = Uni.createFrom().voidItem();
		for (Map.Entry<String, String> entry : profile.toAttributeValues().entrySet())
		{
			final String classificationName = entry.getKey();
			final String value = entry.getValue();
			chain = chain.chain(() -> raw.addOrUpdateClassification(session, classificationName, (String) null, value, system, token));
		}
		return chain;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private Uni<Void> hydrateNames(Mutiny.StatelessSession session, IInvolvedParty<?, ?> party, ComprehensiveProfileDTO dto, ISystems<?, ?> system, UUID token)
	{
		IInvolvedParty raw = party;
		Uni<Void> chain = Uni.createFrom().voidItem();
		for (NameTypes nameType : NameTypes.values())
		{
			chain = chain.chain(() -> ((Uni<IRelationshipValue<?, IInvolvedPartyNameType<?, ?>, ?>>) (Uni<?>) raw.findInvolvedPartyNameType(session,
					NoClassification.classificationValue(), nameType.toString(), null, system, true, true, token))
				.map(relationship -> relationship == null ? null : relationship.getValue())
				.onFailure().recoverWithItem(() -> null)
				.invoke(value -> {
					if (value != null)
					{
						dto.applyName(nameType, value);
					}
				})
				.replaceWithVoid());
		}
		return chain;
	}
}
