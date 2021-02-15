package com.guicedee.activitymaster.profiles.implementations.updates;

import com.guicedee.activitymaster.core.services.IActivityMasterProgressMonitor;
import com.guicedee.activitymaster.core.services.dto.IEnterprise;
import com.guicedee.activitymaster.core.services.dto.ISystems;
import com.guicedee.activitymaster.core.services.system.IClassificationService;
import com.guicedee.activitymaster.core.services.system.IEventService;
import com.guicedee.activitymaster.core.services.system.IInvolvedPartyService;
import com.guicedee.activitymaster.core.updates.DatedUpdate;
import com.guicedee.activitymaster.core.updates.ISystemUpdate;
import com.guicedee.activitymaster.profiles.ProfileSystem;
import com.guicedee.activitymaster.profiles.enumerations.ProfileIdentificationTypes;
import com.guicedee.guicedinjection.GuiceContext;

import static com.guicedee.activitymaster.profiles.enumerations.ProfileClassifications.*;
import static com.guicedee.activitymaster.profiles.enumerations.ProfileClassifications.LogonDetails;
import static com.guicedee.activitymaster.profiles.enumerations.ProfileEventTypes.*;
import static com.guicedee.activitymaster.profiles.enumerations.ProfileEventTypes.UserConfirmedAccount;
import static com.guicedee.activitymaster.profiles.enumerations.SiteClientClassifications.*;
import static com.guicedee.activitymaster.profiles.enumerations.SiteClientClassifications.ClientConnectionDetails;

@DatedUpdate(date = "2020/12/01", taskCount = 1)
public class ProfileMasterInstall implements ISystemUpdate
{
	@Override
	public void update(IEnterprise<?> enterprise, IActivityMasterProgressMonitor progressMonitor)
	{
		createInvolvedPartyClassifications(enterprise);
		createSiteDetailsClassifications(enterprise);
	}
	
	private void createInvolvedPartyClassifications(IEnterprise<?> enterprise)
	{
		
		ProfileSystem system = GuiceContext.get(ProfileSystem.class);
		ISystems<?> profileSystem = system.getSystem(enterprise);
		
		IEventService<?> eventsService = GuiceContext.get(IEventService.class);
	
		eventsService.createEventType(UserRegistered, profileSystem, system.getSystemToken(enterprise));
		eventsService.createEventType(VisitorRegistered, profileSystem, system.getSystemToken(enterprise));
		
		
		GuiceContext.get(IInvolvedPartyService.class)
		            .createIdentificationType(profileSystem, ProfileIdentificationTypes.IdentificationTypeWebClientUUID,
				            "The Web Client UUID stored as a device identifier",
				            system.getSystemToken(enterprise));
	}
	
	private void createSiteDetailsClassifications(IEnterprise<?> enterprise)
	{
		IClassificationService<?> classificationService = GuiceContext.get(IClassificationService.class);
		ProfileSystem system = GuiceContext.get(ProfileSystem.class);
		ISystems<?> profileSystem = system.getSystem(enterprise);
		
		classificationService.create(ClientConnectionDetails, profileSystem);
		classificationService.create(BrowserDeviceCategory, profileSystem,ClientConnectionDetails);
		classificationService.create(OperatingSystemFamily, profileSystem, ClientConnectionDetails);
		classificationService.create(BrowserDeviceCategory, profileSystem, ClientConnectionDetails);
		classificationService.create(BrowserDevice, profileSystem, ClientConnectionDetails);
		classificationService.create(BrowserIcon, profileSystem, ClientConnectionDetails);
		classificationService.create(OperatingSystem, profileSystem, ClientConnectionDetails);
	}
}
