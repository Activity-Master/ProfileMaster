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
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import io.smallrye.mutiny.Uni;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration test proving that the profiles module's data structures become available
 * through the shared ActivityMaster GraphQL schema and service registry purely by being on the class/module path.
 */
@Log4j2
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProfileGraphQLIntegrationTest
{
	private static final String ENTERPRISE = "ProfileTestCo";
	private static final String PROFILE_SYSTEM = IProfileService.ProfileSystemName;

	private Mutiny.SessionFactory sessionFactory;
	private GraphQL graphQL;
	private UUID testProfileId;

	@BeforeAll
	public void setup()
	{
		LogUtils.addConsoleLogger(Level.INFO);
		ActivityMasterConfiguration.get().setApplicationEnterpriseName(ENTERPRISE);
		IGuiceContext.instance();

		sessionFactory = IGuiceContext.get(Key.get(Mutiny.SessionFactory.class, Names.named("ActivityMaster-Test")));
		assertNotNull(sessionFactory, "SessionFactory should not be null");

		graphQL = IGuiceContext.get(GraphQL.class);
		assertNotNull(graphQL, "GraphQL instance should be assembled from the schema providers");

		IEnterpriseService<?> es = IGuiceContext.get(IEnterpriseService.class);
		sessionFactory.withStatelessSession(session -> session.withTransaction(tx ->
				es.getEnterprise(session, ENTERPRISE)
						.onFailure().recoverWithUni(t -> {
							var ent = es.get();
							ent.setName(ENTERPRISE);
							ent.setDescription("Profile GraphQL integration-test enterprise");
							return es.createNewEnterprise(session, ent)
									.chain(e -> es.startNewEnterprise(session, ENTERPRISE, "admin", "adminadmin!@"));
						})
						.replaceWith(Uni.createFrom().voidItem())
		)).await().atMost(Duration.ofMinutes(3));

		// Install profile taxonomy (name types + classifications)
		ProfileMasterInstall install = IGuiceContext.get(ProfileMasterInstall.class);
		IEnterprise<?, ?> enterprise = sessionFactory.withStatelessSession(s -> es.getEnterprise(s, ENTERPRISE))
				.await().atMost(Duration.ofMinutes(1));
		assertNotNull(enterprise, "Baseline enterprise must be provisioned in setup");

		Boolean installed = sessionFactory.withStatelessSession(s -> s.withTransaction(tx -> install.update(s, enterprise)))
				.await().atMost(Duration.ofMinutes(3));
		assertEquals(Boolean.TRUE, installed, "Profile taxonomy installation should succeed");

		// Persist test profile
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

		testProfileId = SessionUtils.<UUID>withActivityMaster(ENTERPRISE, PROFILE_SYSTEM, tuple -> {
			Mutiny.StatelessSession session = tuple.getItem1();
			IEnterprise<?, ?> ent = tuple.getItem2();
			IProfileService<?> profileService = IGuiceContext.get(IProfileService.class);
			return profileService.saveProfile(session, ent, profile);
		}).await().atMost(Duration.ofMinutes(2));

		assertNotNull(testProfileId, "Test profile ID must be generated");
	}

	@Test
	@Order(1)
	@DisplayName("GraphQL profile query resolves comprehensive profile attributes")
	public void profileQueryReturnsStronglyTypedProfile()
	{
		String document =
				"query Profile($e: String!, $s: String!, $id: String!) {\n"
				+ "    profile(enterprise: $e, system: $s, profileId: $id) {\n"
				+ "        profileId\n"
				+ "        enterpriseName\n"
				+ "        title\n"
				+ "        firstName\n"
				+ "        surname\n"
				+ "        occupation\n"
				+ "        jobTitle\n"
				+ "        employer\n"
				+ "        primaryEmail\n"
				+ "        mobileNumber\n"
				+ "        nationality\n"
				+ "        dateOfBirth\n"
				+ "        city\n"
				+ "        country\n"
				+ "        linkedIn\n"
				+ "    }\n"
				+ "}\n";

		ExecutionInput input = ExecutionInput.newExecutionInput()
				.query(document)
				.variables(Map.of("e", ENTERPRISE, "s", PROFILE_SYSTEM, "id", testProfileId.toString()))
				.build();

		ExecutionResult result;
		try
		{
			result = graphQL.executeAsync(input).get(2, TimeUnit.MINUTES);
		}
		catch (Exception e)
		{
			throw new RuntimeException("GraphQL execution failed for profile query", e);
		}

		assertTrue(result.getErrors().isEmpty(), () -> "GraphQL errors: " + result.getErrors());

		Map<String, Object> data = result.getData();
		assertNotNull(data, "GraphQL data should not be null");
		@SuppressWarnings("unchecked")
		Map<String, Object> profile = (Map<String, Object>) data.get("profile");
		assertNotNull(profile, "profile query should resolve a profile");

		assertEquals(testProfileId.toString(), profile.get("profileId"));
		assertEquals(ENTERPRISE, profile.get("enterpriseName"));
		assertEquals("Dr", profile.get("title"));
		assertEquals("Ada", profile.get("firstName"));
		assertEquals("Lovelace", profile.get("surname"));
		assertEquals("Mathematician", profile.get("occupation"));
		assertEquals("Analyst", profile.get("jobTitle"));
		assertEquals("Analytical Engines Ltd", profile.get("employer"));
		assertEquals("ada@example.com", profile.get("primaryEmail"));
		assertEquals("+27 11 555 0100", profile.get("mobileNumber"));
		assertEquals("British", profile.get("nationality"));
		assertEquals("1815-12-10", profile.get("dateOfBirth"));
		assertEquals("London", profile.get("city"));
		assertEquals("United Kingdom", profile.get("country"));
		assertEquals("https://linkedin.com/in/ada", profile.get("linkedIn"));
	}

	@Test
	@Order(2)
	@DisplayName("GraphQL comprehensiveProfile query resolves comprehensive profile attributes")
	public void comprehensiveProfileQueryReturnsStronglyTypedProfile()
	{
		String document =
				"query ComprehensiveProfile($e: String!, $s: String!, $id: String!) {\n"
				+ "    comprehensiveProfile(enterprise: $e, system: $s, profileId: $id) {\n"
				+ "        profileId\n"
				+ "        title\n"
				+ "        firstName\n"
				+ "        surname\n"
				+ "        occupation\n"
				+ "    }\n"
				+ "}\n";

		ExecutionInput input = ExecutionInput.newExecutionInput()
				.query(document)
				.variables(Map.of("e", ENTERPRISE, "s", PROFILE_SYSTEM, "id", testProfileId.toString()))
				.build();

		ExecutionResult result;
		try
		{
			result = graphQL.executeAsync(input).get(2, TimeUnit.MINUTES);
		}
		catch (Exception e)
		{
			throw new RuntimeException("GraphQL execution failed for comprehensiveProfile query", e);
		}

		assertTrue(result.getErrors().isEmpty(), () -> "GraphQL errors: " + result.getErrors());

		Map<String, Object> data = result.getData();
		assertNotNull(data, "GraphQL data should not be null");
		@SuppressWarnings("unchecked")
		Map<String, Object> profile = (Map<String, Object>) data.get("comprehensiveProfile");
		assertNotNull(profile, "comprehensiveProfile query should resolve a profile");

		assertEquals(testProfileId.toString(), profile.get("profileId"));
		assertEquals("Dr", profile.get("title"));
		assertEquals("Ada", profile.get("firstName"));
		assertEquals("Lovelace", profile.get("surname"));
		assertEquals("Mathematician", profile.get("occupation"));
	}
}
