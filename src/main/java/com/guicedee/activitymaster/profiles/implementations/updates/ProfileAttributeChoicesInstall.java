package com.guicedee.activitymaster.profiles.implementations.updates;

import com.guicedee.activitymaster.fsdm.client.services.IClassificationService;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts;
import com.guicedee.activitymaster.fsdm.client.services.systems.ISystemUpdate;
import com.guicedee.activitymaster.fsdm.client.services.systems.SortedUpdate;
import com.guicedee.activitymaster.profiles.ProfileSystem;
import com.guicedee.activitymaster.profiles.enumerations.ProfileAttributeChoices;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.reactive.mutiny.Mutiny;

/**
 * Installs the default choices for gender, pronouns and marital status as child classifications of
 * their attribute classifications (created by {@link ProfileMasterInstall}). Idempotent, and a separate
 * update so enterprises that already applied the earlier profile updates receive it.
 */
@SortedUpdate(sortOrder = 52, taskCount = 1,force = true)
public class ProfileAttributeChoicesInstall implements ISystemUpdate
{
	private static final Logger log = LogManager.getLogger(ProfileAttributeChoicesInstall.class);

	@Override
	public Uni<Boolean> update(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise)
	{
		IClassificationService<?> classificationService = com.guicedee.client.IGuiceContext.get(IClassificationService.class);
		ProfileSystem system = com.guicedee.client.IGuiceContext.get(ProfileSystem.class);

		return system.getSystem(session, enterprise)
			.chain(profileSystem -> system.getSystemToken(session, enterprise)
				// One statement at a time on the stateless connection.
				.chain(systemToken -> Multi.createFrom().items(ProfileAttributeChoices.values())
					.onItem().transformToUniAndConcatenate(choice -> classificationService
						.find(session, choice.attribute().name(), profileSystem, systemToken)
						.chain(parent -> classificationService.create(session,
							choice.name(),
							choice.label(),
							EnterpriseClassificationDataConcepts.NoClassificationDataConceptName,
							profileSystem,
							choice.sequence(),
							(IClassification<?, ?>) parent,
							systemToken)))
					.collect().last()))
			.map(result -> true)
			.onFailure().invoke(error -> log.error("Error creating profile attribute choices (stateless): {}", error.getMessage(), error));
	}
}
