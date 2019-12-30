package com.guicedee.activitymaster.profiles;

import com.google.inject.Singleton;
import com.guicedee.activitymaster.core.services.IActivityMasterProgressMonitor;
import com.guicedee.activitymaster.core.services.IActivityMasterSystem;
import com.guicedee.activitymaster.core.services.dto.IEnterprise;
import com.guicedee.activitymaster.core.services.dto.IInvolvedParty;
import com.guicedee.activitymaster.core.services.dto.IInvolvedPartyIdentificationType;
import com.guicedee.activitymaster.core.services.dto.ISystems;
import com.guicedee.activitymaster.core.services.system.*;
import com.guicedee.activitymaster.profiles.enumerations.ProfileIdentificationTypes;
import com.guicedee.activitymaster.profiles.services.interfaces.IRolesService;
import com.guicedee.activitymaster.profiles.services.interfaces.IUserRole;
import com.guicedee.guicedinjection.GuiceContext;

import java.util.List;
import java.util.UUID;

import static com.guicedee.activitymaster.core.services.types.IdentificationTypes.*;
import static com.guicedee.activitymaster.profiles.enumerations.ProfileClassifications.*;
import static com.guicedee.activitymaster.profiles.enumerations.ProfileEventTypes.*;
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
	public void loadUpdates(IEnterprise<?> enterprise, IActivityMasterProgressMonitor progressMonitor)
	{
		createInvolvedPartyClassifications(enterprise);
	}

	private void createInvolvedPartyClassifications(IEnterprise<?> enterprise)
	{
		IClassificationService<?> classificationService = GuiceContext.get(IClassificationService.class);
		ISystems activityMasterSystem = GuiceContext.get(ISystemsService.class)
		                                            .getActivityMaster(enterprise);

		IEventService<?> eventsService = GuiceContext.get(IEventService.class);
		eventsService.createEventType(SiteVisit, getSystem(enterprise), getSystemToken(enterprise));
		eventsService.createEventType(UserRegistered, getSystem(enterprise), getSystemToken(enterprise));
		eventsService.createEventType(VisitorRegistered, getSystem(enterprise), getSystemToken(enterprise));
		eventsService.createEventType(UserConfirmedAccount, getSystem(enterprise), getSystemToken(enterprise));

		classificationService.create(LogonDetails, getSystem(enterprise));
		classificationService.create(LastLoginTime, getSystem(enterprise), LogonDetails);
		classificationService.create(LastVisitTime, getSystem(enterprise), LogonDetails);
		classificationService.create(ConfirmationKey, getSystem(enterprise), LogonDetails);
		classificationService.create(UserRoles, getSystem(enterprise), LogonDetails);
		classificationService.create(RememberMe, getSystem(enterprise), LogonDetails);
		classificationService.create(LoggedOn, getSystem(enterprise), LogonDetails);

		IInvolvedPartyIdentificationType<?> idType = GuiceContext.get(IInvolvedPartyService.class)
		                                                         .createIdentificationType(enterprise, ProfileIdentificationTypes.IdentificationTypeWebClientUUID,
		                                                                                   "The Web Client UUID stored as a device identifier",
		                                                                                   getSystemToken(enterprise));
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
