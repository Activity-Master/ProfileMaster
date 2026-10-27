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
import java.util.HashMap;
import java.util.LinkedHashSet;
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
									.onFailure(failureToRecover -> !com.guicedee.activitymaster.fsdm.client.services.SessionUtils.isSerializationFailure(failureToRecover)).recoverWithItem(() -> null)
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
					.onFailure(failureToRecover -> !com.guicedee.activitymaster.fsdm.client.services.SessionUtils.isSerializationFailure(failureToRecover)).recoverWithItem(() -> {
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
				.onFailure(failureToRecover -> !com.guicedee.activitymaster.fsdm.client.services.SessionUtils.isSerializationFailure(failureToRecover)).recoverWithItem(() -> {
					Set<String> guest = new TreeSet<>();
					guest.add("Guest");
					return guest;
				});
	}

	// ---- Comprehensive profile storage ----

	// ---- Comprehensive profile storage (stateless twins) ----

	@Override
    public Uni<UUID> saveProfile(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, ComprehensiveProfileDTO profile) {
        return saveProfile(session, enterprise, profile, false);
    }

    @Override
    public Uni<UUID> saveProfileWithGroupedGrants(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, ComprehensiveProfileDTO profile) {
        return saveProfile(session, enterprise, profile, true);
    }

    private Uni<UUID> saveProfile(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, ComprehensiveProfileDTO profile, boolean grouped)
    {
        for (var field : List.of("ResidentialAddress", "PostalAddress", "City", "Province", "PostalCode", "Country")) {
            String value = profile.toAttributeValues().get(field);
            if (value != null && !value.isEmpty())
                return Uni.createFrom().failure(new IllegalArgumentException("Use structured addresses with separate components and geography references"));
        }
		boolean suppliedValues = profile.toNameValues().values().stream().anyMatch(value -> !value.isEmpty())
			|| profile.toAttributeValues().entrySet().stream().anyMatch(entry -> !entry.getKey().equals(ProfileAttributes.Biography.name()) && !entry.getValue().isEmpty())
			|| (profile.getPhones() != null && !profile.getPhones().isEmpty())
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
					.chain(party -> persistNames(session, party, profile, system, token, grouped)
						.chain(() -> persistAttributes(session, party, profile, system, token, grouped))
						.chain(() -> persistAddresses(session, party, profile, system, token, grouped))
                        .chain(() -> persistPhones(session, party, profile, system, token, grouped))
						.chain(() -> retireLegacySelectedChoices(session, party, profile, system, token))
						.replaceWith(profileId))))
			.onFailure().invoke(error -> log.error("Error saving profile {} (stateless): {}", profileId, error.getMessage(), error));
	}

    @Override
    public Uni<Boolean> replacePersonalName(Mutiny.StatelessSession session,IEnterprise<?, ?> enterprise,UUID profileId,
                                             NameTypes nameType,String expected,String value) {
        return replacePersonalName(session, enterprise, profileId, nameType, expected, value, false);
    }

    @Override
    public Uni<Boolean> replacePersonalNameWithGroupedGrants(Mutiny.StatelessSession session,IEnterprise<?, ?> enterprise,UUID profileId,
                                             NameTypes nameType,String expected,String value) {
        return replacePersonalName(session, enterprise, profileId, nameType, expected, value, true);
    }

    private Uni<Boolean> replacePersonalName(Mutiny.StatelessSession session,IEnterprise<?, ?> enterprise,UUID profileId,
                                             NameTypes nameType,String expected,String value,boolean grouped) {
        java.util.Objects.requireNonNull(value);
        return getPersonalName(session,enterprise,profileId,nameType).chain(current -> {
            if(java.util.Objects.equals(java.util.Objects.requireNonNullElse(current,""),value))return Uni.createFrom().item(false);
            String encryptionMode = com.guicedee.client.Environment.getProperty("activitymaster.encryption.mode", "legacy");
            if (!value.isEmpty() && !Set.of("aes-gcm", "enterprise").contains(encryptionMode))
                return Uni.createFrom().failure(new IllegalStateException("Authenticated encryption must be configured before storing personal profile values"));
            if(!java.util.Objects.equals(java.util.Objects.requireNonNullElse(current,""),java.util.Objects.requireNonNullElse(expected,"")))
                return Uni.createFrom().failure(new java.util.ConcurrentModificationException("Profile entry changed"));
            return getISystem(session,ProfileSystemName,enterprise).chain(system ->
                    getISystemToken(session,ProfileSystemName,enterprise).chain(token ->
                            persistNames(session,preppedParty(profileId),NoClassification.classificationValue(),Map.of(nameType,value),system,token,grouped)))
                    .replaceWith(true);
        });
    }

    private record ProtectedAttributeState(String value, boolean hasLegacy, boolean hasLegacyChoice) { }
    private record LegacyChoiceState(String value, boolean present) { }

    private Uni<LegacyChoiceState> legacyChoiceState(Mutiny.StatelessSession session, IInvolvedParty<?, ?> party,
            ProfileChoiceConcepts concept, ISystems<?, ?> system, UUID token) {
        if (concept == null) return Uni.createFrom().item(new LegacyChoiceState(null,false));
        return legacyPresence(session,party,system).chain(presence -> {
            boolean exists=concept.linkKind()==ProfileChoiceConcepts.LinkKind.NAME ? presence.names() : presence.types();
            if (!exists) return Uni.createFrom().item(new LegacyChoiceState(null,false));
            return (concept.linkKind()==ProfileChoiceConcepts.LinkKind.NAME
                    ? party.findInvolvedPartyNameTypesAll(session,NoClassification.classificationValue(),concept.linkTypeName(),null,system,false,token)
                    : party.findInvolvedPartyTypesByType(session,NoClassification.classificationValue(),concept.linkTypeName(),system,token))
                    .map(links -> new LegacyChoiceState(links.isEmpty()?null:links.getLast().getValue(),!links.isEmpty()));
        });
    }

    private Uni<ProtectedAttributeState> protectedAttributeState(Mutiny.StatelessSession session, IInvolvedParty<?, ?> party,
            String attribute, ISystems<?, ?> system, UUID token) {
        String type=ProfileProtectedFields.attributeType(attribute);
        var concept=ProfileChoiceConcepts.forAttributeName(attribute);
        return party.findClassificationValues(session,system,token).chain(legacy ->
                legacyChoiceState(session,party,concept,system,token).chain(choice ->
                party.findInvolvedPartyIdentificationTypesByType(session,NoClassification.classificationValue(),type,system,token)
                        .onFailure(jakarta.persistence.NoResultException.class).recoverWithItem(java.util.List.of())
                        .map(links -> {
                            if (links.size()>1) throw new IllegalStateException("Multiple active protected profile values");
                            String prior=choice.value()!=null?choice.value():legacy.get(attribute);
                            return new ProtectedAttributeState(links.isEmpty()?prior:links.getFirst().getValue(),legacy.containsKey(attribute),choice.present());
                        })));
    }

    @Override
    public Uni<String> getProtectedAttribute(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, UUID profileId, String attribute) {
        java.util.Objects.requireNonNull(profileId);
        if (!IProfileService.supportsIndependentProtectedAttribute(attribute))
            return Uni.createFrom().failure(new IllegalArgumentException("Unsupported independent profile attribute"));
        return getISystem(session,ProfileSystemName,enterprise).chain(system ->
                getISystemToken(session,ProfileSystemName,enterprise).chain(token ->
                        protectedAttributeState(session,preppedParty(profileId),attribute,system,token).map(ProtectedAttributeState::value)));
    }

    @Override
    public Uni<Boolean> replaceProtectedAttribute(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, UUID profileId,
            String attribute, String expected, String value) {
        return replaceProtectedAttribute(session, enterprise, profileId, attribute, expected, value, false);
    }

    @Override
    public Uni<Boolean> replaceProtectedAttributeWithGroupedGrants(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, UUID profileId,
            String attribute, String expected, String value) {
        return replaceProtectedAttribute(session, enterprise, profileId, attribute, expected, value, true);
    }

    private Uni<Boolean> replaceProtectedAttribute(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, UUID profileId,
            String attribute, String expected, String value, boolean grouped) {
        java.util.Objects.requireNonNull(profileId); java.util.Objects.requireNonNull(value);
        if (!IProfileService.supportsIndependentProtectedAttribute(attribute))
            return Uni.createFrom().failure(new IllegalArgumentException("Unsupported independent profile attribute"));
        String type = ProfileProtectedFields.attributeType(attribute);
        return getISystem(session,ProfileSystemName,enterprise).chain(system ->
                getISystemToken(session,ProfileSystemName,enterprise).chain(token -> {
                    var party = preppedParty(profileId);
                    return protectedAttributeState(session,party,attribute,system,token).chain(state -> {
                                    String current = state.value();
                                    if (java.util.Objects.equals(java.util.Objects.requireNonNullElse(current,""),value)) return Uni.createFrom().item(false);
                                    if (!java.util.Objects.equals(java.util.Objects.requireNonNullElse(current,""),java.util.Objects.requireNonNullElse(expected,"")))
                                        return Uni.createFrom().failure(new java.util.ConcurrentModificationException("Profile entry changed"));
                                    String mode = com.guicedee.client.Environment.getProperty("activitymaster.encryption.mode", "legacy");
                                    if (!value.isEmpty() && !Set.of("aes-gcm", "enterprise").contains(mode))
                                        return Uni.createFrom().failure(new IllegalStateException("Authenticated encryption must be configured before storing personal profile values"));
                                    Uni<Void> prepare = Uni.createFrom().voidItem();
                                    try { ProfileAttributes.valueOf(attribute); }
                                    catch (IllegalArgumentException custom) {
                                        prepare = profileTypeIdentity(session,system,type).chain(id -> involvedPartyService.createIdentificationTypeWithId(
                                                session,system,type,"Protected profile attribute",id,token)).replaceWithVoid();
                                    }
                                    Uni<Void> prepared = prepare;
                                    return resolveOrCreateParty(session,system,profileId,token)
                                            .chain(resolved -> prepared.chain(() -> replaceProtectedValue(session,resolved,type,value,system,token,grouped))
                                                    .chain(() -> state.hasLegacy() ? retireProfileAttribute(session,profileId,attribute,system,token)
                                                            : Uni.createFrom().voidItem())
                                                    .chain(() -> {
                                                        var concept=ProfileChoiceConcepts.forAttributeName(attribute);
                                                        if (!state.hasLegacyChoice() || concept==null) return Uni.createFrom().voidItem();
                                                        return concept.linkKind()==ProfileChoiceConcepts.LinkKind.NAME
                                                                ? retireLegacyNameChoice(session,resolved,concept.linkTypeName(),system,token)
                                                                : retireLegacyPartyChoice(session,resolved,concept.linkTypeName(),system,token);
                                                    }))
                                            .replaceWith(true);
                                });
                }));
    }

    private static String canonicalLanguageValue(String attribute, String value) {
        return String.join(",",ProfileLanguageCatalog.parse(attribute,java.util.Objects.requireNonNullElse(value,"")).stream().sorted().toList());
    }

    @Override
    public Uni<String> getProfileLanguage(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, UUID profileId, String attribute) {
        java.util.Objects.requireNonNull(profileId);
        if (!ProfileLanguageCatalog.isLanguage(attribute)) return Uni.createFrom().failure(new IllegalArgumentException("Unsupported profile language"));
        return getISystem(session,ProfileSystemName,enterprise).chain(system ->
                getISystemToken(session,ProfileSystemName,enterprise).chain(token -> {
                    var party=preppedParty(profileId);
                    return protectedAttributeState(session,party,attribute,system,token).chain(state ->
                            languages.read(session,party,attribute,system,token).map(selected -> selected==null?state.value():selected));
                }));
    }

    @Override
    public Uni<Boolean> replaceProfileLanguage(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, UUID profileId,
            String attribute, String expected, String value) {
        java.util.Objects.requireNonNull(value);
        if (!ProfileLanguageCatalog.isLanguage(attribute)) return Uni.createFrom().failure(new IllegalArgumentException("Unsupported profile language"));
        final String canonical;
        try { canonical=canonicalLanguageValue(attribute,value); }
        catch (IllegalArgumentException invalid) { return Uni.createFrom().failure(invalid); }
        return getProfileLanguage(session,enterprise,profileId,attribute).chain(current -> {
            if (java.util.Objects.equals(java.util.Objects.requireNonNullElse(current,""),canonical)) return Uni.createFrom().item(false);
            if (!java.util.Objects.equals(java.util.Objects.requireNonNullElse(current,""),java.util.Objects.requireNonNullElse(expected,"")))
                return Uni.createFrom().failure(new java.util.ConcurrentModificationException("Profile entry changed"));
            String mode=com.guicedee.client.Environment.getProperty("activitymaster.encryption.mode","legacy");
            if (!canonical.isEmpty() && !Set.of("aes-gcm","enterprise").contains(mode))
                return Uni.createFrom().failure(new IllegalStateException("Authenticated encryption must be configured before storing personal profile values"));
            return getISystem(session,ProfileSystemName,enterprise).chain(system ->
                    getISystemToken(session,ProfileSystemName,enterprise).chain(token -> {
                        var party=preppedParty(profileId);
                        return protectedAttributeState(session,party,attribute,system,token).chain(state ->
                                attributeChoices(session,ProfileAttributes.valueOf(attribute),system,token,canonical.isEmpty()?null:Set.copyOf(ProfileLanguageCatalog.parse(attribute,canonical))).chain(published ->
                                    resolveOrCreateParty(session,system,profileId,token).chain(resolved ->
                                            languages.replace(session,resolved,attribute,canonical,published,system,token)
                                                .chain(() -> state.hasLegacy()?retireProfileAttribute(session,profileId,attribute,system,token):Uni.createFrom().voidItem())
                                                .chain(() -> {
                                                    var concept=ProfileChoiceConcepts.forAttributeName(attribute);
                                                    if (!state.hasLegacyChoice() || concept==null) return Uni.createFrom().voidItem();
                                                    return concept.linkKind()==ProfileChoiceConcepts.LinkKind.NAME
                                                            ? retireLegacyNameChoice(session,resolved,concept.linkTypeName(),system,token)
                                                            : retireLegacyPartyChoice(session,resolved,concept.linkTypeName(),system,token);
                                                })))).replaceWith(true);
                    }));
        });
    }

    private String nameClassification(String realm, NameTypes nameType) {
        java.util.Objects.requireNonNull(nameType);
        if ("Personal".equals(realm)) return NoClassification.classificationValue();
        var selected=ProfileNameRealms.fromRealm(realm);
        if (selected==null || !selected.nameTypes().contains(nameType)) throw new IllegalArgumentException("Unsupported profile name realm or type");
        return selected.name();
    }

    @Override
    public Uni<String> getProfileName(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, UUID profileId,
            String realm, NameTypes nameType) {
        java.util.Objects.requireNonNull(profileId);
        String classification=nameClassification(realm,nameType);
        String canonicalRealm="Personal".equals(realm)?"Personal":ProfileNameRealms.fromRealm(realm).realm();
        return getISystem(session,ProfileSystemName,enterprise).chain(system ->
                getISystemToken(session,ProfileSystemName,enterprise).chain(token -> {
                    var party=preppedParty(profileId);
                    return party.findInvolvedPartyIdentificationTypesByType(session,NoClassification.classificationValue(),
                            ProfileProtectedFields.nameType(canonicalRealm,nameType),system,token)
                            .onFailure(jakarta.persistence.NoResultException.class).recoverWithItem(java.util.List.of())
                            .chain(links -> {
                                if (links.size()>1) return Uni.createFrom().failure(new IllegalStateException("Multiple active protected profile values"));
                                if (!links.isEmpty()) return Uni.createFrom().item(links.getFirst().getValue());
                                return party.findInvolvedPartyNameType(session,classification,nameType.toString(),null,system,true,true,token)
                                        .map(link -> link==null?null:link.getValue())
                                        .onFailure(jakarta.persistence.NoResultException.class).recoverWithNull();
                            });
                }));
    }

    @Override
    public Uni<Boolean> replaceProfileName(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, UUID profileId,
            String realm, NameTypes nameType, String expected, String value) {
        return replaceProfileName(session,enterprise,profileId,realm,nameType,expected,value,false);
    }

    @Override
    public Uni<Boolean> replaceProfileNameWithGroupedGrants(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, UUID profileId,
            String realm, NameTypes nameType, String expected, String value) {
        return replaceProfileName(session,enterprise,profileId,realm,nameType,expected,value,true);
    }

    private Uni<Boolean> replaceProfileName(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, UUID profileId,
            String realm, NameTypes nameType, String expected, String value, boolean grouped) {
        java.util.Objects.requireNonNull(value);
        String classification=nameClassification(realm,nameType);
        return getProfileName(session,enterprise,profileId,realm,nameType).chain(current -> {
            if (java.util.Objects.equals(java.util.Objects.requireNonNullElse(current,""),value)) return Uni.createFrom().item(false);
            if (!java.util.Objects.equals(java.util.Objects.requireNonNullElse(current,""),java.util.Objects.requireNonNullElse(expected,"")))
                return Uni.createFrom().failure(new java.util.ConcurrentModificationException("Profile entry changed"));
            String mode=com.guicedee.client.Environment.getProperty("activitymaster.encryption.mode","legacy");
            if (!value.isEmpty() && !Set.of("aes-gcm","enterprise").contains(mode))
                return Uni.createFrom().failure(new IllegalStateException("Authenticated encryption must be configured before storing personal profile values"));
            return getISystem(session,ProfileSystemName,enterprise).chain(system ->
                    getISystemToken(session,ProfileSystemName,enterprise).chain(token ->
                            persistNames(session,preppedParty(profileId),classification,Map.of(nameType,value),system,token,grouped)))
                    .replaceWith(true);
        });
    }

    @Override
    public Uni<List<com.guicedee.activitymaster.fsdm.client.services.dto.PartyPhoneDTO>> replacePhoneCollection(
            Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, UUID profileId,
            List<com.guicedee.activitymaster.fsdm.client.services.dto.PartyPhoneDTO> phones) {
        java.util.Objects.requireNonNull(profileId); java.util.Objects.requireNonNull(phones);
        String mode=com.guicedee.client.Environment.getProperty("activitymaster.encryption.mode","legacy");
        if (!phones.isEmpty() && !Set.of("aes-gcm","enterprise").contains(mode))
            return Uni.createFrom().failure(new IllegalStateException("Authenticated encryption must be configured before storing personal profile values"));
        var dto=new ComprehensiveProfileDTO(); dto.setPhones(phones);
        return getISystem(session,ProfileSystemName,enterprise).chain(system ->
                getISystemToken(session,ProfileSystemName,enterprise).chain(token ->
                        resolveOrCreateParty(session,system,profileId,token).chain(party ->
                                persistPhones(session,party,dto,system,token,false)
                                        .chain(() -> addresses.findPartyPhones(session,party,system,token)))));
    }

    @Override
    public Uni<List<com.guicedee.activitymaster.fsdm.client.services.dto.PartyAddressDTO>> replaceAddressCollection(
            Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, UUID profileId,
            List<com.guicedee.activitymaster.fsdm.client.services.dto.PartyAddressDTO> values) {
        java.util.Objects.requireNonNull(profileId); java.util.Objects.requireNonNull(values);
        String mode=com.guicedee.client.Environment.getProperty("activitymaster.encryption.mode","legacy");
        if (values.stream().anyMatch(address -> !address.identifiers().isEmpty()) && !Set.of("aes-gcm","enterprise").contains(mode))
            return Uni.createFrom().failure(new IllegalStateException("Authenticated encryption must be configured before storing personal profile values"));
        var dto=new ComprehensiveProfileDTO(); dto.setAddresses(values);
        return getISystem(session,ProfileSystemName,enterprise).chain(system ->
                getISystemToken(session,ProfileSystemName,enterprise).chain(token ->
                        resolveOrCreateParty(session,system,profileId,token).chain(party ->
                                persistAddresses(session,party,dto,system,token,false)
                                        .chain(() -> addresses.findPartyAddresses(session,party,system,token)))));
    }

    @Override
    public Uni<String> getPersonalName(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise, UUID profileId, NameTypes nameType) {
        java.util.Objects.requireNonNull(profileId); java.util.Objects.requireNonNull(nameType);
        return getISystem(session, ProfileSystemName, enterprise).chain(system ->
                getISystemToken(session, ProfileSystemName, enterprise).chain(token -> {
                    var party=preppedParty(profileId);
                    return party.findInvolvedPartyIdentificationTypesByType(session,NoClassification.classificationValue(),
                            ProfileProtectedFields.nameType("Personal",nameType),system,token)
                            .chain(links -> {
                                if(links.size()>1)return Uni.createFrom().failure(new IllegalStateException("Multiple active protected profile values"));
                                if(!links.isEmpty())return Uni.createFrom().item(links.getFirst().getValue());
                                return party.findInvolvedPartyNameType(session,NoClassification.classificationValue(),nameType.toString(),null,system,true,true,token)
                                        .map(link -> link==null?null:link.getValue())
                                        .onFailure(jakarta.persistence.NoResultException.class).recoverWithNull();
                            });
                }));
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
                        .chain(ignored -> legacyPresence(session,party,system).chain(presence ->
                                (presence.names() ? hydrateNames(session,party,dto,system,token) : Uni.createFrom().voidItem())
                                    .chain(() -> hydrateSelectedChoices(session,party,dto,system,token,presence.names(),presence.types()))))
						.chain(() -> hydrateProtectedValues(session, party, dto, system, token))
                        .chain(() -> languages.read(session, party, "HomeLanguage", system, token)
                            .invoke(value -> { if (value != null) dto.setHomeLanguage(value); }).replaceWithVoid())
                        .chain(() -> languages.read(session, party, "SpokenLanguages", system, token)
                            .invoke(value -> { if (value != null) dto.setSpokenLanguages(value); }).replaceWithVoid())
						.chain(() -> addresses.findPartyAddresses(session, party, system, token).invoke(dto::setAddresses))
                        .chain(() -> addresses.findPartyPhones(session, party, system, token).invoke(dto::setPhones))
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

    @Override
    public Uni<List<ProfileAttributeChoiceDTO>> getAttributeChoicesFor(Mutiny.StatelessSession session,IEnterprise<?, ?> enterprise,String attribute) {
        var concept=ProfileChoiceConcepts.forAttributeName(attribute);
        if(concept==null)return Uni.createFrom().failure(new IllegalArgumentException("Unknown Profile choice concept"));
        return getISystem(session,ProfileSystemName,enterprise)
                .chain(system -> getISystemToken(session,ProfileSystemName,enterprise)
                        .chain(token -> attributeChoices(session,concept.attribute(),system,token)));
    }
    @Override
    public Uni<List<ProfileAttributeChoiceDTO>> getAttributeChoicesForValues(Mutiny.StatelessSession session,IEnterprise<?, ?> enterprise,String attribute,Set<String> values) {
        var concept=ProfileChoiceConcepts.forAttributeName(attribute);
        if(concept==null)return Uni.createFrom().failure(new IllegalArgumentException("Unknown Profile choice concept"));
        var selected=Set.copyOf(values);
        return getISystem(session,ProfileSystemName,enterprise).chain(system ->
                getISystemToken(session,ProfileSystemName,enterprise).chain(token -> attributeChoices(session,concept.attribute(),system,token,selected)));
    }

    private Uni<List<ProfileAttributeChoiceDTO>> attributeChoices(Mutiny.StatelessSession session, ProfileAttributes attribute, ISystems<?, ?> system, UUID token) {
        return attributeChoices(session,attribute,system,token,null);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
	private Uni<List<ProfileAttributeChoiceDTO>> attributeChoices(Mutiny.StatelessSession session, ProfileAttributes attribute, ISystems<?, ?> system, UUID token,Set<String> selected)
	{
		String conceptName = ProfileChoiceConcepts.forAttribute(attribute).conceptName();
        if(selected!=null && selected.isEmpty())return Uni.createFrom().item(List.of());
		return classifications.find(session, attribute.name(), system, token)
            .chain(parent -> {
                if(selected==null) return ((IClassification) parent).findChildren(session, NoClassification.name(), null, system, token);
                return new com.guicedee.activitymaster.fsdm.db.entities.classifications.ClassificationXClassification().builder(session)
                        .findLink((com.guicedee.activitymaster.fsdm.db.entities.classifications.Classification)parent,null,null)
                        .inActiveRange().withClassification(NoClassification.name(),system).inDateRange().canRead(system,token)
                        .withEnterprise(system.getEnterprise()).where("childClassificationID.name",com.entityassist.enumerations.Operand.InList,selected).getAll();
            })
            .chain(links -> {
                if (((List<?>) links).isEmpty()) return Uni.createFrom().item(List.of());
                // The hierarchy query above owns link authorization. Batch only the IDs it returned.
                var ids=new LinkedHashSet<UUID>();
                for (Object link : (List<Object>) links) ids.add(((IClassification<?, ?>)((IRelationshipValue)link).getSecondary()).getId());
                return session.createQuery("select c.id, c.name, c.description from Classification c join c.concept where c.id in :ids",Object[].class)
                        .setParameter("ids",ids).getResultList().chain(childRows -> {
                            if (childRows.size()!=ids.size()) return Uni.createFrom().failure(new IllegalStateException("Profile choice classification unavailable"));
                            var childrenById=new LinkedHashMap<UUID,Object[]>(); var names=new LinkedHashSet<String>();
                            for (var row:childRows) {childrenById.put((UUID)row[0],row);names.add((String)row[1]);}
                            com.guicedee.activitymaster.fsdm.client.services.IClassificationDataConceptService<?> concepts=com.guicedee.client.IGuiceContext.get(com.guicedee.activitymaster.fsdm.client.services.IClassificationDataConceptService.class);
                            return concepts.find(session,conceptName,system,token)
                                    .onFailure(failure -> !com.guicedee.activitymaster.fsdm.client.services.SessionUtils.isSerializationFailure(failure)).recoverWithNull()
                                    .chain(concept -> {
                                        if (concept==null) return Uni.createFrom().item(List.<ProfileAttributeChoiceDTO>of());
                                        var query=new com.guicedee.activitymaster.fsdm.db.entities.classifications.Classification().builder(session)
                                                .withEnterprise(system.getEnterprise())
                                                .where("name",com.entityassist.enumerations.Operand.InList,names)
                                                .where("concept",com.entityassist.enumerations.Operand.Equals,concept)
                                                .inActiveRange().inDateRange()
                                                .selectColumn(com.guicedee.activitymaster.fsdm.db.entities.classifications.Classification_.id)
                                                .selectColumn(com.guicedee.activitymaster.fsdm.db.entities.classifications.Classification_.name)
                                                .selectColumn(com.guicedee.activitymaster.fsdm.db.entities.classifications.Classification_.description)
                                                .selectColumn(com.guicedee.activitymaster.fsdm.db.entities.classifications.Classification_.classificationSequenceNumber);
                                        return query.getAll(Object[].class).chain(rows -> {
                                            var byName=new HashMap<String,List<Object[]>>();
                                            for (var row:rows) byName.computeIfAbsent((String)row[1],ignored -> new ArrayList<>()).add(row);
                                            List<IClassification<?, ?>> accepted=new ArrayList<>();
                                            Uni<Void> duplicates=Uni.createFrom().voidItem();
                                            for (var child:childrenById.entrySet()) {
                                                String name=(String)child.getValue()[1]; var matches=byName.getOrDefault(name,List.of());
                                                if (matches.size()==1 && child.getKey().equals(matches.getFirst()[0])) {
                                                    var match=matches.getFirst();
                                                    accepted.add(new com.guicedee.activitymaster.fsdm.db.entities.classifications.Classification(
                                                            (UUID)match[0],(String)match[1],(String)match[2],((Number)match[3]).intValue()));
                                                } else if (matches.size()>1) {
                                                    // Preserve the canonical resolver's first-row behavior for duplicate names.
                                                    duplicates=duplicates.chain(() -> classifications.findInConcept(session,name,conceptName,system,token)
                                                            .invoke(scoped -> { if (child.getKey().equals(scoped.getId())) accepted.add(scoped); })
                                                            .onFailure(failure -> !com.guicedee.activitymaster.fsdm.client.services.SessionUtils.isSerializationFailure(failure)).recoverWithNull().replaceWithVoid());
                                                }
                                            }
                                            return duplicates.replaceWith(() -> sortedChoices(accepted));
                                        });
                                    });
                        });
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
	private Uni<Void> persistNames(Mutiny.StatelessSession session, IInvolvedParty<?, ?> party, ComprehensiveProfileDTO profile, ISystems<?, ?> system, UUID token, boolean grouped)
	{
		Uni<Void> chain = persistNames(session, party, NoClassification.classificationValue(), profile.toNameValues(), system, token, grouped);
		for (ProfileNameRealms realm : ProfileNameRealms.values())
		{
			Map<NameTypes, String> names = profile.toRealmNameValues(realm);
			if (!names.isEmpty())
			{
				chain = chain.chain(() -> persistNames(session, party, realm.name(), names, system, token, grouped));
			}
		}
		return chain;
	}

	/** Replaces each changed name of one realm; the classification separates personal, social and work names. */
	@SuppressWarnings({"rawtypes", "unchecked"})
	private Uni<Void> persistNames(Mutiny.StatelessSession session, IInvolvedParty<?, ?> party, String classification,
	                               Map<NameTypes, String> names, ISystems<?, ?> system, UUID token, boolean grouped)
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
					return replaceProtectedValue(session, party, protectedType, value, system, token, grouped).chain(() -> archiveNames);
				}));
		}
		return chain;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private Uni<Void> persistAttributes(Mutiny.StatelessSession session, IInvolvedParty<?, ?> party, ComprehensiveProfileDTO profile, ISystems<?, ?> system, UUID token, boolean grouped)
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
							ProfileProtectedFields.attributeType(classificationName), "", system, token, grouped));
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
                        updates = updates.chain(() -> {
                            String name=ProfileProtectedFields.attributeType(classificationName);
                            return profileTypeIdentity(session,system,name).chain(id -> involvedPartyService.createIdentificationTypeWithId(
                                    session,system,name,"Protected profile attribute",id,token)).replaceWithVoid();
                        });
					}
					updates = updates.chain(() -> replaceProtectedValue(session, party,
						ProfileProtectedFields.attributeType(classificationName), value, system, token, grouped));
					if (previous != null)
					{
						updates = updates.chain(() -> retireProfileAttribute(session, party.getId(), classificationName, system, token));
					}
				}
				return updates;
			});
	}

    private Uni<UUID> profileTypeIdentity(Mutiny.StatelessSession session,ISystems<?, ?> system,String name) {
        return session.createNativeQuery("select involvedpartyidentificationtypeid from party.involvedpartyidentificationtype where enterpriseid=:enterprise and involvedpartyidentificationname=:name order by warehousecreatedtimestamp desc, involvedpartyidentificationtypeid desc limit 1",UUID.class)
                .setParameter("enterprise",system.getEnterprise().getId()).setParameter("name",name).getResultList()
                .map(previous -> {
                    try {
                        var bytes=new java.io.ByteArrayOutputStream();
                        try(var out=new java.io.DataOutputStream(bytes)) {
                            out.writeUTF("ActivityMasterProfileAttributeType-v1");out.writeUTF(system.getEnterprise().getId().toString());
                            out.writeUTF(new UUID(0,0).toString());out.writeUTF(new UUID(0,0).toString());
                            out.writeUTF("taxonomy-identification:"+name);out.writeUTF((previous.isEmpty()?new UUID(0,0):previous.getFirst()).toString());
                        }
                        var hash=java.security.MessageDigest.getInstance("SHA-256").digest(bytes.toByteArray());
                        hash[6]=(byte)((hash[6]&15)|0x80);hash[8]=(byte)((hash[8]&63)|0x80);
                        var buffer=java.nio.ByteBuffer.wrap(hash);return new UUID(buffer.getLong(),buffer.getLong());
                    } catch(java.io.IOException|java.security.NoSuchAlgorithmException impossible) {throw new IllegalStateException(impossible);}
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

    private record LegacyPresence(boolean names, boolean types) { }
    /** A negative existence test cannot expose or skip any canonical readable legacy relationship. */
    private Uni<LegacyPresence> legacyPresence(Mutiny.StatelessSession session,IInvolvedParty<?, ?> party,ISystems<?, ?> system) {
        return session.createNativeQuery("""
                select exists(select 1 from party.involvedpartyxinvolvedpartynametype n
                              where n.involvedpartyid=:party and n.enterpriseid=:enterprise),
                       exists(select 1 from party.involvedpartyxinvolvedpartytype t
                              where t.involvedpartyid=:party and t.enterpriseid=:enterprise)
                """,Object[].class).setParameter("party",party.getId()).setParameter("enterprise",system.getEnterprise().getId())
                .getSingleResult().map(row -> new LegacyPresence(Boolean.TRUE.equals(row[0]),Boolean.TRUE.equals(row[1])));
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
				.onFailure(failureToRecover -> !com.guicedee.activitymaster.fsdm.client.services.SessionUtils.isSerializationFailure(failureToRecover)).recoverWithItem(() -> null)
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
					.onFailure(failureToRecover -> !com.guicedee.activitymaster.fsdm.client.services.SessionUtils.isSerializationFailure(failureToRecover)).recoverWithItem(() -> null)
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
	                                         ComprehensiveProfileDTO dto, ISystems<?, ?> system, UUID token, boolean names, boolean types)
	{
		Uni<Void> reads = Uni.createFrom().voidItem();
		for (ProfileChoiceConcepts concept : ProfileChoiceConcepts.values())
		{
            if (concept.linkKind() == ProfileChoiceConcepts.LinkKind.NAME ? !names : !types) continue;
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

    private Uni<Void> persistPhones(Mutiny.StatelessSession session, IInvolvedParty<?, ?> party,
                                    ComprehensiveProfileDTO profile, ISystems<?, ?> system, UUID token, boolean grouped) {
        if (profile.getPhones() == null) return Uni.createFrom().voidItem();
        if (profile.getPhones().size() > 20) return Uni.createFrom().failure(new IllegalArgumentException("At most twenty phones are supported"));
        var ids = new java.util.HashSet<UUID>();
        for (var phone : profile.getPhones())
            if (phone.id() != null && !ids.add(phone.id()))
                return Uni.createFrom().failure(new IllegalArgumentException("Duplicate telephone reference"));
        return addresses.findPartyPhones(session, party, system, token).chain(existing -> {
            Uni<Void> chain = Uni.createFrom().voidItem();
            for (var phone : profile.getPhones())
                chain = chain.chain(() -> addresses.savePartyPhone(session, party, phone, system, token).replaceWithVoid());
            for (var phone : existing)
                if (!ids.contains(phone.id())) chain = chain.chain(() -> addresses.endPartyPhone(session, party, phone.id(), system, token));
            for (var name : List.of("MobileNumber", "HomeNumber", "WorkNumber", "FaxNumber"))
                chain = chain.chain(() -> replaceProtectedValue(session, party, ProfileProtectedFields.attributeType(name), "", system, token, grouped))
                    .chain(() -> retireProfileAttribute(session, party.getId(), name, system, token));
            return chain;
        });
    }

    private Uni<Void> persistAddresses(Mutiny.StatelessSession session, IInvolvedParty<?, ?> party,
                                       ComprehensiveProfileDTO profile, ISystems<?, ?> system, UUID token, boolean grouped) {
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
                chain = chain.chain(() -> replaceProtectedValue(session, party, ProfileProtectedFields.attributeType(name), "", system, token, grouped))
                        .chain(() -> retireProfileAttribute(session, party.getId(), name, system, token));
            }
            return chain;
        });
    }

	private Uni<Void> replaceProtectedValue(Mutiny.StatelessSession session, IInvolvedParty<?, ?> party,
	                                        String type, String value, ISystems<?, ?> system, UUID token, boolean grouped)
	{
		return party.findInvolvedPartyIdentificationTypesByType(session, NoClassification.classificationValue(), type, system, token)
			.chain(existing -> {
				if (existing.size() == 1 && value.equals(existing.getFirst().getValue())) return Uni.createFrom().voidItem();
				Uni<Void> retire = Uni.createFrom().voidItem();
				for (var link : existing)
					retire = retire.chain(() -> link.archive(session, system, token).replaceWithVoid());
				return value.isEmpty() ? retire : retire.chain(() -> (grouped
                    ? party.addProtectedInvolvedPartyIdentificationTypeGrouped(session, NoClassification.classificationValue(), type, value, system, token)
                    : party.addProtectedInvolvedPartyIdentificationType(session, NoClassification.classificationValue(), type, value, system, token)).replaceWithVoid());
			});
	}

	private Uni<Void> hydrateProtectedValues(Mutiny.StatelessSession session, IInvolvedParty<?, ?> party,
	                                         ComprehensiveProfileDTO dto, ISystems<?, ?> system, UUID token)
	{
        return party.findInvolvedPartyIdentificationTypes(session, NoClassification.classificationValue(), system, token)
            .chain(links -> {
                if (links.isEmpty()) return Uni.createFrom().voidItem();
                // Links have already passed the canonical enterprise and row-read query. Resolve only
                // the type IDs referenced by those links, in one query instead of one fetch per value.
                var ids = new java.util.LinkedHashSet<UUID>();
                for (var link : links) ids.add(java.util.Objects.requireNonNull(link.getSecondary()).getId());
                return session.createQuery("select t.id, t.name from InvolvedPartyIdentificationType t where t.id in :ids", Object[].class)
                        .setParameter("ids", ids).getResultList().invoke(rows -> {
                            var names = new java.util.HashMap<UUID, String>();
                            for (var row : rows) names.put((UUID) row[0], (String) row[1]);
                            for (var link : links) {
                                String name = java.util.Objects.requireNonNull(names.get(link.getSecondary().getId()), "Identification type unavailable");
                                applyProtectedValue(dto, name, link.getValue());
                            }
                        }).replaceWithVoid();
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
