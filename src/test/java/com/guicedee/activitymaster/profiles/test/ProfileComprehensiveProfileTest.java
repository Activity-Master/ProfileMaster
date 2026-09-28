package com.guicedee.activitymaster.profiles.test;

import com.google.inject.Key;
import com.google.inject.name.Names;
import com.guicedee.activitymaster.fsdm.client.services.IEnterpriseService;
import com.guicedee.activitymaster.fsdm.client.services.SessionUtils;
import com.guicedee.activitymaster.fsdm.client.services.administration.ActivityMasterConfiguration;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.classifications.types.NameTypes;
import com.guicedee.activitymaster.profiles.enumerations.ProfileAttributeChoices;
import com.guicedee.activitymaster.profiles.enumerations.ProfileNameRealms;
import com.guicedee.activitymaster.profiles.implementations.updates.ProfileAttributeChoicesInstall;
import com.guicedee.activitymaster.profiles.implementations.updates.ProfileMasterInstall;
import com.guicedee.activitymaster.profiles.implementations.updates.ProfileNameRealmsInstall;
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

	@BeforeAll
	public void setup()
	{
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
	}

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
		assertEquals(List.of("Gender", "Pronouns", "MaritalStatus"), List.copyOf(choices.keySet()));
		for (var attribute : ProfileAttributeChoices.attributes())
		{
			List<String> expected = java.util.Arrays.stream(ProfileAttributeChoices.values())
					.filter(choice -> choice.attribute() == attribute).map(Enum::name).toList();
			assertEquals(expected, choices.get(attribute.name()).stream().map(ProfileAttributeChoiceDTO::value).toList(),
					attribute + " children in declared order, once each after a repeated install");
		}
		assertEquals(new ProfileAttributeChoiceDTO("MaritalStatusPartnered", "Domestic partnership"),
				choices.get("MaritalStatus").get(2));
		assertEquals("Prefer not to say", choices.get("Gender").getLast().label());
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
		profile.setCity("London");
		profile.setCountry("United Kingdom");
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
		assertEquals("London", stored.getCity());
		assertEquals("United Kingdom", stored.getCountry());
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
		initial.setCity("New York");

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
		update.setCity("Arlington");

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
		assertEquals("Arlington", reread.getCity(), "Another existing attribute must be replaced");
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
		profile.setCity("Hampton");

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
		assertEquals("Hampton", stored.getCity());
	}
}
