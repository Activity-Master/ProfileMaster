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
import com.guicedee.guicedinjection.GuiceContext;

import java.util.Set;
import java.util.UUID;

import static com.guicedee.activitymaster.fsdm.client.services.classifications.types.IdentificationTypes.*;
import static com.guicedee.activitymaster.profiles.services.enumerations.UserRoles.*;
import static com.guicedee.activitymaster.profiles.services.interfaces.IProfileService.*;

public class ProfileSystem
		extends ActivityMasterDefaultSystem<ProfileSystem>
		implements IActivityMasterSystem<ProfileSystem>
{
	@Inject
	private ISystemsService<?> systemsService;
	
	@Override
	public ISystems<?,?>  registerSystem(IEnterprise<?,?> enterprise)
	{
		ISystems<?, ?> iSystems = systemsService
		                                        .create(enterprise, getSystemName(), getSystemDescription());
		systemsService
		              .registerNewSystem(enterprise, getSystem(enterprise));
		
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
	public void postStartup(IEnterprise<?,?> enterprise)
	{
		ISystems<?,?> system = getSystem(enterprise);
		UUID identityToken = getSystemToken(enterprise);
		
		IInvolvedPartyService<?> involvedPartyService = GuiceContext.get(IInvolvedPartyService.class);
		IInvolvedParty<?, ?> ip = involvedPartyService.get()
		                                              .builder()
		                                              .findByIdentificationType(IdentificationTypeEnterpriseCreatorRole.toString(), null, system, identityToken)
		                                              .get()
		                                              .orElse(null);
		if (ip != null)
		{
			IRolesService<?> rolesService = GuiceContext.get(IRolesService.class);
			Set<String> roles = rolesService.getRoles(ip, system, identityToken);
			if (!roles.contains(Administrator.toString()))
			{
				roles.addAll(rolesService.addRole(ip, Administrator.toString(), null, system, identityToken));
			}
		}
		super.postStartup(enterprise);
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
