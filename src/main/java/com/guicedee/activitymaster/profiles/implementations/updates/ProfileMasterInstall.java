package com.guicedee.activitymaster.profiles.implementations.updates;

import com.guicedee.activitymaster.fsdm.client.services.*;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.systems.ISystemUpdate;
import com.guicedee.activitymaster.fsdm.client.services.systems.SortedUpdate;
import com.guicedee.activitymaster.profiles.ProfileSystem;
import com.guicedee.activitymaster.profiles.enumerations.ProfileIdentificationTypes;
import io.smallrye.mutiny.Uni;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.guicedee.activitymaster.profiles.enumerations.ProfileEventTypes.*;
import static com.guicedee.activitymaster.profiles.enumerations.SiteClientClassifications.*;

@SortedUpdate(sortOrder = 50, taskCount = 1)
public class ProfileMasterInstall implements ISystemUpdate
{
    private static final Logger log = LogManager.getLogger(ProfileMasterInstall.class);
    
    @Override
    public Uni<Boolean> update(IEnterprise<?,?> enterprise)
    {
        // Chain reactive operations
        return createInvolvedPartyClassifications(enterprise)
            .chain(v -> createSiteDetailsClassifications(enterprise))
            .map(v -> true)
            .onFailure().invoke(error -> log.error("Error in ProfileMasterInstall update: {}", error.getMessage(), error));
    }

    private Uni<Void> createInvolvedPartyClassifications(IEnterprise<?,?> enterprise)
    {
        ProfileSystem system = com.guicedee.client.IGuiceContext.get(ProfileSystem.class);
        ISystems<?,?> profileSystem = system.getSystem(enterprise);
        
        IEventService<?> eventsService = com.guicedee.client.IGuiceContext.get(IEventService.class);
        IInvolvedPartyService<?> involvedPartyService = com.guicedee.client.IGuiceContext.get(IInvolvedPartyService.class);
        
        // Chain reactive operations
        return eventsService.createEventType(UserRegistered.toString(), profileSystem, system.getSystemToken(enterprise))
            .chain(userRegistered -> 
                eventsService.createEventType(VisitorRegistered.toString(), profileSystem, system.getSystemToken(enterprise))
            )
            .chain(visitorRegistered -> 
                involvedPartyService.createIdentificationType(
                    profileSystem, 
                    ProfileIdentificationTypes.IdentificationTypeWebClientUUID,
                    "The Web Client UUID stored as a device identifier",
                    system.getSystemToken(enterprise)
                )
            )
            .map(result -> null); // Convert to Void
    }

    private Uni<Void> createSiteDetailsClassifications(IEnterprise<?,?> enterprise)
    {
        IClassificationService<?> classificationService = com.guicedee.client.IGuiceContext.get(IClassificationService.class);
        ProfileSystem system = com.guicedee.client.IGuiceContext.get(ProfileSystem.class);
        ISystems<?,?> profileSystem = system.getSystem(enterprise);
        
        // Chain reactive operations
        return classificationService.create(ClientConnectionDetails, profileSystem)
            .chain(clientConnectionDetails -> 
                classificationService.create(BrowserDeviceCategory, profileSystem, ClientConnectionDetails)
            )
            .chain(browserDeviceCategory -> 
                classificationService.create(OperatingSystemFamily, profileSystem, ClientConnectionDetails)
            )
            .chain(operatingSystemFamily -> 
                classificationService.create(BrowserDeviceCategory, profileSystem, ClientConnectionDetails)
            )
            .chain(browserDeviceCategory -> 
                classificationService.create(BrowserDevice, profileSystem, ClientConnectionDetails)
            )
            .chain(browserDevice -> 
                classificationService.create(BrowserIcon, profileSystem, ClientConnectionDetails)
            )
            .chain(browserIcon -> 
                classificationService.create(OperatingSystem, profileSystem, ClientConnectionDetails)
            )
            .map(result -> null); // Convert to Void
    }
}