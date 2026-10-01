package com.guicedee.activitymaster.profiles.test;

import com.google.inject.Key;
import com.google.inject.name.Names;
import com.guicedee.activitymaster.fsdm.client.services.IEnterpriseService;
import com.guicedee.activitymaster.fsdm.client.services.IClassificationService;
import com.guicedee.activitymaster.fsdm.client.services.SessionUtils;
import com.guicedee.activitymaster.fsdm.client.services.administration.ActivityMasterConfiguration;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.classifications.types.NameTypes;
import com.guicedee.activitymaster.profiles.enumerations.ProfileAttributeChoices;
import com.guicedee.activitymaster.profiles.enumerations.ProfileChoiceConcepts;
import com.guicedee.activitymaster.profiles.enumerations.ProfileLanguageCatalog;
import com.guicedee.activitymaster.profiles.enumerations.ProfileProtectedFields;
import com.guicedee.activitymaster.profiles.ProfileSystem;
import com.guicedee.activitymaster.profiles.enumerations.ProfileNameRealms;
import com.guicedee.activitymaster.profiles.implementations.updates.ProfileAttributeChoicesInstall;
import com.guicedee.activitymaster.profiles.implementations.updates.ProfileMasterInstall;
import com.guicedee.activitymaster.profiles.implementations.updates.ProfileNameRealmsInstall;
import com.guicedee.activitymaster.profiles.implementations.updates.ProfileProtectedValuesInstall;
import com.guicedee.activitymaster.profiles.services.interfaces.IProfileService;
import com.guicedee.activitymaster.profiles.webdto.ComprehensiveProfileDTO;
import com.guicedee.activitymaster.profiles.webdto.ProfileAttributeChoiceDTO;
import com.guicedee.client.IGuiceContext;
import com.guicedee.client.utils.LogUtils;
import io.smallrye.mutiny.Uni;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration test for the comprehensive profile feature.
 *
 * <p>Provisions a throwaway enterprise (which registers the Profile system), installs the profile
 * taxonomy (name types + attribute classifications) via {@link ProfileMasterInstall}, then exercises
 * the {@link IProfileService#saveProfile}/{@link IProfileService#getProfile} round trip — the same
 * service the {@code ProfileRestService} and {@code ProfileRestClients} delegate to.</p>
 */
@Log4j2
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProfileComprehensiveProfileTest
{
	private static final String ENTERPRISE = "ProfileTestCo";
	private static final String PROFILE_SYSTEM = IProfileService.ProfileSystemName;

	private Mutiny.SessionFactory sessionFactory;
	private ProfileEncryptionFixture encryption;

	@BeforeAll
	public void setup()
	{
		encryption = new ProfileEncryptionFixture();
		LogUtils.addConsoleLogger(Level.INFO);
		ActivityMasterConfiguration.get().setApplicationEnterpriseName(ENTERPRISE);
		IGuiceContext.instance();

		sessionFactory = IGuiceContext.get(Key.get(Mutiny.SessionFactory.class, Names.named("ActivityMaster-Test")));
		assertNotNull(sessionFactory, "SessionFactory should not be null");

		IEnterpriseService<?> es = IGuiceContext.get(IEnterpriseService.class);
		sessionFactory.withStatelessSession(session -> session.withTransaction(tx ->
				es.getEnterprise(session, ENTERPRISE)
						.onFailure().recoverWithUni(t -> {
							var ent = es.get();
							ent.setName(ENTERPRISE);
							ent.setDescription("Profile comprehensive test enterprise");
							return es.createNewEnterprise(session, ent)
									.chain(e -> es.startNewEnterprise(session, ENTERPRISE, "admin", "adminadmin!@"));
						})
						.replaceWith(Uni.createFrom().voidItem())
		)).await().atMost(Duration.ofMinutes(3));

		// Install the profile taxonomy (name types + comprehensive attribute classifications).
		ProfileMasterInstall install = IGuiceContext.get(ProfileMasterInstall.class);
		IEnterprise<?, ?> enterprise = sessionFactory.withStatelessSession(s -> es.getEnterprise(s, ENTERPRISE))
				.await().atMost(Duration.ofMinutes(1));
		assertNotNull(enterprise, "Baseline enterprise must be provisioned in setup");

		Boolean installed = sessionFactory.withStatelessSession(s -> s.withTransaction(tx -> install.update(s, enterprise)))
				.await().atMost(Duration.ofMinutes(3));
		assertEquals(Boolean.TRUE, installed, "Profile taxonomy installation should succeed");

		ProfileNameRealmsInstall realms = IGuiceContext.get(ProfileNameRealmsInstall.class);
		Boolean realmsInstalled = sessionFactory.withStatelessSession(s -> s.withTransaction(tx -> realms.update(s, enterprise)))
				.await().atMost(Duration.ofMinutes(3));
		assertEquals(Boolean.TRUE, realmsInstalled, "Profile name realm installation should succeed");

		ProfileAttributeChoicesInstall choices = IGuiceContext.get(ProfileAttributeChoicesInstall.class);
		for (int run = 0; run < 2; run++)
		{
			Boolean choicesInstalled = sessionFactory.withStatelessSession(s -> s.withTransaction(tx -> choices.update(s, enterprise)))
					.await().atMost(Duration.ofMinutes(3));
			assertEquals(Boolean.TRUE, choicesInstalled, "Profile attribute choice installation should succeed and be repeatable");
		}
		assertTrue(sessionFactory.withStatelessSession(s -> s.withTransaction(tx ->
			IGuiceContext.get(ProfileProtectedValuesInstall.class).update(s, enterprise)))
			.await().atMost(Duration.ofMinutes(3)));
	}

	@AfterAll
	void restoreEncryption() { encryption.close(); }

	@Test
	@Order(6)
	@DisplayName("Attribute choices are read from the FSDM classification hierarchy")
	public void attributeChoicesComeFromClassificationChildren()
	{
		IProfileService<?> profileService = IGuiceContext.get(IProfileService.class);
		Map<String, List<ProfileAttributeChoiceDTO>> choices = SessionUtils
				.<Map<String, List<ProfileAttributeChoiceDTO>>>withActivityMasterStateless(ENTERPRISE, PROFILE_SYSTEM, tuple ->
						profileService.getAttributeChoices(tuple.getItem1(), tuple.getItem2()))
				.await().atMost(Duration.ofMinutes(2));
		assertEquals(List.of("Gender", "Pronouns", "MaritalStatus", "Occupation", "Ethnicity", "Religion", "BloodType", "HomeLanguage", "SpokenLanguages"),
			List.copyOf(choices.keySet()));
		for (var attribute : ProfileAttributeChoices.attributes())
		{
			if (ProfileLanguageCatalog.isLanguage(attribute.name())) continue;
			List<String> expected = java.util.Arrays.stream(ProfileAttributeChoices.values())
					.filter(choice -> choice.attribute() == attribute).map(Enum::name).toList();
			assertEquals(expected, choices.get(attribute.name()).stream().map(ProfileAttributeChoiceDTO::value).toList(),
					attribute + " children in declared order, once each after a repeated install");
		}
		assertEquals(new ProfileAttributeChoiceDTO("MaritalStatusPartnered", "Domestic partnership"),
				choices.get("MaritalStatus").get(2));
		assertEquals("Prefer not to say", choices.get("Gender").getLast().label());
		assertEquals(ProfileLanguageCatalog.DEFAULTS, choices.get("HomeLanguage"));
		assertEquals(choices.get("HomeLanguage"), choices.get("SpokenLanguages"));
		assertEquals("Caucasian / White", choices.get("Ethnicity").stream()
			.filter(choice -> choice.value().equals("EthnicityEuropean")).findFirst().orElseThrow().label());
		assertTrue(choices.get("Religion").contains(new ProfileAttributeChoiceDTO("ReligionChineseAncestral", "Ancestral")));

		ProfileSystem profileSystem = IGuiceContext.get(ProfileSystem.class);
		IClassificationService<?> classifications = IGuiceContext.get(IClassificationService.class);
		sessionFactory.withStatelessSession(session -> session.withTransaction(tx ->
			profileSystem.getSystem(session, ENTERPRISE)
				.chain(system -> profileSystem.getSystemToken(session, ENTERPRISE)
						.chain(token -> classifications.find(session, "Occupation", system, token)
							.chain(parent -> classifications.createInConcept(session, "OccupationMathematician",
								"Mathematician", ProfileChoiceConcepts.Occupation.conceptName(), system, 1, parent, token))))))
			.await().atMost(Duration.ofMinutes(2));
		Map<String, List<ProfileAttributeChoiceDTO>> withOccupation = SessionUtils
			.<Map<String, List<ProfileAttributeChoiceDTO>>>withActivityMasterStateless(ENTERPRISE, PROFILE_SYSTEM,
				tuple -> profileService.getAttributeChoices(tuple.getItem1(), tuple.getItem2()))
			.await().atMost(Duration.ofMinutes(2));
		assertEquals(List.of(new ProfileAttributeChoiceDTO("OccupationMathematician", "Mathematician")),
			withOccupation.get("Occupation"));
	}

	@Test
	@Order(7)
	@DisplayName("Selected profile values use encrypted identification links and can be cleared")
	public void selectedChoicesUseTypedLinks()
	{
		IProfileService<?> service = IGuiceContext.get(IProfileService.class);
		ComprehensiveProfileDTO selected = new ComprehensiveProfileDTO();
		selected.setGender("GenderFemale");
		selected.setPronouns("PronounsSheHer");
		selected.setMaritalStatus("MaritalStatusSingle");
		selected.setEthnicity("EthnicityAfrican");
		selected.setReligion("ReligionNone");
		selected.setBloodType("BloodTypeOPositive");
		selected.setOccupation("OccupationMathematician");
		UUID id = SessionUtils.<UUID>withActivityMasterStateless(ENTERPRISE, PROFILE_SYSTEM,
			tuple -> service.saveProfile(tuple.getItem1(), tuple.getItem2(), selected))
			.await().atMost(Duration.ofMinutes(2));
		ComprehensiveProfileDTO stored = SessionUtils.<ComprehensiveProfileDTO>withActivityMasterStateless(
			ENTERPRISE, PROFILE_SYSTEM, tuple -> service.getProfile(tuple.getItem1(), tuple.getItem2(), id))
			.await().atMost(Duration.ofMinutes(2));
		for (var entry : selected.toAttributeValues().entrySet())
			assertEquals(entry.getValue(), stored.toAttributeValues().get(entry.getKey()), entry.getKey());

		SessionUtils.<Void>withActivityMasterStateless(ENTERPRISE, PROFILE_SYSTEM, tuple -> {
			var party = service.preppedParty(id);
			return party.findClassificationValues(tuple.getItem1(), tuple.getItem3(), tuple.getItem4())
				.invoke(values -> {
					for (var concept : ProfileChoiceConcepts.values())
						assertFalse(values.containsKey(concept.attribute().name()), concept.attribute().name());
				})
				.chain(() -> party.findInvolvedPartyIdentificationTypesByType(tuple.getItem1(), "NoClassification",
					ProfileProtectedFields.attributeType("Pronouns"), tuple.getItem3(), tuple.getItem4()))
				.invoke(links -> assertEquals("PronounsSheHer", links.getFirst().getValue()))
				.chain(() -> party.findInvolvedPartyIdentificationTypesByType(tuple.getItem1(), "NoClassification",
					ProfileProtectedFields.attributeType("Gender"), tuple.getItem3(), tuple.getItem4()))
				.invoke(links -> assertEquals("GenderFemale", links.getFirst().getValue()))
				.replaceWithVoid();
		}).await().atMost(Duration.ofMinutes(2));

		ComprehensiveProfileDTO cleared = new ComprehensiveProfileDTO();
		cleared.setProfileId(id);
		for (var concept : ProfileChoiceConcepts.values())
			cleared.applyAttributeValues(Map.of(concept.attribute().name(), ""));
		ComprehensiveProfileDTO afterClear = SessionUtils.<ComprehensiveProfileDTO>withActivityMasterStateless(
			ENTERPRISE, PROFILE_SYSTEM, tuple -> service.saveProfile(tuple.getItem1(), tuple.getItem2(), cleared)
				.chain(saved -> service.getProfile(tuple.getItem1(), tuple.getItem2(), saved)))
			.await().atMost(Duration.ofMinutes(2));
		for (var concept : ProfileChoiceConcepts.values())
			assertNull(afterClear.toAttributeValues().get(concept.attribute().name()), concept.attribute().name());
	}

	@Test
	@Order(8)
	@DisplayName("Medical details and personal names are encrypted in the identification table")
	public void sensitiveValuesAreCiphertextAtRest()
	{
		IProfileService<?> service = IGuiceContext.get(IProfileService.class);
		ComprehensiveProfileDTO supplied = new ComprehensiveProfileDTO();
		supplied.setFirstName("Private first name");
		supplied.setSurname("Private surname");
		supplied.setMedicalAidName("Private medical provider");
		supplied.setMedicalAidNumber("PRIVATE-1234");
		supplied.setDietaryRequirements("Private dietary requirements");
		supplied.setDisabilityStatus("Private medical details");
		supplied.setIdNumber("PRIVATE-ID-4321");
		supplied.setBiography("Public profile biography");
		supplied.setAdditionalAttributes(Map.of("PrivateCustomNote", "Private custom profile value"));
		ComprehensiveProfileDTO stored = SessionUtils.<ComprehensiveProfileDTO>withActivityMasterStateless(
			ENTERPRISE, PROFILE_SYSTEM, tuple -> service.saveProfile(tuple.getItem1(), tuple.getItem2(), supplied)
				.chain(id -> service.getProfile(tuple.getItem1(), tuple.getItem2(), id))
				.chain(dto -> service.preppedParty(dto.getProfileId()).findInvolvedPartyIdentificationType(
					tuple.getItem1(), "NoClassification", "ProfileAttributeMedicalAidNumber",
					supplied.getMedicalAidNumber(), tuple.getItem3(), true, false, tuple.getItem4())
					.invoke(link -> assertEquals(supplied.getMedicalAidNumber(), link.getValue()))
					.replaceWith(dto)))
			.await().atMost(Duration.ofMinutes(2));
		assertEquals(supplied.getFirstName(), stored.getFirstName());
		assertEquals(supplied.getSurname(), stored.getSurname());
		for (var entry : supplied.toAttributeValues().entrySet())
			assertEquals(entry.getValue(), stored.toAttributeValues().get(entry.getKey()), entry.getKey());
		sessionFactory.withStatelessSession(session -> session.createNativeQuery("""
			SELECT link.value
			FROM party.involvedpartyxinvolvedpartyidentificationtype link
			JOIN party.involvedpartyidentificationtype type
			  ON type.involvedpartyidentificationtypeid = link.involvedpartyidentificationtypeid
			WHERE link.involvedpartyid = :party
			  AND (type.involvedpartyidentificationname LIKE 'ProfileAttribute%'
			       OR type.involvedpartyidentificationname LIKE 'ProfileName%')
			""").setParameter("party", stored.getProfileId()).getResultList()
			.invoke(values -> {
				assertEquals(supplied.toAttributeValues().size() - 1 + supplied.toNameValues().size(), values.size());
				for (Object value : values) {
					assertTrue(value.toString().startsWith("amenc:1:"));
					assertFalse(value.toString().contains("Private"));
				}
			}).chain(values -> session.createNativeQuery("""
				SELECT COUNT(*)
				FROM party.involvedpartyxinvolvedpartyidentificationtypesecuritytoken security
				JOIN party.involvedpartyxinvolvedpartyidentificationtype link
				  ON link.involvedpartyxinvolvedpartyidentificationtypeid = security.involvedpartyxinvolvedpartyidentificationtypeid
				JOIN party.involvedpartyidentificationtype type
				  ON type.involvedpartyidentificationtypeid = link.involvedpartyidentificationtypeid
				WHERE link.involvedpartyid = :party
				  AND (type.involvedpartyidentificationname LIKE 'ProfileAttribute%'
				       OR type.involvedpartyidentificationname LIKE 'ProfileName%')
				""").setParameter("party", stored.getProfileId()).getSingleResult()
				.invoke(count -> assertEquals(4L * (supplied.toAttributeValues().size() - 1 + supplied.toNameValues().size()),
					((Number) count).longValue(), "Protected links receive only the four restricted grants"))))
			.await().atMost(Duration.ofMinutes(2));
	}

	@Test
	@Order(9)
	void personalProfileWritesRejectLegacyObfuscation()
	{
		String previous = System.getProperty("activitymaster.encryption.mode");
		try {
			System.setProperty("activitymaster.encryption.mode", "legacy");
			ComprehensiveProfileDTO value = new ComprehensiveProfileDTO();
			value.setMedicalAidNumber("PRIVATE-1234");
			assertThrows(IllegalStateException.class, () -> IGuiceContext.get(IProfileService.class)
				.saveProfile(null, null, value).await().atMost(Duration.ofSeconds(5)));
		} finally {
			System.setProperty("activitymaster.encryption.mode", previous);
		}
	}

    @Test
    @Order(10)
    void addressesReuseComponentsProtectIdentifiersAndClearIndependently() {
        IProfileService<?> service = IGuiceContext.get(IProfileService.class);
        var supplied = new ComprehensiveProfileDTO();
        supplied.setAddresses(List.of(
            new com.guicedee.activitymaster.fsdm.client.services.dto.PartyAddressDTO(null, "Residential",
                Map.of("StreetName", "Shared Street", "StreetType", "Road"), Map.of(), Map.of("BuildingNumber", "12", "Unit", "4")),
            new com.guicedee.activitymaster.fsdm.client.services.dto.PartyAddressDTO(null, "Residential",
                Map.of("StreetName", "Shared Street", "StreetType", "Road"), Map.of(), Map.of("BuildingNumber", "24")),
            new com.guicedee.activitymaster.fsdm.client.services.dto.PartyAddressDTO(null, "Postal",
                Map.of("BoxKind", "PO Box"), Map.of(), Map.of("BoxNumber", "987", "PostalCode", "1234"))));
        var stored = SessionUtils.<ComprehensiveProfileDTO>withActivityMasterStateless(ENTERPRISE, PROFILE_SYSTEM, tuple ->
            service.saveProfile(tuple.getItem1(), tuple.getItem2(), supplied)
                .chain(id -> service.getProfile(tuple.getItem1(), tuple.getItem2(), id)))
            .await().atMost(Duration.ofMinutes(2));
        assertEquals(3, stored.getAddresses().size());
        var first = stored.getAddresses().stream().filter(address -> "12".equals(address.identifiers().get("BuildingNumber"))).findFirst().orElseThrow();
        var second = stored.getAddresses().stream().filter(address -> "24".equals(address.identifiers().get("BuildingNumber"))).findFirst().orElseThrow();
        var postal = stored.getAddresses().stream().filter(address -> address.purpose().equals("Postal")).findFirst().orElseThrow();
        assertNotEquals(first.id(), second.id());
        assertNull(stored.getResidentialAddress(), "No formatted street address is stored on the profile");
        SessionUtils.<Void>withActivityMasterStateless(ENTERPRISE, PROFILE_SYSTEM, tuple -> {
            var session = tuple.getItem1();
            return session.createNativeQuery("""
                SELECT COUNT(DISTINCT ac.componentaddressid), COUNT(*) FROM address.addressxaddress ac
                JOIN party.involvedpartyxaddress pa ON pa.addressid = ac.addressid
                WHERE pa.involvedpartyid = :party AND ac.value = 'StreetName'
                """, Object[].class).setParameter("party", stored.getProfileId()).getSingleResult()
                .invoke(count -> { assertEquals(1L, ((Number) count[0]).longValue()); assertEquals(2L, ((Number) count[1]).longValue()); })
                .chain(() -> session.createNativeQuery("""
                    SELECT a.value FROM address.address a JOIN party.involvedpartyxaddress pa ON pa.addressid = a.addressid
                    WHERE pa.involvedpartyid = :party
                    """, String.class).setParameter("party", stored.getProfileId()).getResultList())
                .invoke(values -> assertTrue(values.stream().allMatch(String::isEmpty)))
                .chain(() -> session.createNativeQuery("""
                    SELECT value FROM party.involvedpartyxinvolvedpartyidentificationtype
                    WHERE involvedpartyid = :party AND addressid IS NOT NULL
                    """, String.class).setParameter("party", stored.getProfileId()).getResultList())
                .invoke(values -> { assertEquals(5, values.size()); assertTrue(values.stream().allMatch(value -> value.startsWith("amenc:1:"))); })
                .chain(() -> session.createNativeQuery("""
                    SELECT COUNT(*) FROM party.involvedpartyxinvolvedpartyidentificationtype identification
                    JOIN address.addresstype type ON type.addresstypeid = identification.addresstypeid
                    WHERE identification.involvedpartyid = :party AND type.addresstypename = 'StreetNumber'
                    """).setParameter("party", stored.getProfileId()).getSingleResult())
                .invoke(count -> assertEquals(2L, ((Number) count).longValue(), "Street number is an address type with protected values"))
                .chain(() -> session.createNativeQuery("""
                    SELECT component.value FROM address.addressxaddress link
                    JOIN address.address component ON component.addressid = link.componentaddressid
                    JOIN address.addresstype type ON type.addresstypeid = component.addresstypeid
                    JOIN party.involvedpartyxaddress party ON party.addressid = link.addressid
                    WHERE party.involvedpartyid = :party AND type.addresstypename = 'Street'
                    """, String.class).setParameter("party", stored.getProfileId()).getResultList())
                .invoke(values -> assertEquals(List.of("Shared Street", "Shared Street"), values))
                .replaceWithVoid();
        }).await().atMost(Duration.ofMinutes(2));
        var changed = new ComprehensiveProfileDTO();
        changed.setProfileId(stored.getProfileId());
        changed.setAddresses(List.of(
            new com.guicedee.activitymaster.fsdm.client.services.dto.PartyAddressDTO(first.id(), first.purpose(), first.components(), first.geographies(), Map.of("BuildingNumber", "12")),
            second));
        var reread = SessionUtils.<ComprehensiveProfileDTO>withActivityMasterStateless(ENTERPRISE, PROFILE_SYSTEM, tuple ->
            service.saveProfile(tuple.getItem1(), tuple.getItem2(), changed)
                .chain(id -> service.getProfile(tuple.getItem1(), tuple.getItem2(), id)))
            .await().atMost(Duration.ofMinutes(2));
        assertEquals(2, reread.getAddresses().size(), "Removing the postal entry ends only its relationships");
        assertTrue(reread.getAddresses().stream().noneMatch(address -> address.id().equals(postal.id())));
        assertFalse(reread.getAddresses().stream().filter(address -> address.id().equals(first.id())).findFirst().orElseThrow().identifiers().containsKey("Unit"));
        assertEquals(second, reread.getAddresses().stream().filter(address -> address.id().equals(second.id())).findFirst().orElseThrow());
        SessionUtils.<Void>withActivityMasterStateless(ENTERPRISE, PROFILE_SYSTEM, tuple -> tuple.getItem1().createNativeQuery("""
            SELECT COUNT(*) FROM party.involvedpartyxinvolvedpartyidentificationtype i
            JOIN dbo.activeflag f ON f.activeflagid = i.activeflagid
            WHERE i.involvedpartyid = :party AND i.addressid = :address AND f.activeflagname = 'Archived'
            """).setParameter("party", stored.getProfileId()).setParameter("address", postal.id()).getSingleResult()
            .invoke(count -> assertEquals(2L, ((Number) count).longValue())).replaceWithVoid())
            .await().atMost(Duration.ofMinutes(2));
        var sparse = new ComprehensiveProfileDTO(); sparse.setProfileId(stored.getProfileId()); sparse.setBiography("Public biography");
        var sparseRead = SessionUtils.<ComprehensiveProfileDTO>withActivityMasterStateless(ENTERPRISE, PROFILE_SYSTEM, tuple ->
            service.saveProfile(tuple.getItem1(), tuple.getItem2(), sparse).chain(id -> service.getProfile(tuple.getItem1(), tuple.getItem2(), id)))
            .await().atMost(Duration.ofMinutes(2));
        assertEquals(2, sparseRead.getAddresses().size(), "Omitting addresses preserves them");
    }

    @Test
    @Order(11)
    void flatAddressWritesAreRejectedRatherThanPersisted() {
        var supplied = new ComprehensiveProfileDTO(); supplied.setResidentialAddress("12 Shared Street Road");
        assertThrows(IllegalArgumentException.class, () -> IGuiceContext.get(IProfileService.class)
            .saveProfile(null, null, supplied).await().atMost(Duration.ofSeconds(5)));
    }

    @Test
    @Order(12)
    void addressGeographyIsReusedAndForeignAddressReferencesAreDenied() {
        IProfileService<?> service = IGuiceContext.get(IProfileService.class);
        UUID country = SessionUtils.<UUID>withActivityMasterStateless(ENTERPRISE, PROFILE_SYSTEM, tuple -> {
            IClassificationService<?> classifications = IGuiceContext.get(IClassificationService.class);
            com.guicedee.activitymaster.fsdm.client.services.IClassificationDataConceptService<?> concepts = IGuiceContext.get(com.guicedee.activitymaster.fsdm.client.services.IClassificationDataConceptService.class);
            com.guicedee.activitymaster.fsdm.client.services.IActiveFlagService<?> flags = IGuiceContext.get(com.guicedee.activitymaster.fsdm.client.services.IActiveFlagService.class);
            com.guicedee.activitymaster.fsdm.client.services.ISecurityTokenService<?> geographySecurity = IGuiceContext.get(com.guicedee.activitymaster.fsdm.client.services.ISecurityTokenService.class);
            var geo = new com.guicedee.activitymaster.fsdm.db.entities.geography.Geography();
            geo.setName("ZZ"); geo.setDescription("Reference Country");
            geo.setEnterpriseID(tuple.getItem2()); geo.setSystemID(tuple.getItem3()); geo.setOriginalSourceSystemID(tuple.getItem3().getId());
            return concepts.createNamedDataConcept(tuple.getItem1(), "AddressTestGeographyKinds", "Test geography", tuple.getItem3(), tuple.getItem4())
                .chain(() -> classifications.createInConcept(tuple.getItem1(), "Country", "Country", "AddressTestGeographyKinds", tuple.getItem3(), null, null, tuple.getItem4()))
                .chain(kind -> { geo.setClassificationID(kind); return flags.getActiveFlag(tuple.getItem1(), tuple.getItem2(), tuple.getItem4()); })
                .chain(flag -> { geo.setActiveFlagID(flag); return tuple.getItem1().insert(geo); })
                .chain(() -> geographySecurity.resolveDefaultGroupFolderTokens(tuple.getItem1(), tuple.getItem3(), tuple.getItem4())
                    .chain(grants -> geo.createDefaultSecurity(tuple.getItem1(), tuple.getItem3(), tuple.getItem2(), geo.getActiveFlagID(), grants, tuple.getItem4())))
                .replaceWith(geo.getId());
        }).await().atMost(Duration.ofMinutes(2));
        var profile = new ComprehensiveProfileDTO();
        profile.setAddresses(List.of(
            new com.guicedee.activitymaster.fsdm.client.services.dto.PartyAddressDTO(null, "Residential", Map.of("StreetName", "Reuse Street"), Map.of("Country", country), Map.of("BuildingNumber", "1")),
            new com.guicedee.activitymaster.fsdm.client.services.dto.PartyAddressDTO(null, "Billing", Map.of("BoxKind", "PO Box"), Map.of("Country", country), Map.of("BoxNumber", "2"))));
        var stored = SessionUtils.<ComprehensiveProfileDTO>withActivityMasterStateless(ENTERPRISE, PROFILE_SYSTEM, tuple ->
            service.saveProfile(tuple.getItem1(), tuple.getItem2(), profile).chain(id -> service.getProfile(tuple.getItem1(), tuple.getItem2(), id)))
            .await().atMost(Duration.ofMinutes(2));
        assertTrue(stored.getAddresses().stream().anyMatch(address -> "Billing".equals(address.purpose())));
        assertTrue(stored.getAddresses().stream().allMatch(address -> country.equals(address.geographies().get("Country"))));
        assertTrue(stored.getAddresses().stream().allMatch(address -> "Reference Country".equals(address.geographyLabels().get("Country"))));
        SessionUtils.<Void>withActivityMasterStateless(ENTERPRISE, PROFILE_SYSTEM, tuple -> tuple.getItem1().createNativeQuery("""
            SELECT COUNT(DISTINCT ag.geographyid), COUNT(*) FROM address.addressxgeography ag
            JOIN party.involvedpartyxaddress pa ON pa.addressid = ag.addressid
            WHERE pa.involvedpartyid = :party
            """, Object[].class).setParameter("party", stored.getProfileId()).getSingleResult()
            .invoke(count -> { assertEquals(1L, ((Number) count[0]).longValue()); assertEquals(2L, ((Number) count[1]).longValue()); }).replaceWithVoid())
            .await().atMost(Duration.ofMinutes(2));
        var another = new ComprehensiveProfileDTO(); another.setBiography("Another party");
        UUID anotherId = SessionUtils.<UUID>withActivityMasterStateless(ENTERPRISE, PROFILE_SYSTEM, tuple ->
            service.saveProfile(tuple.getItem1(), tuple.getItem2(), another)).await().atMost(Duration.ofMinutes(2));
        var stolen = new ComprehensiveProfileDTO(); stolen.setProfileId(anotherId); stolen.setAddresses(stored.getAddresses());
        assertThrows(SecurityException.class, () -> SessionUtils.<UUID>withActivityMasterStateless(ENTERPRISE, PROFILE_SYSTEM, tuple ->
            service.saveProfile(tuple.getItem1(), tuple.getItem2(), stolen)).await().atMost(Duration.ofMinutes(2)));
        var wrongLevel = new ComprehensiveProfileDTO(); wrongLevel.setProfileId(stored.getProfileId());
        wrongLevel.setAddresses(List.of(new com.guicedee.activitymaster.fsdm.client.services.dto.PartyAddressDTO(stored.getAddresses().getFirst().id(), "Residential",
            Map.of(), Map.of("Country", country, "Province", country), Map.of())));
        assertThrows(IllegalArgumentException.class, () -> SessionUtils.<UUID>withActivityMasterStateless(ENTERPRISE, PROFILE_SYSTEM, tuple ->
            service.saveProfile(tuple.getItem1(), tuple.getItem2(), wrongLevel)).await().atMost(Duration.ofMinutes(2)));
        var preserved = SessionUtils.<ComprehensiveProfileDTO>withActivityMasterStateless(ENTERPRISE, PROFILE_SYSTEM, tuple ->
            service.getProfile(tuple.getItem1(), tuple.getItem2(), stored.getProfileId())).await().atMost(Duration.ofMinutes(2));
        assertEquals(stored.getAddresses().size(), preserved.getAddresses().size(), "Failed updates roll back");
        assertTrue(preserved.getAddresses().stream().allMatch(address -> country.equals(address.geographies().get("Country"))));
    }

	@Test
	@Order(5)
	@DisplayName("Social and work names are classified separately from personal names")
	public void realmNamesAreSeparateFromPersonalNames()
	{
		IProfileService<?> profileService = IGuiceContext.get(IProfileService.class);
		ComprehensiveProfileDTO initial = new ComprehensiveProfileDTO();
		initial.setFirstName("Grace");
		initial.setPreferredName("Grace");
		initial.setSuffix("PhD");
		initial.setRealmName(ProfileNameRealms.ProfileSocialName, NameTypes.PreferredNameType, "Amazing Grace");
		initial.setRealmName(ProfileNameRealms.ProfileWorkName, NameTypes.PreferredNameType, "Admiral Hopper");
		initial.setRealmName(ProfileNameRealms.ProfileWorkName, NameTypes.SuffixType, "USN");

		ComprehensiveProfileDTO stored = SessionUtils.<ComprehensiveProfileDTO>withActivityMasterStateless(ENTERPRISE, PROFILE_SYSTEM, tuple ->
				profileService.saveProfile(tuple.getItem1(), tuple.getItem2(), initial)
						.chain(id -> profileService.getProfile(tuple.getItem1(), tuple.getItem2(), id)))
				.await().atMost(Duration.ofMinutes(2));
		assertEquals("Grace", stored.getPreferredName());
		assertEquals("PhD", stored.getSuffix());
		assertEquals("Amazing Grace", stored.getRealmName(ProfileNameRealms.ProfileSocialName, NameTypes.PreferredNameType));
		assertEquals("Admiral Hopper", stored.getRealmName(ProfileNameRealms.ProfileWorkName, NameTypes.PreferredNameType));
		assertEquals("USN", stored.getRealmName(ProfileNameRealms.ProfileWorkName, NameTypes.SuffixType));
		assertNull(stored.getRealmName(ProfileNameRealms.ProfileSocialName, NameTypes.SuffixType));

		ComprehensiveProfileDTO changed = new ComprehensiveProfileDTO();
		changed.setProfileId(stored.getProfileId());
		changed.setRealmName(ProfileNameRealms.ProfileWorkName, NameTypes.PreferredNameType, "Rear Admiral Hopper");
		ComprehensiveProfileDTO reread = SessionUtils.<ComprehensiveProfileDTO>withActivityMasterStateless(ENTERPRISE, PROFILE_SYSTEM, tuple ->
				profileService.saveProfile(tuple.getItem1(), tuple.getItem2(), changed)
						.chain(id -> profileService.getProfile(tuple.getItem1(), tuple.getItem2(), id)))
				.await().atMost(Duration.ofMinutes(2));
		assertEquals("Rear Admiral Hopper", reread.getRealmName(ProfileNameRealms.ProfileWorkName, NameTypes.PreferredNameType));
		assertEquals("Amazing Grace", reread.getRealmName(ProfileNameRealms.ProfileSocialName, NameTypes.PreferredNameType));
		assertEquals("USN", reread.getRealmName(ProfileNameRealms.ProfileWorkName, NameTypes.SuffixType));
		assertEquals("Grace", reread.getPreferredName(), "A work name change must not replace the personal name");
	}

	@Test
	@Order(13)
	@DisplayName("Languages are classified encrypted party links with independent sparse updates")
	void languagesArePublishedPartyRelationships() {
		IProfileService<?> service = IGuiceContext.get(IProfileService.class);
		var supplied = new ComprehensiveProfileDTO();
		supplied.setHomeLanguage("ProfileLanguage_en");
		supplied.setSpokenLanguages("ProfileLanguage_fr,ProfileLanguage_en");
		var stored = SessionUtils.<ComprehensiveProfileDTO>withActivityMasterStateless(ENTERPRISE, PROFILE_SYSTEM, tuple ->
			service.saveProfile(tuple.getItem1(), tuple.getItem2(), supplied)
				.chain(id -> service.getProfile(tuple.getItem1(), tuple.getItem2(), id)))
			.await().atMost(Duration.ofMinutes(2));
		assertEquals("ProfileLanguage_en", stored.getHomeLanguage());
		assertEquals("ProfileLanguage_en,ProfileLanguage_fr", stored.getSpokenLanguages());
		sessionFactory.withStatelessSession(session -> session.createNativeQuery("""
			SELECT concept.classificationdataconceptname, classification.classificationname, link.value,
			       (SELECT COUNT(*) FROM party.involvedpartyxinvolvedpartyidentificationtypesecuritytoken security
			        WHERE security.involvedpartyxinvolvedpartyidentificationtypeid = link.involvedpartyxinvolvedpartyidentificationtypeid)
			FROM party.involvedpartyxinvolvedpartyidentificationtype link
			JOIN party.involvedpartyidentificationtype type ON type.involvedpartyidentificationtypeid = link.involvedpartyidentificationtypeid
			JOIN classification.classification classification ON classification.classificationid = link.classificationid
			JOIN classification.classificationdataconcept concept ON concept.classificationdataconceptid = classification.classificationdataconceptid
			WHERE link.involvedpartyid = :party
			  AND type.involvedpartyidentificationname IN ('ProfileAttributeHomeLanguage', 'ProfileAttributeSpokenLanguages')
			""", Object[].class).setParameter("party", stored.getProfileId()).getResultList().invoke(rows -> {
				assertEquals(3, rows.size());
				for (var row : rows) {
					assertEquals("ProfileLanguages", row[0]);
					assertTrue(row[1].toString().startsWith("ProfileLanguage_"));
					assertTrue(row[2].toString().startsWith("amenc:1:"));
					assertEquals(4L, ((Number) row[3]).longValue());
				}
			})).await().atMost(Duration.ofMinutes(2));
		for (String invalid : List.of("English", "ProfileLanguage_unpublished", "ProfileLanguage_en,ProfileLanguage_en", "ProfileLanguage_en,")) {
			var rejected = new ComprehensiveProfileDTO();
			rejected.setProfileId(stored.getProfileId());
			rejected.setSpokenLanguages(invalid);
			assertThrows(IllegalArgumentException.class, () -> SessionUtils.<UUID>withActivityMasterStateless(ENTERPRISE, PROFILE_SYSTEM, tuple ->
				service.saveProfile(tuple.getItem1(), tuple.getItem2(), rejected)).await().atMost(Duration.ofMinutes(2)));
		}
		var reduced = new ComprehensiveProfileDTO();
		reduced.setProfileId(stored.getProfileId());
		reduced.setSpokenLanguages("ProfileLanguage_fr");
		var afterRemoval = SessionUtils.<ComprehensiveProfileDTO>withActivityMasterStateless(ENTERPRISE, PROFILE_SYSTEM, tuple ->
			service.saveProfile(tuple.getItem1(), tuple.getItem2(), reduced)
				.chain(id -> service.getProfile(tuple.getItem1(), tuple.getItem2(), id)))
			.await().atMost(Duration.ofMinutes(2));
		assertEquals("ProfileLanguage_fr", afterRemoval.getSpokenLanguages());
		assertEquals("ProfileLanguage_en", afterRemoval.getHomeLanguage());
		var cleared = new ComprehensiveProfileDTO();
		cleared.setProfileId(stored.getProfileId());
		cleared.setSpokenLanguages("");
		var afterClear = SessionUtils.<ComprehensiveProfileDTO>withActivityMasterStateless(ENTERPRISE, PROFILE_SYSTEM, tuple ->
			service.saveProfile(tuple.getItem1(), tuple.getItem2(), cleared)
				.chain(id -> service.getProfile(tuple.getItem1(), tuple.getItem2(), id)))
			.await().atMost(Duration.ofMinutes(2));
		assertNull(afterClear.getSpokenLanguages());
		assertEquals("ProfileLanguage_en", afterClear.getHomeLanguage());
	}

	@Test
	@Order(14)
	void linkedAddressGeographiesReadDisplayNamesWithoutReplacingTheirIds() {
		var levels = List.of("Country", "Province", "District", "Locality", "PostalArea");
		var kinds = List.of("Country", "Province", "Municipalities", "City", "PostalCode");
		var codes = List.of("ZZ", "ZZ.01", "ZZ.01.DC1", "ZZ.01.JHB", "2000");
		var names = List.of("Reference Country", "Gauteng", "Johannesburg District", "Johannesburg", "Johannesburg");
		Map<String, UUID> references = SessionUtils.<Map<String, UUID>>withActivityMasterStateless(ENTERPRISE, PROFILE_SYSTEM, tuple -> {
			var session = tuple.getItem1();
			var system = tuple.getItem3();
			var token = tuple.getItem4();
			IClassificationService<?> classifications = IGuiceContext.get(IClassificationService.class);
			com.guicedee.activitymaster.fsdm.client.services.IClassificationDataConceptService<?> concepts = IGuiceContext.get(com.guicedee.activitymaster.fsdm.client.services.IClassificationDataConceptService.class);
			com.guicedee.activitymaster.fsdm.client.services.IActiveFlagService<?> flags = IGuiceContext.get(com.guicedee.activitymaster.fsdm.client.services.IActiveFlagService.class);
			com.guicedee.activitymaster.fsdm.client.services.ISecurityTokenService<?> security = IGuiceContext.get(com.guicedee.activitymaster.fsdm.client.services.ISecurityTokenService.class);
			var geographies = new java.util.ArrayList<com.guicedee.activitymaster.fsdm.db.entities.geography.Geography>();
			Uni<Void> inserts = concepts.createNamedDataConcept(session, "AddressLabelTestKinds", "Address display test", system, token).replaceWithVoid();
			for (int position = 0; position < levels.size(); position++) {
				final int index = position;
				inserts = inserts.chain(() -> classifications.createInConcept(session, kinds.get(index), kinds.get(index), "AddressLabelTestKinds", system, null, null, token)
					.chain(kind -> flags.getActiveFlag(session, tuple.getItem2(), token).chain(active -> {
						var geo = new com.guicedee.activitymaster.fsdm.db.entities.geography.Geography();
						geo.setId(UUID.randomUUID()); geo.setName(codes.get(index)); geo.setDescription(names.get(index));
						geo.setEnterpriseID(tuple.getItem2()); geo.setSystemID(system); geo.setOriginalSourceSystemID(system.getId());
						geo.setClassificationID(kind); geo.setActiveFlagID(active);
						geographies.add(geo);
						return session.insert(geo)
							.chain(() -> security.resolveDefaultGroupFolderTokens(session, system, token)
								.chain(grants -> geo.createDefaultSecurity(session, system, tuple.getItem2(), active, grants, token)))
							.chain(() -> {
								if (index == 0) return Uni.createFrom().voidItem();
								var link = new com.guicedee.activitymaster.fsdm.db.entities.geography.GeographyXGeography();
								link.setId(UUID.randomUUID()); link.setEnterpriseID(tuple.getItem2()); link.setSystemID(system);
								link.setOriginalSourceSystemID(system.getId()); link.setActiveFlagID(active); link.setClassificationID(kind);
								link.setParentGeographyID(geographies.get(index - 1)); link.setChildGeographyID(geo);
								link.setValue("");
								return session.insert(link).chain(() -> security.resolveDefaultGroupFolderTokens(session, system, token)
									.chain(grants -> link.createDefaultSecurity(session, system, tuple.getItem2(), active, grants, token))).replaceWithVoid();
							});
					})).replaceWithVoid());
			}
			return inserts.replaceWith(() -> {
				var ids = new java.util.LinkedHashMap<String, UUID>();
				for (int index = 0; index < levels.size(); index++) ids.put(levels.get(index), geographies.get(index).getId());
				return ids;
			});
		}).await().atMost(Duration.ofMinutes(2));
		IProfileService<?> service = IGuiceContext.get(IProfileService.class);
		var supplied = new ComprehensiveProfileDTO();
		supplied.setAddresses(List.of(new com.guicedee.activitymaster.fsdm.client.services.dto.PartyAddressDTO(null, "Residential", Map.of(), references, Map.of())));
		var stored = SessionUtils.<ComprehensiveProfileDTO>withActivityMasterStateless(ENTERPRISE, PROFILE_SYSTEM, tuple ->
			service.saveProfile(tuple.getItem1(), tuple.getItem2(), supplied)
				.chain(id -> service.getProfile(tuple.getItem1(), tuple.getItem2(), id))).await().atMost(Duration.ofMinutes(2));
		var address = stored.getAddresses().getFirst();
		assertEquals(references, address.geographies());
		assertEquals(Map.of("Country", "Reference Country", "Province", "Gauteng", "District", "Johannesburg District",
			"Locality", "Johannesburg", "PostalArea", "2000 — Johannesburg"), address.geographyLabels());
	}

	@Test
	@Order(1)
	@DisplayName("A comprehensive profile saves and reads back across names and attributes")
	public void saveAndReadComprehensiveProfile()
	{
		ComprehensiveProfileDTO profile = new ComprehensiveProfileDTO();
		profile.setTitle("Dr");
		profile.setFirstName("Ada");
		profile.setSurname("Lovelace");
		profile.setOccupation("Mathematician");
		profile.setJobTitle("Analyst");
		profile.setEmployer("Analytical Engines Ltd");
		profile.setPrimaryEmail("ada@example.com");
		profile.setMobileNumber("+27 11 555 0100");
		profile.setNationality("British");
		profile.setDateOfBirth("1815-12-10");
		profile.setHomeLanguage("ProfileLanguage_en");
		
		profile.setLinkedIn("https://linkedin.com/in/ada");

		ComprehensiveProfileDTO stored = SessionUtils.<ComprehensiveProfileDTO>withActivityMaster(ENTERPRISE, PROFILE_SYSTEM, tuple -> {
			Mutiny.StatelessSession session = tuple.getItem1();
			IEnterprise<?, ?> enterprise = tuple.getItem2();
			IProfileService<?> profileService = IGuiceContext.get(IProfileService.class);
			return profileService.saveProfile(session, enterprise, profile)
					.chain(id -> profileService.getProfile(session, enterprise, id));
		}).await().atMost(Duration.ofMinutes(2));

		assertNotNull(stored, "Stored profile must be returned");
		assertNotNull(stored.getProfileId(), "Stored profile must carry its generated id");

		// Comprehensive attributes round-trip via the classification read path.
		assertEquals("Mathematician", stored.getOccupation());
		assertEquals("Analyst", stored.getJobTitle());
		assertEquals("Analytical Engines Ltd", stored.getEmployer());
		assertEquals("ada@example.com", stored.getPrimaryEmail());
		assertEquals("+27 11 555 0100", stored.getMobileNumber());
		assertEquals("British", stored.getNationality());
		assertEquals("1815-12-10", stored.getDateOfBirth());
		assertEquals("ProfileLanguage_en", stored.getHomeLanguage());
		
		assertEquals("https://linkedin.com/in/ada", stored.getLinkedIn());
	}

	@Test
	@Order(2)
	@DisplayName("Updating a profile by id changes supplied fields and preserves the id")
	public void updateProfileById()
	{
		IProfileService<?> profileService = IGuiceContext.get(IProfileService.class);

		// Create first
		ComprehensiveProfileDTO initial = new ComprehensiveProfileDTO();
		initial.setFirstName("Grace");
		initial.setSurname("Hopper");
		initial.setOccupation("Computer Scientist");
		initial.setHomeLanguage("ProfileLanguage_en");

		UUID id = SessionUtils.<UUID>withActivityMaster(ENTERPRISE, PROFILE_SYSTEM, tuple -> {
			Mutiny.StatelessSession session = tuple.getItem1();
			IEnterprise<?, ?> enterprise = tuple.getItem2();
			return profileService.saveProfile(session, enterprise, initial);
		}).await().atMost(Duration.ofMinutes(2));
		assertNotNull(id);

		// Update the same profile id with a new occupation + email
		ComprehensiveProfileDTO update = new ComprehensiveProfileDTO();
		update.setProfileId(id);
		update.setOccupation("Rear Admiral");
		update.setPrimaryEmail("grace@example.com");
		update.setHomeLanguage("ProfileLanguage_fr");

		ComprehensiveProfileDTO reread = SessionUtils.<ComprehensiveProfileDTO>withActivityMaster(ENTERPRISE, PROFILE_SYSTEM, tuple -> {
			Mutiny.StatelessSession session = tuple.getItem1();
			IEnterprise<?, ?> enterprise = tuple.getItem2();
			return profileService.saveProfile(session, enterprise, update)
					.chain(savedId -> profileService.getProfile(session, enterprise, savedId));
		}).await().atMost(Duration.ofMinutes(2));

		assertNotNull(reread);
		assertEquals(id, reread.getProfileId(), "Update must operate on the same profile id");
		assertEquals("Rear Admiral", reread.getOccupation(), "Updated field must be persisted");
		assertEquals("grace@example.com", reread.getPrimaryEmail(), "New field must be persisted");
		assertEquals("ProfileLanguage_fr", reread.getHomeLanguage(), "Another existing attribute must be replaced");
		assertEquals("Grace", reread.getFirstName(), "Sparse update must preserve the name");
		assertEquals("Hopper", reread.getSurname(), "Sparse update must preserve the surname");
	}

	@Test
	@Order(3)
	@DisplayName("Changing a name retires the previous active name and preserves other names")
	public void updateNameById()
	{
		IProfileService<?> profileService = IGuiceContext.get(IProfileService.class);
		ComprehensiveProfileDTO initial = new ComprehensiveProfileDTO();
		initial.setFirstName("Augusta");
		initial.setSurname("Lovelace");
		initial.setPreferredName("Ada");

		UUID id = SessionUtils.<UUID>withActivityMasterStateless(ENTERPRISE, PROFILE_SYSTEM, tuple ->
				profileService.saveProfile(tuple.getItem1(), tuple.getItem2(), initial))
				.await().atMost(Duration.ofMinutes(2));

		ComprehensiveProfileDTO changed = new ComprehensiveProfileDTO();
		changed.setProfileId(id);
		changed.setFirstName("Ada");
		changed.setPreferredName("Countess");
		ComprehensiveProfileDTO reread = SessionUtils.<ComprehensiveProfileDTO>withActivityMasterStateless(ENTERPRISE, PROFILE_SYSTEM, tuple ->
				profileService.saveProfile(tuple.getItem1(), tuple.getItem2(), changed)
						.chain(saved -> profileService.getProfile(tuple.getItem1(), tuple.getItem2(), saved)))
				.await().atMost(Duration.ofMinutes(2));
		assertEquals("Ada", reread.getFirstName());
		assertEquals("Countess", reread.getPreferredName());
		assertEquals("Lovelace", reread.getSurname());

		ComprehensiveProfileDTO again = SessionUtils.<ComprehensiveProfileDTO>withActivityMasterStateless(ENTERPRISE, PROFILE_SYSTEM, tuple ->
				profileService.saveProfile(tuple.getItem1(), tuple.getItem2(), changed)
						.chain(saved -> profileService.getProfile(tuple.getItem1(), tuple.getItem2(), saved)))
				.await().atMost(Duration.ofMinutes(2));
		assertEquals("Ada", again.getFirstName());
		assertEquals("Countess", again.getPreferredName());
	}

	@Test
	@Order(4)
	@DisplayName("A comprehensive profile saves and reads back via a stateless session")
	public void saveAndReadComprehensiveProfileStateless()
	{
		ComprehensiveProfileDTO profile = new ComprehensiveProfileDTO();
		profile.setFirstName("Katherine");
		profile.setSurname("Johnson");
		profile.setOccupation("Mathematician");
		profile.setEmployer("NASA");
		profile.setPrimaryEmail("katherine@example.com");
		profile.setNationality("American");
		profile.setHomeLanguage("ProfileLanguage_en");

		ComprehensiveProfileDTO stored = SessionUtils.<ComprehensiveProfileDTO>withActivityMasterStateless(ENTERPRISE, PROFILE_SYSTEM, tuple -> {
			Mutiny.StatelessSession session = tuple.getItem1();
			IEnterprise<?, ?> enterprise = tuple.getItem2();
			IProfileService<?> profileService = IGuiceContext.get(IProfileService.class);
			return profileService.saveProfile(session, enterprise, profile)
					.chain(id -> profileService.getProfile(session, enterprise, id));
		}).await().atMost(Duration.ofMinutes(2));

		assertNotNull(stored, "Stored profile must be returned (stateless)");
		assertNotNull(stored.getProfileId(), "Stored profile must carry its generated id (stateless)");
		assertEquals("Mathematician", stored.getOccupation());
		assertEquals("NASA", stored.getEmployer());
		assertEquals("katherine@example.com", stored.getPrimaryEmail());
		assertEquals("American", stored.getNationality());
		assertEquals("ProfileLanguage_en", stored.getHomeLanguage());
	}

	@Test
	@Order(5)
	@DisplayName("Clearing values archives their FSDM links and preserves omitted values")
	public void clearProfileValuesStateless()
	{
		IProfileService<?> service = IGuiceContext.get(IProfileService.class);
		ComprehensiveProfileDTO initial = new ComprehensiveProfileDTO();
		initial.setFirstName("Ada");
		initial.setSurname("Lovelace");
		initial.setHomeLanguage("ProfileLanguage_en");
		initial.setOccupation("Mathematician");
		UUID id = SessionUtils.<UUID>withActivityMasterStateless(ENTERPRISE, PROFILE_SYSTEM, tuple ->
				service.saveProfile(tuple.getItem1(), tuple.getItem2(), initial))
				.await().atMost(Duration.ofMinutes(2));

		ComprehensiveProfileDTO clearing = new ComprehensiveProfileDTO();
		clearing.setProfileId(id);
		clearing.setFirstName("");
		clearing.setHomeLanguage("");
		ComprehensiveProfileDTO readback = SessionUtils.<ComprehensiveProfileDTO>withActivityMasterStateless(ENTERPRISE, PROFILE_SYSTEM, tuple ->
				service.saveProfile(tuple.getItem1(), tuple.getItem2(), clearing)
					.chain(saved -> service.getProfile(tuple.getItem1(), tuple.getItem2(), saved)))
				.await().atMost(Duration.ofMinutes(2));
		assertNull(readback.getFirstName());
		assertNull(readback.getHomeLanguage());
		assertEquals("Lovelace", readback.getSurname());
		assertEquals("Mathematician", readback.getOccupation());
	}
}
