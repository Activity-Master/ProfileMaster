package com.guicedee.activitymaster.profiles.rest;

import com.google.inject.Inject;
import com.guicedee.activitymaster.fsdm.client.services.SessionUtils;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.profiles.services.interfaces.IProfileService;
import com.guicedee.activitymaster.profiles.webdto.ComprehensiveProfileDTO;
import io.smallrye.mutiny.Uni;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.log4j.Log4j2;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.UUID;

/**
 * JAX-RS resource exposing comprehensive individual-profile storage for ActivityMaster.
 *
 * <p>Each profile is stored against an {@code InvolvedParty} within the requesting system's security
 * scope: names are persisted through the FSDM name-type mechanism and every other attribute
 * (occupation, contact, demographics, identification, social handles, etc.) as classification values.
 * A profile is only readable inside the security scope that stored it.</p>
 *
 * <p>All operations run inside the requesting system's security scope via
 * {@link SessionUtils#withActivityMaster}, referencing the owning enterprise and requesting system by
 * name.</p>
 */
@Path("{enterprise}/profile")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Profile", description = "Comprehensive individual-profile storage — names, demographics, contact, address, occupation, education, identification, social presence and more, backed by security-scoped involved parties.")
@Log4j2
public class ProfileRestService
{
	@Inject
	private IProfileService<?> profileService;

	/**
	 * Creates (or upserts) a comprehensive profile and returns the fully-hydrated stored profile.
	 */
	@POST
	@Path("{requestingSystemName}/create")
	@Operation(summary = "Create a comprehensive profile",
			description = "Stores an individual's full profile (names, demographics, contact, occupation, etc.) and returns the hydrated profile including its generated id.")
	@ApiResponse(responseCode = "200", description = "Profile created")
	@ApiResponse(responseCode = "500", description = "Storage failure")
	public Uni<ComprehensiveProfileDTO> create(@Parameter(description = "Owning enterprise name") @PathParam("enterprise") String enterpriseName,
	                                           @Parameter(description = "Requesting system name (security scope)") @PathParam("requestingSystemName") String systemName,
	                                           ComprehensiveProfileDTO profile)
	{
		return save(enterpriseName, systemName, profile);
	}

	/**
	 * Updates an existing comprehensive profile (identified by its {@code profileId}) and returns the
	 * fully-hydrated stored profile.
	 */
	@PUT
	@Path("{requestingSystemName}/update")
	@Operation(summary = "Update a comprehensive profile",
			description = "Updates an existing individual's full profile (identified by profileId) and returns the hydrated profile. Only supplied fields are written.")
	@ApiResponse(responseCode = "200", description = "Profile updated")
	@ApiResponse(responseCode = "500", description = "Storage failure")
	public Uni<ComprehensiveProfileDTO> update(@Parameter(description = "Owning enterprise name") @PathParam("enterprise") String enterpriseName,
	                                           @Parameter(description = "Requesting system name (security scope)") @PathParam("requestingSystemName") String systemName,
	                                           ComprehensiveProfileDTO profile)
	{
		return save(enterpriseName, systemName, profile);
	}

	/**
	 * Reads back the comprehensive profile for the given profile id.
	 */
	@GET
	@Path("{requestingSystemName}/find/{profileId}")
	@Operation(summary = "Find a comprehensive profile by id",
			description = "Reads back the full profile for the given profile id, hydrating every stored name and attribute.")
	@ApiResponse(responseCode = "200", description = "Profile found")
	@ApiResponse(responseCode = "500", description = "Lookup failure")
	public Uni<ComprehensiveProfileDTO> find(@Parameter(description = "Owning enterprise name") @PathParam("enterprise") String enterpriseName,
	                                         @Parameter(description = "Requesting system name (security scope)") @PathParam("requestingSystemName") String systemName,
	                                         @Parameter(description = "Profile id (UUID)") @PathParam("profileId") String profileId)
	{
		UUID id = UUID.fromString(profileId);
		return SessionUtils.<ComprehensiveProfileDTO>withActivityMaster(enterpriseName, systemName, tuple -> {
			Mutiny.Session session = tuple.getItem1();
			IEnterprise<?, ?> enterprise = tuple.getItem2();
			return profileService.getProfile(session, enterprise, id);
		}).onFailure().invoke(e ->
				log.error("Error finding profile {} for enterprise {} and system {}: {}",
						profileId, enterpriseName, systemName, e.getMessage(), e));
	}

	private Uni<ComprehensiveProfileDTO> save(String enterpriseName, String systemName, ComprehensiveProfileDTO profile)
	{
		return SessionUtils.<ComprehensiveProfileDTO>withActivityMaster(enterpriseName, systemName, tuple -> {
			Mutiny.Session session = tuple.getItem1();
			IEnterprise<?, ?> enterprise = tuple.getItem2();
			return profileService.saveProfile(session, enterprise, profile)
					.chain(id -> profileService.getProfile(session, enterprise, id));
		}).onFailure().invoke(e ->
				log.error("Error saving profile for enterprise {} and system {}: {}",
						enterpriseName, systemName, e.getMessage(), e));
	}
}

