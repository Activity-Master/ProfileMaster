package com.armineasy.activitymaster.profiles;

import com.armineasy.activitymaster.activitymaster.db.entities.classifications.Classification;
import com.armineasy.activitymaster.activitymaster.db.entities.events.EventType;
import com.armineasy.activitymaster.activitymaster.db.entities.involvedparty.InvolvedPartyIdentificationType;
import com.armineasy.activitymaster.activitymaster.implementations.ClassificationService;
import com.armineasy.activitymaster.activitymaster.implementations.EventsService;
import com.armineasy.activitymaster.activitymaster.implementations.InvolvedPartyService;
import com.armineasy.activitymaster.activitymaster.implementations.SystemsService;
import com.armineasy.activitymaster.activitymaster.services.IActivityMasterProgressMonitor;
import com.armineasy.activitymaster.activitymaster.services.IActivityMasterSystem;
import com.armineasy.activitymaster.activitymaster.services.dto.IEnterprise;
import com.armineasy.activitymaster.activitymaster.services.dto.ISystems;
import com.armineasy.activitymaster.activitymaster.systems.SystemsSystem;
import com.armineasy.activitymaster.profiles.enumerations.ProfileIdentificationTypes;
import com.google.inject.Singleton;
import com.jwebmp.guicedinjection.GuiceContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.armineasy.activitymaster.profiles.enumerations.ProfileClassifications.*;
import static com.armineasy.activitymaster.profiles.enumerations.ProfileEventTypes.*;

@Singleton
public class ProfileSystem
		implements IActivityMasterSystem<ProfileSystem>
{
	private static final Map<IEnterprise<?>, UUID> systemTokens = new HashMap<>();
	private static final Map<IEnterprise<?>, ISystems> newSystem = new HashMap<>();

	@Override
	public void createDefaults(IEnterprise enterprise, IActivityMasterProgressMonitor progressMonitor)
	{

	}

	private void createInvolvedPartyClassifications(IEnterprise enterprise)
	{
		ClassificationService classificationService = GuiceContext.get(ClassificationService.class);
		ISystems activityMasterSystem = GuiceContext.get(SystemsService.class)
		                                           .getActivityMaster(enterprise);

		EventsService eventsService = GuiceContext.get(EventsService.class);
		EventType eType = eventsService.createEventType(SiteVisit, newSystem.get(enterprise), systemTokens.get(enterprise));
		EventType eType2 = eventsService.createEventType(UserRegistered, newSystem.get(enterprise), systemTokens.get(enterprise));
		EventType eType3 = eventsService.createEventType(UserConfirmedAccount, newSystem.get(enterprise), systemTokens.get(enterprise));

		Classification clazz = classificationService.create(LastLoginTime, newSystem.get(enterprise));
		Classification clazz1 = classificationService.create(LastVisitTime, newSystem.get(enterprise));
		Classification clazz2 = classificationService.create(ConfirmationKey, newSystem.get(enterprise));
		Classification userRolesClassification = classificationService.create(UserRoles, newSystem.get(enterprise));
		clazz.createDefaultSecurity(activityMasterSystem);
		clazz1.createDefaultSecurity(activityMasterSystem);
		clazz2.createDefaultSecurity(activityMasterSystem);
		userRolesClassification.createDefaultSecurity(activityMasterSystem);

		eType.createDefaultSecurity(activityMasterSystem);
		eType2.createDefaultSecurity(activityMasterSystem);
		eType3.createDefaultSecurity(activityMasterSystem);
		InvolvedPartyIdentificationType idType = GuiceContext.get(InvolvedPartyService.class)
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
	public void postUpdate(IEnterprise enterprise, IActivityMasterProgressMonitor progressMonitor)
	{
		newSystem.put(enterprise, GuiceContext.get(SystemsService.class)
		                                      .create(enterprise, "Profiles System",
		                                              "The system for managing User Profiles", ""));
		UUID uuid = GuiceContext.get(SystemsSystem.class)
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
