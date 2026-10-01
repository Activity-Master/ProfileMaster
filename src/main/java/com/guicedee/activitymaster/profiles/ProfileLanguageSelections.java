package com.guicedee.activitymaster.profiles;

import com.google.inject.Inject;
import com.guicedee.activitymaster.fsdm.client.services.*;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications.IClassification;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.db.entities.classifications.Classification;
import com.guicedee.activitymaster.fsdm.db.entities.involvedparty.*;
import com.guicedee.activitymaster.profiles.enumerations.ProfileLanguageCatalog;
import com.guicedee.activitymaster.profiles.enumerations.ProfileProtectedFields;
import com.guicedee.activitymaster.profiles.webdto.ProfileAttributeChoiceDTO;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;
import java.util.*;
import static com.guicedee.activitymaster.fsdm.client.services.builders.IQueryBuilderSCD.*;

/** Classified, encrypted language selections on the caller's stateless transaction. */
public class ProfileLanguageSelections {
    @Inject private IClassificationService<?> classifications;
    @Inject private IInvolvedPartyService<?> parties;
    @Inject private IActiveFlagService<?> flags;
    @Inject private ISecurityTokenService<?> security;

    private Uni<List<InvolvedPartyXInvolvedPartyIdentificationType>> links(Mutiny.StatelessSession session,
            IInvolvedParty<?, ?> party, String attribute, ISystems<?, ?> system, UUID token) {
        return new InvolvedPartyXInvolvedPartyIdentificationType().builder(session)
            .withEnterprise(system.getEnterprise()).findLink((InvolvedParty) party, null, null)
            .withType(ProfileProtectedFields.attributeType(attribute), system, token)
            .inActiveRange().inDateRange().canRead(system, token).getAll();
    }

    public Uni<Void> replace(Mutiny.StatelessSession session, IInvolvedParty<?, ?> party, String attribute,
            String value, List<ProfileAttributeChoiceDTO> published, ISystems<?, ?> system, UUID token) {
        final List<String> selected;
        try {
            selected = ProfileLanguageCatalog.parse(attribute, value);
            if (selected.stream().anyMatch(name -> published.stream().noneMatch(choice -> choice.value().equals(name))))
                throw new IllegalArgumentException("Choose a published language");
        } catch (IllegalArgumentException invalid) { return Uni.createFrom().failure(invalid); }
        // Resolve every classification before retiring old data. No writes to the catalogue from user input.
        List<IClassification<?, ?>> resolved = new ArrayList<>();
        Uni<Void> validate = Uni.createFrom().voidItem();
        for (String name : selected)
            validate = validate.chain(() -> classifications.findInConcept(session, name, ProfileLanguageCatalog.CONCEPT, system, token)
                .invoke(resolved::add).replaceWithVoid());
        return validate.chain(() -> links(session, party, attribute, system, token)).chain(existing -> {
            Uni<Void> writes = Uni.createFrom().voidItem();
            for (var link : existing) writes = writes.chain(() -> link.archive(session, system, token).replaceWithVoid());
            for (var language : resolved) writes = writes.chain(() -> insert(session, party, attribute, language, system, token));
            return writes;
        });
    }

    private Uni<Void> insert(Mutiny.StatelessSession session, IInvolvedParty<?, ?> party, String attribute,
            IClassification<?, ?> language, ISystems<?, ?> system, UUID token) {
        return parties.findInvolvedPartyIdentificationType(session, ProfileProtectedFields.attributeType(attribute), system, token)
            .chain(type -> flags.getActiveFlag(session, system.getEnterprise(), token).chain(active -> {
                var link = new InvolvedPartyXInvolvedPartyIdentificationType();
                link.setId(UUID.randomUUID());
                link.setEnterpriseID(system.getEnterprise());
                link.setSystemID(system);
                link.setOriginalSourceSystemID(system.getId());
                link.setOriginalSourceSystemUniqueID(new UUID(0, 0));
                link.setInvolvedPartyID((InvolvedParty) party);
                link.setInvolvedPartyIdentificationTypeID((InvolvedPartyIdentificationType) type);
                link.setClassificationID((Classification) language);
                link.setActiveFlagID(active);
                link.setEffectiveFromDate(convertToUTCDateTime(com.entityassist.RootEntity.getNow()));
                link.setEffectiveToDate(EndOfTime.atOffset(java.time.ZoneOffset.UTC));
                link.setValue(language.getName());
                return session.insert(link).chain(() -> security.resolveDefaultGroupFolderTokens(session, system, token)
                    .chain(tokens -> link.createScopeRestrictedSecurity(session, system, system.getEnterprise(), active, tokens, null, token)))
                    .replaceWithVoid();
            }));
    }

    /** Null leaves an untouched legacy scalar readable until its owner explicitly replaces it. */
    public Uni<String> read(Mutiny.StatelessSession session, IInvolvedParty<?, ?> party, String attribute,
            ISystems<?, ?> system, UUID token) {
        return links(session, party, attribute, system, token).chain(existing -> {
            List<String> names = new ArrayList<>();
            Uni<Void> reads = Uni.createFrom().voidItem();
            for (var link : existing) reads = reads.chain(() -> session.fetch(link.getClassificationID())
                .chain(language -> "NoClassification".equals(language.getName()) ? Uni.createFrom().voidItem()
                    : classifications.findInConcept(session, language.getName(), ProfileLanguageCatalog.CONCEPT, system, token)
                        .invoke(scoped -> {
                            if (!scoped.getId().equals(language.getId())) throw new IllegalStateException("Language concept mismatch");
                            names.add(scoped.getName());
                        }).replaceWithVoid()));
            return reads.replaceWith(() -> names.isEmpty() ? null : String.join(",", names.stream().distinct().sorted().toList()));
        });
    }
}
