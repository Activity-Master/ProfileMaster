package com.guicedee.activitymaster.profiles.implementations.updates;

import com.guicedee.activitymaster.fsdm.client.services.*;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts;
import com.guicedee.activitymaster.fsdm.client.services.classifications.types.NameTypes;
import com.guicedee.activitymaster.fsdm.client.services.systems.ISystemUpdate;
import com.guicedee.activitymaster.fsdm.client.services.systems.SortedUpdate;
import com.guicedee.activitymaster.profiles.ProfileSystem;
import com.guicedee.activitymaster.profiles.enumerations.ProfileAttributes;
import com.guicedee.activitymaster.profiles.enumerations.ProfileIdentificationTypes;
import io.smallrye.mutiny.Uni;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.UUID;

import static com.guicedee.activitymaster.profiles.enumerations.ProfileEventTypes.*;
import static com.guicedee.activitymaster.profiles.enumerations.SiteClientClassifications.*;

@SortedUpdate(sortOrder = 50, taskCount = 1)
public class ProfileMasterInstall implements ISystemUpdate
{
    private static final Logger log = LogManager.getLogger(ProfileMasterInstall.class);
    
    @Override
    public Uni<Boolean> update(Mutiny.Session session, IEnterprise<?,?> enterprise)
    {
        // Chain reactive operations
        return createInvolvedPartyClassifications(session, enterprise)
            .chain(v -> createSiteDetailsClassifications(session, enterprise))
            .chain(v -> createProfileNameTypes(session, enterprise))
            .chain(v -> createProfileAttributeClassifications(session, enterprise))
            .map(v -> true)
            .onFailure().invoke(error -> log.error("Error in ProfileMasterInstall update: {}", error.getMessage(), error));
    }

    private Uni<Void> createInvolvedPartyClassifications(Mutiny.Session session, IEnterprise<?,?> enterprise)
    {
        ProfileSystem system = com.guicedee.client.IGuiceContext.get(ProfileSystem.class);
        IEventService<?> eventsService = com.guicedee.client.IGuiceContext.get(IEventService.class);
        IInvolvedPartyService<?> involvedPartyService = com.guicedee.client.IGuiceContext.get(IInvolvedPartyService.class);
        
        // Chain reactive operations - first get the system
        return system.getSystem(session, enterprise)
            .chain(profileSystem -> {
                // Then get the system token
                return system.getSystemToken(session, enterprise)
                    .chain(systemToken -> {
                        // Now use both profileSystem and systemToken for the operations
                        return eventsService.createEventType(session, UserRegistered.toString(), profileSystem, systemToken)
                            .chain(userRegistered -> 
                                eventsService.createEventType(session, VisitorRegistered.toString(), profileSystem, systemToken)
                            )
                            .chain(visitorRegistered -> 
                                involvedPartyService.createIdentificationType(
                                    session, profileSystem,
                                    ProfileIdentificationTypes.IdentificationTypeWebClientUUID,
                                    "The Web Client UUID stored as a device identifier",
                                    systemToken
                                )
                            );
                    });
            })
            .map(result -> null); // Convert to Void
    }

    private Uni<Void> createSiteDetailsClassifications(Mutiny.Session session, IEnterprise<?,?> enterprise)
    {
        IClassificationService<?> classificationService = com.guicedee.client.IGuiceContext.get(IClassificationService.class);
        ProfileSystem system = com.guicedee.client.IGuiceContext.get(ProfileSystem.class);
        
        // Chain reactive operations - first get the system
        return system.getSystem(session, enterprise)
            .chain(profileSystem -> {
                // Now use profileSystem for the operations
                return classificationService.create(session, ClientConnectionDetails, profileSystem)
                    .chain(clientConnectionDetails -> 
                        classificationService.create(session, BrowserDeviceCategory, profileSystem, ClientConnectionDetails)
                    )
                    .chain(browserDeviceCategory -> 
                        classificationService.create(session, OperatingSystemFamily, profileSystem, ClientConnectionDetails)
                    )
                    .chain(operatingSystemFamily -> 
                        classificationService.create(session, BrowserDeviceCategory, profileSystem, ClientConnectionDetails)
                    )
                    .chain(browserDeviceCategory -> 
                        classificationService.create(session, BrowserDevice, profileSystem, ClientConnectionDetails)
                    )
                    .chain(browserDevice -> 
                        classificationService.create(session, BrowserIcon, profileSystem, ClientConnectionDetails)
                    )
                    .chain(browserIcon -> 
                        classificationService.create(session, OperatingSystem, profileSystem, ClientConnectionDetails)
                    );
            })
            .map(result -> null); // Convert to Void
    }

    /**
     * Provisions the FSDM name types used by the comprehensive profile (first name, surname, etc.).
     * Idempotent — {@code createNameType} is find-or-create, so re-running the install is safe and also
     * makes this module self-sufficient when the core name-type setup has not run.
     */
    private Uni<Void> createProfileNameTypes(Mutiny.Session session, IEnterprise<?,?> enterprise)
    {
        ProfileSystem system = com.guicedee.client.IGuiceContext.get(ProfileSystem.class);
        IInvolvedPartyService<?> involvedPartyService = com.guicedee.client.IGuiceContext.get(IInvolvedPartyService.class);

        return system.getSystem(session, enterprise)
            .chain(profileSystem -> system.getSystemToken(session, enterprise)
                .chain(systemToken -> {
                    Uni<Void> chain = Uni.createFrom().voidItem();
                    for (NameTypes nameType : NameTypes.values())
                    {
                        chain = chain.chain(() -> involvedPartyService
                            .createNameType(session, nameType, nameType.classificationDescription(), profileSystem, systemToken)
                            .replaceWithVoid());
                    }
                    return chain;
                }));
    }

    /**
     * Provisions every {@link ProfileAttributes} as a classification under the
     * {@link EnterpriseClassificationDataConcepts#NoClassificationDataConceptName} concept — the same
     * concept the comprehensive profile reads/writes use ({@code addOrUpdateClassification} defaults to
     * it), so the create and find concepts always match. Idempotent (name+concept+enterprise scoped).
     */
    private Uni<Void> createProfileAttributeClassifications(Mutiny.Session session, IEnterprise<?,?> enterprise)
    {
        IClassificationService<?> classificationService = com.guicedee.client.IGuiceContext.get(IClassificationService.class);
        ProfileSystem system = com.guicedee.client.IGuiceContext.get(ProfileSystem.class);

        return system.getSystem(session, enterprise)
            .chain(profileSystem -> system.getSystemToken(session, enterprise)
                .chain(systemToken -> {
                    Uni<Void> chain = Uni.createFrom().voidItem();
                    for (ProfileAttributes attribute : ProfileAttributes.values())
                    {
                        chain = chain.chain(() -> classificationService.create(session,
                                attribute.name(),
                                attribute.classificationDescription(),
                                EnterpriseClassificationDataConcepts.NoClassificationDataConceptName,
                                profileSystem,
                                0,
                                (String) null,
                                systemToken)
                            .replaceWithVoid());
                    }
                    return chain;
                }));
    }
}