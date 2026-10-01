package com.guicedee.activitymaster.profiles;

import com.google.inject.Inject;
import com.guicedee.activitymaster.fsdm.client.services.IActivityMasterService;
import com.guicedee.activitymaster.fsdm.client.services.IActiveFlagService;
import com.guicedee.activitymaster.fsdm.client.services.IClassificationService;
import com.guicedee.activitymaster.fsdm.client.services.IInvolvedPartyService;
import com.guicedee.activitymaster.fsdm.client.services.IPasswordsService;
import com.guicedee.activitymaster.fsdm.client.services.IRelationshipValue;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedPartyNameType;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.classifications.types.NameTypes;
import com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts;
import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;
import com.guicedee.activitymaster.profiles.enumerations.ProfileAttributeChoices;
import com.guicedee.activitymaster.profiles.enumerations.ProfileAttributes;
import com.guicedee.activitymaster.profiles.enumerations.ProfileChoiceConcepts;
import com.guicedee.activitymaster.profiles.enumerations.ProfileNameRealms;
import com.guicedee.activitymaster.profiles.enumerations.ProfileProtectedFields;
import com.guicedee.activitymaster.profiles.enumerations.ProfileLanguageCatalog;
import com.guicedee.activitymaster.profiles.services.interfaces.IProfileService;
import com.guicedee.activitymaster.profiles.webdto.ComprehensiveProfileDTO;
import com.guicedee.activitymaster.profiles.webdto.ProfileAttributeChoiceDTO;
import com.guicedee.client.utils.Pair;
import io.smallrye.mutiny.Uni;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
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

	@Inject
	private IClassificationService<?> classifications;

	@Inject
	private IActiveFlagService<?> activeFlags;

    @Inject private com.guicedee.activitymaster.fsdm.client.services.IAddressService<?> addresses;
    @Inject private ProfileLanguageSelections languages;

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
										dto.setIdentityToken(allId.getId());
										dto.setEnterprise(enterprise);
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
					chain = chain.chain(acc -> findRoles(session, user).map(have -> {
						for (String role : roles) { if (have.contains(role)) { acc.add(user); break; } }
						return acc;
					}));
				}
				return chain;
			}));
	}

	@Override
	public Uni<IInvolvedParty<?, ?>> findInvolvedParty(Mutiny.StatelessSession session, ProfileServiceDTO<?> userDTO)
	{
		if (userDTO == null)
		{
			return Uni.createFrom().nullItem();
		}
		return getISystem(session, ProfileSystemName, userDTO.getEnterprise())
				.chain(system -> getISystemToken(session, ProfileSystemName, userDTO.getEnterprise())
						.chain(systemToken -> findInvolvedParty(session, system, systemToken, userDTO)));
	}

	@Override
	public Uni<IInvolvedParty<?, ?>> findInvolvedParty(Mutiny.StatelessSession session, ISystems<?, ?> system, UUID systemToken, ProfileServiceDTO<?> userDTO)
	{
		if (userDTO == null)
		{
			return Uni.createFrom().nullItem();
		}
		if (userDTO.getIdentityToken() != null)
		{
			return Uni.createFrom().item(preppedParty(userDTO.getIdentityToken()));
		}
		if (userDTO.getWebClientUUID() != null)
		{
			return involvedPartyService.findAllByIdentificationType(session, IdentificationTypeWebClientUUID.toString(), userDTO.getWebClientUUID().toString())
					.map(results -> {
						if (results != null && !results.isEmpty() && results.get(0).getPrimary() != null)
						{
							IInvolvedParty<?, ?> party = results.get(0).getPrimary();
							userDTO.setIdentityToken(party.getId());
							return party;
						}
						IInvolvedParty<?, ?> party = preppedParty(userDTO.getWebClientUUID());
						userDTO.setIdentityToken(party.getId());
						return party;
					})
					.onFailure().recoverWithItem(() -> {
						IInvolvedParty<?, ?> party = preppedParty(userDTO.getWebClientUUID());
						userDTO.setIdentityToken(party.getId());
						return party;
					});
		}
		return Uni.createFrom().nullItem();
	}

	@Override
	public Uni<Set<String>> findRoles(Mutiny.StatelessSession session, ProfileServiceDTO<?> userDTO)
	{
		if (userDTO == null)
		{
			Set<String> guest = new TreeSet<>();
			guest.add("Guest");
			return Uni.createFrom().item(guest);
		}
		return getISystem(session, ProfileSystemName, userDTO.getEnterprise())
				.chain(system -> getISystemToken(session, ProfileSystemName, userDTO.getEnterprise())
						.chain(systemToken -> findRoles(session, system, systemToken, userDTO)));
	}

	@Override
	public Uni<Set<String>> findRoles(Mutiny.StatelessSession session, ISystems<?, ?> system, UUID systemToken, ProfileServiceDTO<?> userDTO)
	{
		if (userDTO == null)
		{
			Set<String> guest = new TreeSet<>();
			guest.add("Guest");
			return Uni.createFrom().item(guest);
		}
		return findInvolvedParty(session, system, systemToken, userDTO)
				.chain(party -> rolesService.getRoles(session, party, system, systemToken))
				.onFailure().invoke(error -> log.error("Error finding roles (stateless): {}", error.getMessage(), error))
				.onFailure().recoverWithItem(() -> {
					Set<String> guest = new TreeSet<>();
					guest.add("Guest");
					return guest;
				});
	}

	// ---- Comprehensive profile storage ----

	// ---- Comprehensive profile storage (stateless twins) ----

	@Override
	public Uni<UUID> saveProfile(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, ComprehensiveProfileDTO profile)
	{
        for (var field : List.of("ResidentialAddress", "PostalAddress", "City", "Province", "PostalCode", "Country")) {
            String value = profile.toAttributeValues().get(field);
            if (value != null && !value.isEmpty())
                return Uni.createFrom().failure(new IllegalArgumentException("Use structured addresses with separate components and geography references"));
        }
		boolean suppliedValues = profile.toNameValues().values().stream().anyMatch(value -> !value.isEmpty())
			|| profile.toAttributeValues().entrySet().stream().anyMatch(entry -> !entry.getKey().equals(ProfileAttributes.Biography.name()) && !entry.getValue().isEmpty())
			|| (profile.getAddresses() != null && profile.getAddresses().stream().anyMatch(address -> !address.identifiers().isEmpty()))
			|| java.util.Arrays.stream(ProfileNameRealms.values()).anyMatch(realm ->
				profile.toRealmNameValues(realm).values().stream().anyMatch(value -> !value.isEmpty()));
		String mode = com.guicedee.client.Environment.getProperty("activitymaster.encryption.mode", "legacy");
		if (suppliedValues && !Set.of("aes-gcm", "enterprise").contains(mode))
			return Uni.createFrom().failure(new IllegalStateException("Authenticated encryption must be configured before storing personal profile values"));
		final UUID profileId = profile.getProfileId() != null ? profile.getProfileId() : UUID.randomUUID();
		return getISystem(session, ProfileSystemName, enterprise)
			.chain(system -> getISystemToken(session, ProfileSystemName, enterprise)
				.chain(token -> resolveOrCreateParty(session, system, profileId, token)
					.chain(party -> persistNames(session, party, profile, system, token)
						.chain(() -> persistAttributes(session, party, profile, system, token))
						.chain(() -> persistAddresses(session, party, profile, system, token))
						.chain(() -> retireLegacySelectedChoices(session, party, profile, system, token))
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
						.chain(() -> hydrateSelectedChoices(session, party, dto, system, token))
						.chain(() -> hydrateProtectedValues(session, party, dto, system, token))
                        .chain(() -> languages.read(session, party, "HomeLanguage", system, token)
                            .invoke(value -> { if (value != null) dto.setHomeLanguage(value); }).replaceWithVoid())
                        .chain(() -> languages.read(session, party, "SpokenLanguages", system, token)
                            .invoke(value -> { if (value != null) dto.setSpokenLanguages(value); }).replaceWithVoid())
						.chain(() -> addresses.findPartyAddresses(session, party, system, token).invoke(dto::setAddresses))
						.replaceWith(dto);
				}))
			.onFailure().invoke(error -> log.error("Error reading profile {} (stateless): {}", profileId, error.getMessage(), error));
	}

	@Override
	public Uni<Map<String, List<ProfileAttributeChoiceDTO>>> getAttributeChoices(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise)
	{
		return getISystem(session, ProfileSystemName, enterprise)
			.chain(system -> getISystemToken(session, ProfileSystemName, enterprise)
				.chain(token -> {
					Map<String, List<ProfileAttributeChoiceDTO>> choices = new LinkedHashMap<>();
					// Sequential reads: one statement at a time on the stateless connection.
					Uni<Void> chain = Uni.createFrom().voidItem();
					for (ProfileAttributes attribute : ProfileAttributeChoices.attributes())
					{
						chain = chain.chain(() -> attributeChoices(session, attribute, system, token)
							.invoke(list -> choices.put(attribute.name(), list))
							.replaceWithVoid());
					}
					return chain.replaceWith(choices);
				}))
			.onFailure().invoke(error -> log.error("Error reading profile attribute choices (stateless): {}", error.getMessage(), error));
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private Uni<List<ProfileAttributeChoiceDTO>> attributeChoices(Mutiny.StatelessSession session, ProfileAttributes attribute, ISystems<?, ?> system, UUID token)
	{
		String conceptName = ProfileChoiceConcepts.forAttribute(attribute).conceptName();
		return classifications.find(session, attribute.name(), system, token)
			.chain(parent -> ((IClassification) parent).findChildren(session, NoClassification.name(), null, system, token))
			.chain(links -> {
				List<IClassification<?, ?>> children = new ArrayList<>();
				Uni<Void> fetches = Uni.createFrom().voidItem();
				// The link's secondary is lazy; fetch each on the stateless session.
				for (Object link : (List<Object>) links)
				{
					fetches = fetches.chain(() -> session.fetch(((IRelationshipValue) link).getSecondary())
						.chain(child -> child instanceof IClassification<?, ?> classification
							? classifications.findInConcept(session, classification.getName(), conceptName, system, token)
								.invoke(scoped -> { if (classification.getId().equals(scoped.getId())) children.add(scoped); })
								.onFailure().recoverWithNull().replaceWithVoid()
							: Uni.createFrom().voidItem()));
				}
				return fetches.replaceWith(() -> sortedChoices(children));
			});
	}

	private static List<ProfileAttributeChoiceDTO> sortedChoices(List<IClassification<?, ?>> children)
	{
		Map<String, ProfileAttributeChoiceDTO> byName = new LinkedHashMap<>();
		for (IClassification<?, ?> child : children)
		{
			String name = child.getName();
			if (name == null || name.isBlank())
			{
				continue;
			}
			String label = child.getDescription() == null || child.getDescription().isBlank() ? name : child.getDescription();
			byName.putIfAbsent(name, new ProfileAttributeChoiceDTO(name, label));
		}
		List<ProfileAttributeChoiceDTO> sorted = new ArrayList<>(byName.values());
		sorted.sort(Comparator
			.comparingInt((ProfileAttributeChoiceDTO choice) -> {
				ProfileAttributeChoices known = ProfileAttributeChoices.fromName(choice.value());
				return known == null ? Integer.MAX_VALUE : known.ordinal();
			})
			.thenComparing(ProfileAttributeChoiceDTO::label, String.CASE_INSENSITIVE_ORDER));
		return sorted;
	}

	/**
	 * Builds a detached-prepped involved party carrying only {@code profileId} as its id. The FSDM
	 * link capabilities ({@code findLink((J) this, …)}) filter by the primary's id, so a prepped party
	 * is sufficient to read/write the profile's name and classification links without a stateless
	 * find-by-id (which the party service does not expose).
	 */
	@Override
	public IInvolvedParty<?, ?> preppedParty(UUID profileId)
	{
		IInvolvedParty<?, ?> prepped = involvedPartyService.get();
		prepped.setId(profileId);
		return prepped;
	}

	private Uni<IInvolvedParty<?, ?>> resolveOrCreateParty(Mutiny.StatelessSession session, ISystems<?, ?> system, UUID profileId, UUID token)
	{
		Pair<String, String> idTypes = new Pair<>(IdentificationTypeWebClientUUID.toString(), profileId.toString());
		// A failed duplicate insert aborts PostgreSQL's transaction, so check for the existing party first.
		return session.createNativeQuery("SELECT COUNT(*) FROM party.involvedparty WHERE involvedpartyid = :id")
			.setParameter("id", profileId)
			.getSingleResult()
			.chain(count -> ((Number) count).longValue() > 0
				? Uni.createFrom().item(preppedParty(profileId))
				: involvedPartyService.create(session, system, profileId, idTypes, true, token));
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private Uni<Void> persistNames(Mutiny.StatelessSession session, IInvolvedParty<?, ?> party, ComprehensiveProfileDTO profile, ISystems<?, ?> system, UUID token)
	{
		Uni<Void> chain = persistNames(session, party, NoClassification.classificationValue(), profile.toNameValues(), system, token);
		for (ProfileNameRealms realm : ProfileNameRealms.values())
		{
			Map<NameTypes, String> names = profile.toRealmNameValues(realm);
			if (!names.isEmpty())
			{
				chain = chain.chain(() -> persistNames(session, party, realm.name(), names, system, token));
			}
		}
		return chain;
	}

	/** Replaces each changed name of one realm; the classification separates personal, social and work names. */
	@SuppressWarnings({"rawtypes", "unchecked"})
	private Uni<Void> persistNames(Mutiny.StatelessSession session, IInvolvedParty<?, ?> party, String classification,
	                               Map<NameTypes, String> names, ISystems<?, ?> system, UUID token)
	{
		Uni<Void> chain = Uni.createFrom().voidItem();
		for (Map.Entry<NameTypes, String> entry : names.entrySet())
		{
			final NameTypes nameType = entry.getKey();
			final String value = entry.getValue();
			chain = chain.chain(() -> party.findInvolvedPartyNameTypesAll(session,
					classification, nameType.toString(), null, system, false, token)
				.chain(existing -> {
					Uni<Void> retire = Uni.createFrom().voidItem();
					for (IRelationshipValue<?, IInvolvedPartyNameType<?, ?>, ?> link : existing)
					{
						retire = retire.chain(() -> link.archive(session, system, token).replaceWithVoid());
					}
					ProfileNameRealms realm = ProfileNameRealms.fromRealm(classification);
					String protectedType = ProfileProtectedFields.nameType(realm == null ? "Personal" : realm.realm(), nameType);
					Uni<Void> archiveNames = retire;
					return replaceProtectedValue(session, party, protectedType, value, system, token).chain(() -> archiveNames);
				}));
		}
		return chain;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private Uni<Void> persistAttributes(Mutiny.StatelessSession session, IInvolvedParty<?, ?> party, ComprehensiveProfileDTO profile, ISystems<?, ?> system, UUID token)
	{
		return party.findClassificationValues(session, system, token)
			.chain(values -> {
				Uni<Void> updates = Uni.createFrom().voidItem();
					for (Map.Entry<String, String> entry : profile.toAttributeValues().entrySet())
					{
						final String classificationName = entry.getKey();
						final String value = entry.getValue();
					String previous = values.get(classificationName);
                    if (ProfileLanguageCatalog.isLanguage(classificationName)) {
                        updates = updates.chain(() -> attributeChoices(session, ProfileAttributes.valueOf(classificationName), system, token)
                            .chain(published -> languages.replace(session, party, classificationName, value, published, system, token)));
                        if (previous != null) updates = updates.chain(() -> retireProfileAttribute(session, party.getId(), classificationName, system, token));
                        continue;
                    }
					if (classificationName.equals(ProfileAttributes.Biography.name())) {
						updates = updates.chain(() -> replaceProtectedValue(session, party,
							ProfileProtectedFields.attributeType(classificationName), "", system, token));
						if (!java.util.Objects.equals(previous, value)) {
							if (previous != null)
								updates = updates.chain(() -> retireProfileAttribute(session, party.getId(), classificationName, system, token));
							if (!value.isEmpty())
								updates = updates.chain(() -> party.addClassification(session, classificationName, value, system, token));
						}
						continue;
					}
					try { ProfileAttributes.valueOf(classificationName); }
					catch (IllegalArgumentException custom) {
						updates = updates.chain(() -> involvedPartyService.createIdentificationType(session, system,
							ProfileProtectedFields.attributeType(classificationName), "Protected profile attribute", token).replaceWithVoid());
					}
					updates = updates.chain(() -> replaceProtectedValue(session, party,
						ProfileProtectedFields.attributeType(classificationName), value, system, token));
					if (previous != null)
					{
						updates = updates.chain(() -> retireProfileAttribute(session, party.getId(), classificationName, system, token));
					}
				}
				return updates;
			});
	}

	private Uni<Void> retireProfileAttribute(Mutiny.StatelessSession session, UUID partyId, String name, ISystems<?, ?> system, UUID token)
	{
		return classifications.find(session, name, EnterpriseClassificationDataConcepts.NoClassificationDataConceptName, system, token)
			.chain(classification -> activeFlags.getArchivedFlag(session, system.getEnterprise(), token)
				.chain(archived -> {
					var now = com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSCD
						.convertToUTCDateTime(com.entityassist.RootEntity.getNow());
					return session.createNativeQuery("""
							UPDATE party.involvedpartyxclassification
							SET activeflagid = :archived, effectivetodate = :now
							WHERE involvedpartyid = :party AND enterpriseid = :enterprise
							  AND classificationid = :classification
							  AND effectivefromdate <= :now AND effectivetodate > :now
							  AND activeflagid IN (
							    SELECT activeflagid FROM dbo.activeflag
							    WHERE enterpriseid = :enterprise AND activeflagname = 'Active'
							  )
							""")
						.setParameter("archived", archived.getId())
						.setParameter("now", now)
						.setParameter("party", partyId)
						.setParameter("enterprise", system.getEnterprise().getId())
						.setParameter("classification", classification.getId())
						.executeUpdate().replaceWithVoid();
				}));
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
		for (ProfileNameRealms realm : ProfileNameRealms.values())
		{
			for (NameTypes nameType : realm.nameTypes())
			{
				chain = chain.chain(() -> ((Uni<IRelationshipValue<?, IInvolvedPartyNameType<?, ?>, ?>>) (Uni<?>) raw.findInvolvedPartyNameType(session,
						realm.name(), nameType.toString(), null, system, true, true, token))
					.map(relationship -> relationship == null ? null : relationship.getValue())
					.onFailure().recoverWithItem(() -> null)
					.invoke(value -> {
						if (value != null)
						{
							dto.setRealmName(realm, nameType, value);
						}
					})
					.replaceWithVoid());
			}
		}
		return chain;
	}

	private Uni<Void> retireLegacySelectedChoices(Mutiny.StatelessSession session, IInvolvedParty<?, ?> party,
	                                         ComprehensiveProfileDTO profile, ISystems<?, ?> system, UUID token)
	{
		Map<String, String> supplied = profile.toAttributeValues();
		Uni<Void> updates = Uni.createFrom().voidItem();
		for (ProfileChoiceConcepts concept : ProfileChoiceConcepts.values())
		{
			String value = supplied.get(concept.attribute().name());
			if (value == null) continue;
			updates = updates.chain(() -> concept.linkKind() == ProfileChoiceConcepts.LinkKind.NAME
				? retireLegacyNameChoice(session, party, concept.linkTypeName(), system, token)
				: retireLegacyPartyChoice(session, party, concept.linkTypeName(), system, token));
		}
		return updates;
	}

	private Uni<Void> retireLegacyPartyChoice(Mutiny.StatelessSession session, IInvolvedParty<?, ?> party,
	                                         String typeName, ISystems<?, ?> system, UUID token)
	{
		return party.findInvolvedPartyTypesByType(session, NoClassification.classificationValue(), typeName, system, token)
			.chain(existing -> {
				Uni<Void> retire = Uni.createFrom().voidItem();
				for (var link : existing)
					retire = retire.chain(() -> link.archive(session, system, token).replaceWithVoid());
				return retire;
			});
	}

	private Uni<Void> retireLegacyNameChoice(Mutiny.StatelessSession session, IInvolvedParty<?, ?> party,
	                                        String typeName, ISystems<?, ?> system, UUID token)
	{
		return party.findInvolvedPartyNameTypesAll(session, NoClassification.classificationValue(), typeName, null, system, false, token)
			.chain(existing -> {
				Uni<Void> retire = Uni.createFrom().voidItem();
				for (var link : existing)
					retire = retire.chain(() -> link.archive(session, system, token).replaceWithVoid());
				return retire;
			});
	}

	private Uni<Void> hydrateSelectedChoices(Mutiny.StatelessSession session, IInvolvedParty<?, ?> party,
	                                         ComprehensiveProfileDTO dto, ISystems<?, ?> system, UUID token)
	{
		Uni<Void> reads = Uni.createFrom().voidItem();
		for (ProfileChoiceConcepts concept : ProfileChoiceConcepts.values())
		{
			reads = reads.chain(() -> (concept.linkKind() == ProfileChoiceConcepts.LinkKind.NAME
				? party.findInvolvedPartyNameTypesAll(session, NoClassification.classificationValue(), concept.linkTypeName(), null, system, false, token)
					.map(links -> links.isEmpty() ? null : links.getLast().getValue())
				: party.findInvolvedPartyTypesByType(session, NoClassification.classificationValue(), concept.linkTypeName(), system, token)
					.map(links -> links.isEmpty() ? null : links.getLast().getValue()))
				.invoke(value -> { if (value != null) dto.applyAttributeValues(Map.of(concept.attribute().name(), value)); })
				.replaceWithVoid());
		}
		return reads;
	}

    private Uni<Void> persistAddresses(Mutiny.StatelessSession session, IInvolvedParty<?, ?> party,
                                       ComprehensiveProfileDTO profile, ISystems<?, ?> system, UUID token) {
        if (profile.getAddresses() == null) return Uni.createFrom().voidItem();
        if (profile.getAddresses().size() > 20) return Uni.createFrom().failure(new IllegalArgumentException("At most twenty addresses are supported"));
        var ids = new java.util.HashSet<UUID>();
        for (var address : profile.getAddresses())
            if (address.id() != null && !ids.add(address.id()))
                return Uni.createFrom().failure(new IllegalArgumentException("Duplicate address reference"));
        return addresses.findPartyAddresses(session, party, system, token).chain(existing -> {
            Uni<Void> chain = Uni.createFrom().voidItem();
            for (var address : profile.getAddresses())
                chain = chain.chain(() -> addresses.savePartyAddress(session, party, address, system, token).replaceWithVoid());
            for (var address : existing)
                if (!ids.contains(address.id())) chain = chain.chain(() -> addresses.endPartyAddress(session, party, address.id(), system, token));
            // Retire obsolete flattened attributes when the caller explicitly supplies structured addresses.
            for (var name : List.of("ResidentialAddress", "PostalAddress", "City", "Province", "PostalCode", "Country")) {
                chain = chain.chain(() -> replaceProtectedValue(session, party, ProfileProtectedFields.attributeType(name), "", system, token))
                        .chain(() -> retireProfileAttribute(session, party.getId(), name, system, token));
            }
            return chain;
        });
    }

	private Uni<Void> replaceProtectedValue(Mutiny.StatelessSession session, IInvolvedParty<?, ?> party,
	                                        String type, String value, ISystems<?, ?> system, UUID token)
	{
		return party.findInvolvedPartyIdentificationTypesByType(session, NoClassification.classificationValue(), type, system, token)
			.chain(existing -> {
				if (existing.size() == 1 && value.equals(existing.getFirst().getValue())) return Uni.createFrom().voidItem();
				Uni<Void> retire = Uni.createFrom().voidItem();
				for (var link : existing)
					retire = retire.chain(() -> link.archive(session, system, token).replaceWithVoid());
				return value.isEmpty() ? retire : retire.chain(() -> party.addProtectedInvolvedPartyIdentificationType(
					session, NoClassification.classificationValue(), type, value, system, token).replaceWithVoid());
			});
	}

	private Uni<Void> hydrateProtectedValues(Mutiny.StatelessSession session, IInvolvedParty<?, ?> party,
	                                         ComprehensiveProfileDTO dto, ISystems<?, ?> system, UUID token)
	{
		return party.findInvolvedPartyIdentificationTypes(session, NoClassification.classificationValue(), system, token)
			.chain(links -> {
				Uni<Void> reads = Uni.createFrom().voidItem();
				for (var link : links)
					reads = reads.chain(() -> session.fetch(link.getSecondary())
						.invoke(type -> applyProtectedValue(dto, type.getName(), link.getValue())).replaceWithVoid());
				return reads;
			});
	}

	private void applyProtectedValue(ComprehensiveProfileDTO dto, String type, String value)
	{
		String attributePrefix = ProfileProtectedFields.attributeType("");
		if (type.startsWith(attributePrefix)) {
			String attribute = type.substring(attributePrefix.length());
			dto.applyAttributeValues(Map.of(attribute, value));
			try { ProfileAttributes.valueOf(attribute); }
			catch (IllegalArgumentException custom) {
				if (dto.getAdditionalAttributes() == null) dto.setAdditionalAttributes(new LinkedHashMap<>());
				dto.getAdditionalAttributes().put(attribute, value);
			}
			return;
		}
		for (NameTypes name : NameTypes.values())
			if (ProfileProtectedFields.nameType("Personal", name).equals(type)) { dto.applyName(name, value); return; }
		for (ProfileNameRealms realm : ProfileNameRealms.values())
			for (NameTypes name : realm.nameTypes())
				if (ProfileProtectedFields.nameType(realm.realm(), name).equals(type)) { dto.setRealmName(realm, name, value); return; }
	}
}
