package com.guicedee.activitymaster.profiles.test;

import com.google.inject.Key;
import com.google.inject.name.Names;
import com.guicedee.activitymaster.fsdm.client.services.IEnterpriseService;
import com.guicedee.activitymaster.fsdm.client.services.SessionUtils;
import com.guicedee.activitymaster.fsdm.client.services.administration.ActivityMasterConfiguration;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.profiles.implementations.updates.ProfileMasterInstall;
import com.guicedee.activitymaster.profiles.services.interfaces.IProfileService;
import com.guicedee.activitymaster.profiles.webdto.ComprehensiveProfileDTO;
import com.guicedee.client.IGuiceContext;
import com.guicedee.client.utils.LogUtils;
import io.smallrye.mutiny.Uni;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.*;

import java.time.Duration;
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
		sessionFactory.withSession(session -> session.withTransaction(tx ->
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
		IEnterprise<?, ?> enterprise = sessionFactory.withSession(s -> es.getEnterprise(s, ENTERPRISE))
				.await().atMost(Duration.ofMinutes(1));
		assertNotNull(enterprise, "Baseline enterprise must be provisioned in setup");

		Boolean installed = sessionFactory.withSession(s -> s.withTransaction(tx -> install.update(s, enterprise)))
				.await().atMost(Duration.ofMinutes(3));
		assertEquals(Boolean.TRUE, installed, "Profile taxonomy installation should succeed");
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
			Mutiny.Session session = tuple.getItem1();
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

		UUID id = SessionUtils.<UUID>withActivityMaster(ENTERPRISE, PROFILE_SYSTEM, tuple -> {
			Mutiny.Session session = tuple.getItem1();
			IEnterprise<?, ?> enterprise = tuple.getItem2();
			return profileService.saveProfile(session, enterprise, initial);
		}).await().atMost(Duration.ofMinutes(2));
		assertNotNull(id);

		// Update the same profile id with a new occupation + email
		ComprehensiveProfileDTO update = new ComprehensiveProfileDTO();
		update.setProfileId(id);
		update.setOccupation("Rear Admiral");
		update.setPrimaryEmail("grace@example.com");

		ComprehensiveProfileDTO reread = SessionUtils.<ComprehensiveProfileDTO>withActivityMaster(ENTERPRISE, PROFILE_SYSTEM, tuple -> {
			Mutiny.Session session = tuple.getItem1();
			IEnterprise<?, ?> enterprise = tuple.getItem2();
			return profileService.saveProfile(session, enterprise, update)
					.chain(savedId -> profileService.getProfile(session, enterprise, savedId));
		}).await().atMost(Duration.ofMinutes(2));

		assertNotNull(reread);
		assertEquals(id, reread.getProfileId(), "Update must operate on the same profile id");
		assertEquals("Rear Admiral", reread.getOccupation(), "Updated field must be persisted");
		assertEquals("grace@example.com", reread.getPrimaryEmail(), "New field must be persisted");
	}

	@Test
	@Order(3)
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


