package com.guicedee.activitymaster.profiles;

import com.google.inject.Singleton;
import com.guicedee.activitymaster.core.services.IActivityMasterProgressMonitor;
import com.guicedee.activitymaster.core.services.IActivityMasterSystem;
import com.guicedee.activitymaster.core.services.dto.IEnterprise;
import com.guicedee.activitymaster.core.services.dto.IInvolvedParty;
import com.guicedee.activitymaster.core.services.dto.ISystems;
import com.guicedee.activitymaster.core.services.system.ActivityMasterDefaultSystem;
import com.guicedee.activitymaster.core.services.system.IClassificationService;
import com.guicedee.activitymaster.core.services.system.IEventService;
import com.guicedee.activitymaster.core.services.system.IInvolvedPartyService;
import com.guicedee.activitymaster.profiles.enumerations.ProfileIdentificationTypes;
import com.guicedee.activitymaster.profiles.services.interfaces.IRolesService;
import com.guicedee.activitymaster.profiles.services.interfaces.IUserRole;
import com.guicedee.guicedinjection.GuiceContext;

import java.util.List;
import java.util.UUID;

import static com.guicedee.activitymaster.core.services.types.IdentificationTypes.*;
import static com.guicedee.activitymaster.profiles.enumerations.ProfileClassifications.*;
import static com.guicedee.activitymaster.profiles.enumerations.ProfileEventTypes.*;
import static com.guicedee.activitymaster.profiles.enumerations.SiteClientClassifications.*;
import static com.guicedee.activitymaster.profiles.services.enumerations.UserRoles.*;
import static com.guicedee.guicedinjection.GuiceContext.*;

@Singleton
public class ProfileSystem
		extends ActivityMasterDefaultSystem<ProfileSystem>
		implements IActivityMasterSystem<ProfileSystem>
{
	@Override
	public void createDefaults(IEnterprise<?> enterprise, IActivityMasterProgressMonitor progressMonitor)
	{
	}

	@Override
	public int totalTasks()
	{
		return 0;
	}

	@Override
	public void postStartup(IEnterprise<?> enterprise, IActivityMasterProgressMonitor progressMonitor)
	{
		super.postStartup(enterprise, progressMonitor);

		ISystems<?> system = getSystem(enterprise);
		UUID token = getSystemToken(enterprise);

		IInvolvedPartyService<?> involvedPartyService = get(IInvolvedPartyService.class);
		IInvolvedParty<?> ip = involvedPartyService.findByIdentificationType(IdentificationTypeEnterpriseCreatorRole, null, system, token);
		if (ip != null)
		{
			IRolesService<?> rolesService = get(IRolesService.class);
			List<IUserRole<?>> roles = rolesService.getRoles(ip, system, token);
			if (!roles.contains(Administrator))
			{
				roles.addAll(rolesService.addRole(ip, Administrator, null, system, token));
			}
		}
	}
	
	@Override
	public String getSystemName()
	{
		return "Profiles Master";
	}

	@Override
	public String getSystemDescription()
	{
		return "The system for managing User Profiles";
	}

}
