package com.guicedee.activitymaster.profiles;

import com.google.inject.Inject;
import com.guicedee.activitymaster.fsdm.client.services.ISystemsService;
import com.guicedee.activitymaster.fsdm.client.services.administration.ActivityMasterDefaultSystem;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.systems.IActivityMasterSystem;

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
		systemsService.registerNewSystem(enterprise, getSystem(enterprise));
		
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
		/*ISystems<?,?> system = getSystem(enterprise);
		UUID identityToken = getSystemToken(enterprise);
		
		Party party = new PartyCall(new AuthenticationConfiguration()).find(new IdentificationTypes()
				.setIdentificationType(new IdentificationType().setName(IdentificationTypeEnterpriseCreatorRole))
				.setValue(null), null, UserRoles.toString());
		*/
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
