package com.guicedee.activitymaster.profiles;

import com.guicedee.activitymaster.core.services.IActivityMasterProgressMonitor;
import com.guicedee.activitymaster.core.services.IActivityMasterSystem;
import com.guicedee.activitymaster.core.services.dto.IEnterprise;
import com.guicedee.activitymaster.core.services.dto.IInvolvedPartyIdentificationType;
import com.guicedee.activitymaster.core.services.dto.ISystems;
import com.guicedee.activitymaster.core.services.system.IClassificationService;
import com.guicedee.activitymaster.core.services.system.IEventService;
import com.guicedee.activitymaster.core.services.system.IInvolvedPartyService;
import com.guicedee.activitymaster.core.services.system.ISystemsService;
import com.guicedee.activitymaster.profiles.enumerations.ProfileIdentificationTypes;
import com.google.inject.Singleton;
import com.guicedee.guicedinjection.GuiceContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.guicedee.activitymaster.profiles.enumerations.ProfileClassifications.*;
import static com.guicedee.activitymaster.profiles.enumerations.ProfileEventTypes.*;

@Singleton
public class ProfileSystem
		implements IActivityMasterSystem<ProfileSystem>
{
	private static final Map<IEnterprise<?>, UUID> systemTokens = new HashMap<>();
	private static final Map<IEnterprise<?>, ISystems<?>> systemsMap = new HashMap<>();

	@Override
	public void createDefaults(IEnterprise<?> enterprise, IActivityMasterProgressMonitor progressMonitor)
	{

	}

	private void createInvolvedPartyClassifications(IEnterprise<?> enterprise)
	{
		IClassificationService<?> classificationService = GuiceContext.get(IClassificationService.class);
		ISystems activityMasterSystem = GuiceContext.get(ISystemsService.class)
		                                            .getActivityMaster(enterprise);

		IEventService<?> eventsService = GuiceContext.get(IEventService.class);
		eventsService.createEventType(SiteVisit, systemsMap.get(enterprise), systemTokens.get(enterprise));
		eventsService.createEventType(UserRegistered, systemsMap.get(enterprise), systemTokens.get(enterprise));
		eventsService.createEventType(VisitorRegistered, systemsMap.get(enterprise), systemTokens.get(enterprise));
		eventsService.createEventType(UserConfirmedAccount, systemsMap.get(enterprise), systemTokens.get(enterprise));

		classificationService.create(LogonDetails, systemsMap.get(enterprise));
		classificationService.create(LastLoginTime, systemsMap.get(enterprise), LogonDetails);
		classificationService.create(LastVisitTime, systemsMap.get(enterprise), LogonDetails);
		classificationService.create(ConfirmationKey, systemsMap.get(enterprise), LogonDetails);
		classificationService.create(UserRoles, systemsMap.get(enterprise), LogonDetails);
		classificationService.create(RememberMe, systemsMap.get(enterprise), LogonDetails);
		classificationService.create(LoggedOn, systemsMap.get(enterprise), LogonDetails);

		IInvolvedPartyIdentificationType<?> idType = GuiceContext.get(IInvolvedPartyService.class)
		                                                         .createIdentificationType(enterprise, ProfileIdentificationTypes.IdentificationTypeWebClientUUID,
		                                                                                   "The Web Client UUID stored as a device identifier",
		                                                                                   systemTokens.get(enterprise));

		idType.createDefaultSecurity(activityMasterSystem);
	}

	@Override
	public int totalTasks()
	{
		return 0;
	}

	@Override
	public void postStartup(IEnterprise<?> enterprise, IActivityMasterProgressMonitor progressMonitor)
	{
		final String systemName = "Profiles Master";
		final String systemDesc = "The system for managing User Profiles";
		ISystems<?> sys = GuiceContext.get(ISystemsService.class)
		                              .findSystem(enterprise, systemName);
		UUID securityToken = null;
		if (sys == null)
		{
			sys = GuiceContext.get(ISystemsService.class)
			                                    .create(enterprise, systemName, systemDesc, systemName);
			securityToken = GuiceContext.get(ISystemsService.class)
			                            .registerNewSystem(enterprise, sys);
		}
		else
		{
			securityToken = GuiceContext.get(ISystemsService.class)
			                            .getSecurityIdentityToken(sys);
		}
		systemTokens.put(enterprise, securityToken);
		systemsMap.put(enterprise, sys);
	}

	@Override
	public void loadUpdates(IEnterprise<?> enterprise, IActivityMasterProgressMonitor progressMonitor)
	{
		createInvolvedPartyClassifications(enterprise);
	}

	public static Map<IEnterprise<?>, UUID> getSystemTokens()
	{
		return systemTokens;
	}

	public static Map<IEnterprise<?>, ISystems<?>> getSystemsMap()
	{
		return systemsMap;
	}
}
