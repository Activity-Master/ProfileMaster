package com.guicedee.activitymaster.profiles.rest;

import com.google.inject.name.Named;
import com.guicedee.activitymaster.fsdm.client.services.administration.ActivityMasterConfiguration;
import com.guicedee.activitymaster.profiles.webdto.ComprehensiveProfileDTO;
import com.guicedee.rest.client.RestClient;
import com.guicedee.rest.client.annotations.Endpoint;
import io.smallrye.mutiny.Uni;

import java.util.UUID;

/**
 * Typed REST client for the comprehensive profile endpoints exposed by {@link ProfileRestService}.
 *
 * <p>Mirrors the ActivityMaster {@code RestClients} convention: each {@link Endpoint} is templated
 * with {@code {enterpriseName}}/{@code {systemName}} path params resolved per call against the
 * configured {@code ${ACTIVITY_MASTER_HOST}} and the application enterprise name.</p>
 *
 * <pre>{@code
 * ComprehensiveProfileDTO profile = new ComprehensiveProfileDTO();
 * profile.setFirstName("Ada");
 * profile.setSurname("Lovelace");
 * profile.setOccupation("Mathematician");
 * ComprehensiveProfileDTO stored = profileRestClients.createProfile("Profiles Master", profile)
 *         .await().indefinitely();
 * }</pre>
 */
public class ProfileRestClients
{
	@Endpoint(url = "${ACTIVITY_MASTER_HOST}/{enterpriseName}/profile/{systemName}/create", method = "POST")
	@Named("ProfileCreateService")
	private RestClient<ComprehensiveProfileDTO, ComprehensiveProfileDTO> profileCreate;

	@Endpoint(url = "${ACTIVITY_MASTER_HOST}/{enterpriseName}/profile/{systemName}/update", method = "PUT")
	@Named("ProfileUpdateService")
	private RestClient<ComprehensiveProfileDTO, ComprehensiveProfileDTO> profileUpdate;

	@Endpoint(url = "${ACTIVITY_MASTER_HOST}/{enterpriseName}/profile/{systemName}/find/{profileId}", method = "GET")
	@Named("ProfileFindService")
	private RestClient<Void, ComprehensiveProfileDTO> profileFind;

	/**
	 * Creates (or upserts) a comprehensive profile via the given requesting system.
	 */
	public Uni<ComprehensiveProfileDTO> createProfile(String systemName, ComprehensiveProfileDTO profile)
	{
		return profileCreate.pathParam("enterpriseName", ActivityMasterConfiguration.applicationEnterpriseName)
				.pathParam("systemName", systemName)
				.send(profile);
	}

	/**
	 * Updates an existing comprehensive profile via the given requesting system.
	 */
	public Uni<ComprehensiveProfileDTO> updateProfile(String systemName, ComprehensiveProfileDTO profile)
	{
		return profileUpdate.pathParam("enterpriseName", ActivityMasterConfiguration.applicationEnterpriseName)
				.pathParam("systemName", systemName)
				.send(profile);
	}

	/**
	 * Reads back a comprehensive profile by id via the given requesting system.
	 */
	public Uni<ComprehensiveProfileDTO> findProfile(String systemName, UUID profileId)
	{
		return profileFind.pathParam("enterpriseName", ActivityMasterConfiguration.applicationEnterpriseName)
				.pathParam("systemName", systemName)
				.pathParam("profileId", profileId.toString())
				.send();
	}
}

