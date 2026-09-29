package com.guicedee.activitymaster.profiles.implementations.updates;

import com.guicedee.activitymaster.fsdm.client.services.IClassificationService;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts;
import com.guicedee.activitymaster.fsdm.client.services.systems.ISystemUpdate;
import com.guicedee.activitymaster.fsdm.client.services.systems.SortedUpdate;
import com.guicedee.activitymaster.profiles.ProfileSystem;
import com.guicedee.activitymaster.profiles.enumerations.ProfileNameRealms;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.reactive.mutiny.Mutiny;

/**
 * Creates the name-link classifications that separate social and work names from personal names.
 * A separate update so enterprises that already applied {@link ProfileMasterInstall} receive it.
 */
@SortedUpdate(sortOrder = 51, taskCount = 1)
public class ProfileNameRealmsInstall implements ISystemUpdate
{
    private static final Logger log = LogManager.getLogger(ProfileNameRealmsInstall.class);

    @Override
    public Uni<Boolean> update(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise)
    {
        IClassificationService<?> classificationService = com.guicedee.client.IGuiceContext.get(IClassificationService.class);
        ProfileSystem system = com.guicedee.client.IGuiceContext.get(ProfileSystem.class);

        return system.getSystem(session, enterprise)
            .chain(profileSystem -> system.getSystemToken(session, enterprise)
                .chain(systemToken -> Multi.createFrom().items(ProfileNameRealms.values())
                    .onItem().transformToUniAndConcatenate(realm -> classificationService.create(session,
                            realm.name(),
                            realm.classificationDescription(),
                            EnterpriseClassificationDataConcepts.InvolvedPartyXInvolvedPartyNameType,
                            profileSystem,
                            systemToken))
                    .collect().last()))
            .map(result -> true)
            .onFailure().invoke(error -> log.error("Error creating profile name realms (stateless): {}", error.getMessage(), error));
    }
}
