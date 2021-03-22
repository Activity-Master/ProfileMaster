package com.guicedee.activitymaster.profiles;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.guicedee.activitymaster.client.services.IInvolvedPartyService;
import com.guicedee.activitymaster.client.services.ISystemsService;
import com.guicedee.activitymaster.client.services.administration.ActivityMasterDefaultSystem;
import com.guicedee.activitymaster.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.client.services.systems.IActivityMasterProgressMonitor;
import com.guicedee.activitymaster.client.services.systems.IActivityMasterSystem;
import com.guicedee.activitymaster.profiles.services.interfaces.IRolesService;

import java.util.Set;
import java.util.UUID;

import static com.guicedee.activitymaster.client.services.classifications.types.IdentificationTypes.*;
import static com.guicedee.activitymaster.profiles.services.enumerations.UserRoles.*;
import static com.guicedee.activitymaster.profiles.services.interfaces.IProfileService.*;
import static com.guicedee.guicedinjection.GuiceContext.*;

public class ProfileSystem
		extends ActivityMasterDefaultSystem<ProfileSystem>
		implements IActivityMasterSystem<ProfileSystem>
{
	@Inject
	private Provider<ISystemsService<?>> systemsService;
	
	@Override
	public ISystems<?,?>  registerSystem(IEnterprise<?,?> enterprise, IActivityMasterProgressMonitor progressMonitor)
	{
		ISystems<?, ?> iSystems = systemsService.get()
		                                        .create(enterprise, getSystemName(), getSystemDescription());
		systemsService.get()
		              .registerNewSystem(enterprise, getSystem(enterprise));
		
		return iSystems;
	}
	
	@Override
	public void createDefaults(IEnterprise<?,?> enterprise, IActivityMasterProgressMonitor progressMonitor)
	{
	
	}
	
	@Override
	public int totalTasks()
	{
		return 0;
	}
	
	@Override
	public void postStartup(IEnterprise<?,?> enterprise, IActivityMasterProgressMonitor progressMonitor)
	{
		ISystems<?,?> system = getSystem(enterprise);
		UUID token = getSystemToken(enterprise);
		
		IInvolvedPartyService<?> involvedPartyService = get(IInvolvedPartyService.class);
		IInvolvedParty<?,?> ip = involvedPartyService.findByIdentificationType(IdentificationTypeEnterpriseCreatorRole.toString(), null, system, token);
		if (ip != null)
		{
			IRolesService<?> rolesService = get(IRolesService.class);
			Set<String> roles = rolesService.getRoles(ip, system, token);
			if (!roles.contains(Administrator.toString()))
			{
				roles.addAll(rolesService.addRole(ip, Administrator.toString(), null, system, token));
			}
		}
		super.postStartup(enterprise, progressMonitor);
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
