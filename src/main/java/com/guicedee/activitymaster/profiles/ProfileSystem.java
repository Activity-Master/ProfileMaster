package com.guicedee.activitymaster.profiles;

import com.google.inject.Inject;
import com.guicedee.activitymaster.fsdm.client.services.IInvolvedPartyService;
import com.guicedee.activitymaster.fsdm.client.services.ISystemsService;
import com.guicedee.activitymaster.fsdm.client.services.administration.ActivityMasterDefaultSystem;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.systems.IActivityMasterSystem;
import com.guicedee.activitymaster.profiles.services.interfaces.IRolesService;
import io.smallrye.mutiny.Uni;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import static com.guicedee.activitymaster.fsdm.client.services.classifications.types.IdentificationTypes.*;
import static com.guicedee.activitymaster.profiles.services.enumerations.UserRoles.*;
import static com.guicedee.activitymaster.profiles.services.interfaces.IProfileService.*;

public class ProfileSystem
		extends ActivityMasterDefaultSystem<ProfileSystem>
		implements IActivityMasterSystem<ProfileSystem>
{
	private static final Logger log = LogManager.getLogger(ProfileSystem.class);
	
	@Inject
	private ISystemsService<?> systemsService;
	
	@Override
	public ISystems<?,?>  registerSystem(IEnterprise<?,?> enterprise)
	{
		ISystems<?, ?> iSystems = systemsService
		                                        .create(enterprise, getSystemName(), getSystemDescription())
		                                        .await().atMost(Duration.ofMinutes(1));
		systemsService
		              .registerNewSystem(enterprise, getSystem(enterprise))
		              .await().atMost(Duration.ofMinutes(1));
		
		return iSystems;
	}
	
	@Override
	public void createDefaults(IEnterprise<?,?> enterprise)
	{
	
	}
	
	@Override
	public int totalTasks()
	{
		return 0;
	}
	
	@Override
	public Uni<Void> postStartup(IEnterprise<?,?> enterprise)
	{
		ISystems<?,?> system = getSystem(enterprise);
		UUID identityToken = getSystemToken(enterprise);
		
		IInvolvedPartyService<?> involvedPartyService = com.guicedee.client.IGuiceContext.get(IInvolvedPartyService.class);
		
		// Use reactive pattern for finding involved party
		// First get the builder, then call get() which returns a Uni
		Uni<?> ipUni = involvedPartyService.get()
		                                  .builder()
		                                  .findByIdentificationType(IdentificationTypeEnterpriseCreatorRole.toString(), null, system, identityToken)
		                                  .get();
		
		// Execute the reactive chain with proper type handling
		ipUni.chain(ipObj -> {
			if (ipObj != null) {
				// Cast to the correct type
				IInvolvedParty<?, ?> ip = (IInvolvedParty<?, ?>) ipObj;
				IRolesService<?> rolesService = com.guicedee.client.IGuiceContext.get(IRolesService.class);
				
				// Use reactive getRoles method
				return rolesService.getRoles(ip, system, identityToken)
					.chain(roles -> {
						if (!roles.contains(Administrator.toString())) {
							// Use reactive addRole method
							return rolesService.addRole(ip, Administrator.toString(), null, system, identityToken);
						}
						return Uni.createFrom().item(roles);
					});
			}
			return Uni.createFrom().nullItem();
		})
		.subscribe().with(
			result -> {
				// Role assignment completed successfully
				log.debug("Role assignment completed for enterprise creator");
			},
			error -> {
				// Handle error
				log.error("Error assigning roles to enterprise creator: {}", error.getMessage(), error);
			}
		);
		return ipUni.replaceWith(Uni.createFrom().voidItem());
	}
	
	@Override
	public String getSystemName()
	{
		return ProfileSystemName;
	}
	
	@Override
	public String getSystemDescription()
	{
		return "The system for managing User Profiles";
	}
}