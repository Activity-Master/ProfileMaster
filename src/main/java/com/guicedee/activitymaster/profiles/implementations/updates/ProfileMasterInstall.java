package com.guicedee.activitymaster.profiles.implementations.updates;

import com.guicedee.activitymaster.fsdm.client.services.*;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.systems.ISystemUpdate;
import com.guicedee.activitymaster.fsdm.client.services.systems.SortedUpdate;
import com.guicedee.activitymaster.profiles.ProfileSystem;
import com.guicedee.activitymaster.profiles.enumerations.ProfileIdentificationTypes;

import static com.guicedee.activitymaster.profiles.enumerations.ProfileEventTypes.*;
import static com.guicedee.activitymaster.profiles.enumerations.SiteClientClassifications.*;

@SortedUpdate(sortOrder = 50, taskCount = 1)
public class ProfileMasterInstall implements ISystemUpdate
{
	@Override
	public void update(IEnterprise<?,?> enterprise)
	{
		createInvolvedPartyClassifications(enterprise);
		createSiteDetailsClassifications(enterprise);
	}
	
	private void createInvolvedPartyClassifications(IEnterprise<?,?> enterprise)
	{
		
		ProfileSystem system = com.guicedee.client.IGuiceContext.get(ProfileSystem.class);
		ISystems<?,?> profileSystem = system.getSystem(enterprise);
		
		IEventService<?> eventsService = com.guicedee.client.IGuiceContext.get(IEventService.class);
	
		eventsService.createEventType(UserRegistered.toString(), profileSystem, system.getSystemToken(enterprise));
		eventsService.createEventType(VisitorRegistered.toString(), profileSystem, system.getSystemToken(enterprise));
		
		
		com.guicedee.client.IGuiceContext.get(IInvolvedPartyService.class)
		            .createIdentificationType(profileSystem, ProfileIdentificationTypes.IdentificationTypeWebClientUUID,
				            "The Web Client UUID stored as a device identifier",
				            system.getSystemToken(enterprise));
	}
	
	private void createSiteDetailsClassifications(IEnterprise<?,?> enterprise)
	{
		IClassificationService<?> classificationService = com.guicedee.client.IGuiceContext.get(IClassificationService.class);
		ProfileSystem system = com.guicedee.client.IGuiceContext.get(ProfileSystem.class);
		ISystems<?,?> profileSystem = system.getSystem(enterprise);
		
		classificationService.create(ClientConnectionDetails, profileSystem);
		classificationService.create(BrowserDeviceCategory, profileSystem,ClientConnectionDetails);
		classificationService.create(OperatingSystemFamily, profileSystem, ClientConnectionDetails);
		classificationService.create(BrowserDeviceCategory, profileSystem, ClientConnectionDetails);
		classificationService.create(BrowserDevice, profileSystem, ClientConnectionDetails);
		classificationService.create(BrowserIcon, profileSystem, ClientConnectionDetails);
		classificationService.create(OperatingSystem, profileSystem, ClientConnectionDetails);
	}
}
