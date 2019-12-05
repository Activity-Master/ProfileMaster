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
	private static final Map<IEnterprise<?>, ISystems> newSystem = new HashMap<>();

	@Override
	public void createDefaults(IEnterprise<?> enterprise, IActivityMasterProgressMonitor progressMonitor)
	{

	}

	private void createInvolvedPartyClassifications(IEnterprise enterprise)
	{
		IClassificationService<?> classificationService = GuiceContext.get(IClassificationService.class);
		ISystems activityMasterSystem = GuiceContext.get(ISystemsService.class)
		                                            .getActivityMaster(enterprise);

		IEventService<?> eventsService = GuiceContext.get(IEventService.class);
		eventsService.createEventType(SiteVisit, newSystem.get(enterprise), systemTokens.get(enterprise));
		eventsService.createEventType(UserRegistered, newSystem.get(enterprise), systemTokens.get(enterprise));
		eventsService.createEventType(VisitorRegistered, newSystem.get(enterprise), systemTokens.get(enterprise));
		eventsService.createEventType(UserConfirmedAccount, newSystem.get(enterprise), systemTokens.get(enterprise));

		classificationService.create(LastLoginTime, newSystem.get(enterprise));
		classificationService.create(LastVisitTime, newSystem.get(enterprise));
		classificationService.create(ConfirmationKey, newSystem.get(enterprise));
		classificationService.create(UserRoles, newSystem.get(enterprise));
		classificationService.create(RememberMe, newSystem.get(enterprise));
		classificationService.create(LoggedOn, newSystem.get(enterprise));

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
	public void postUpdate(IEnterprise<?> enterprise, IActivityMasterProgressMonitor progressMonitor)
	{
		newSystem.put(enterprise, GuiceContext.get(ISystemsService.class)
		                                      .create(enterprise, "Profiles System",
		                                              "The system for managing User Profiles", ""));
		UUID uuid = GuiceContext.get(ISystemsService.class)
		                        .registerNewSystem(enterprise, newSystem.get(enterprise));
		systemTokens.put(enterprise, uuid);

		createInvolvedPartyClassifications(enterprise);
	}

	public static Map<IEnterprise<?>, UUID> getSystemTokens()
	{
		return systemTokens;
	}

	public static Map<IEnterprise<?>, ISystems> getNewSystem()
	{
		return newSystem;
	}
}
