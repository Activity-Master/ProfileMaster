package com.guicedee.activitymaster.profiles.implementations.updates;

import com.guicedee.activitymaster.fsdm.client.services.IClassificationService;
import com.guicedee.activitymaster.fsdm.client.services.IClassificationDataConceptService;
import com.guicedee.activitymaster.fsdm.client.services.IInvolvedPartyService;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.systems.ISystemUpdate;
import com.guicedee.activitymaster.fsdm.client.services.systems.SortedUpdate;
import com.guicedee.activitymaster.profiles.ProfileSystem;
import com.guicedee.activitymaster.profiles.enumerations.ProfileAttributeChoices;
import com.guicedee.activitymaster.profiles.enumerations.ProfileChoiceConcepts;
import com.guicedee.activitymaster.profiles.enumerations.ProfileLanguageCatalog;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.reactive.mutiny.Mutiny;

/**
 * Installs profile-owned concepts, typed links, and default dropdown classifications under their
 * attribute parents (created by {@link ProfileMasterInstall}). Repeated installs retain custom choices.
 */
@SortedUpdate(sortOrder = 52, taskCount = 1,force = true)
public class ProfileAttributeChoicesInstall implements ISystemUpdate
{
	private static final Logger log = LogManager.getLogger(ProfileAttributeChoicesInstall.class);

	@Override
	public Uni<Boolean> update(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise)
	{
		IClassificationService<?> classificationService = com.guicedee.client.IGuiceContext.get(IClassificationService.class);
		IClassificationDataConceptService<?> dataConceptService = com.guicedee.client.IGuiceContext.get(IClassificationDataConceptService.class);
		IInvolvedPartyService<?> partyService = com.guicedee.client.IGuiceContext.get(IInvolvedPartyService.class);
		ProfileSystem system = com.guicedee.client.IGuiceContext.get(ProfileSystem.class);

		return system.getSystem(session, enterprise)
			.chain(profileSystem -> system.getSystemToken(session, enterprise)
				.chain(systemToken -> Multi.createFrom().items(ProfileChoiceConcepts.values())
					.onItem().transformToUniAndConcatenate(concept -> concept.linkKind() == ProfileChoiceConcepts.LinkKind.NAME
						? partyService.createNameType(session, concept.linkTypeName(), "Selected " + concept.attribute().name(), profileSystem, systemToken)
						: partyService.createType(session, profileSystem, concept.linkTypeName(), "Selected " + concept.attribute().name(), systemToken))
					.collect().last()
					.chain(() -> Multi.createFrom().items(ProfileChoiceConcepts.values())
					.onItem().transformToUniAndConcatenate(concept -> dataConceptService.createNamedDataConcept(
						session, concept.conceptName(), "Profile choices for " + concept.attribute().name(), profileSystem, systemToken))
					.collect().last()
					.chain(() -> Multi.createFrom().items(ProfileAttributeChoices.values())
					.onItem().transformToUniAndConcatenate(choice -> classificationService
						.find(session, choice.attribute().name(), profileSystem, systemToken)
						.chain(parent -> classificationService.createInConcept(session,
							choice.name(),
							choice.label(),
							ProfileChoiceConcepts.forAttribute(choice.attribute()).conceptName(),
							profileSystem,
							choice.sequence(),
							(IClassification<?, ?>) parent,
							systemToken)
                            .chain(child -> session.createQuery("update Classification set description = :label where id = :id and enterpriseID.id = :enterprise")
                                .setParameter("label", choice.label()).setParameter("id", child.getId())
                                .setParameter("enterprise", enterprise.getId()).executeUpdate())))
					.collect().last()
                    .chain(() -> Multi.createFrom().iterable(ProfileLanguageCatalog.DEFAULTS)
                        .onItem().transformToUniAndConcatenate(language -> classificationService
                            .find(session, "HomeLanguage", profileSystem, systemToken)
                            .chain(parent -> classificationService.createInConcept(session, language.value(), language.label(),
                                ProfileLanguageCatalog.CONCEPT, profileSystem, 1, parent, systemToken))
                            .chain(child -> classificationService.find(session, "SpokenLanguages", profileSystem, systemToken)
                                .chain(parent -> classificationService.createInConcept(session, language.value(), language.label(),
                                    ProfileLanguageCatalog.CONCEPT, profileSystem, 1, parent, systemToken))))
                        .collect().last())))))
			.map(result -> true)
			.onFailure().invoke(error -> log.error("Error creating profile attribute choices (stateless): {}", error.getMessage(), error));
	}
}
