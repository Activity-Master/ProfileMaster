package com.guicedee.activitymaster.profiles.implementations.updates;

import com.guicedee.activitymaster.fsdm.client.services.IInvolvedPartyService;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.classifications.types.NameTypes;
import com.guicedee.activitymaster.fsdm.client.services.systems.ISystemUpdate;
import com.guicedee.activitymaster.fsdm.client.services.systems.SortedUpdate;
import com.guicedee.activitymaster.profiles.ProfileSystem;
import com.guicedee.activitymaster.profiles.enumerations.ProfileAttributes;
import com.guicedee.activitymaster.profiles.enumerations.ProfileNameRealms;
import com.guicedee.activitymaster.profiles.enumerations.ProfileProtectedFields;
import com.guicedee.client.IGuiceContext;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

/** Installs protected profile identification types for existing and new enterprises. */
@SortedUpdate(sortOrder = 53, taskCount = 1, force = true)
public class ProfileProtectedValuesInstall implements ISystemUpdate
{
	@Override
	public Uni<Boolean> update(Mutiny.StatelessSession session, IEnterprise<?, ?> enterprise)
	{
		IInvolvedPartyService<?> parties = IGuiceContext.get(IInvolvedPartyService.class);
		ProfileSystem profiles = IGuiceContext.get(ProfileSystem.class);
		return profiles.getSystem(session, enterprise)
			.chain(system -> profiles.getSystemToken(session, enterprise).chain(token -> {
				Uni<Void> install = Uni.createFrom().voidItem();
				for (ProfileAttributes attribute : ProfileAttributes.values())
					install = install.chain(() -> parties.createIdentificationType(session, system,
						ProfileProtectedFields.attributeType(attribute.name()), attribute.classificationDescription(), token).replaceWithVoid());
				for (NameTypes name : NameTypes.values())
					install = install.chain(() -> parties.createIdentificationType(session, system,
						ProfileProtectedFields.nameType("Personal", name), "Personal " + name.name(), token).replaceWithVoid());
				for (ProfileNameRealms realm : ProfileNameRealms.values())
					for (NameTypes name : realm.nameTypes())
						install = install.chain(() -> parties.createIdentificationType(session, system,
							ProfileProtectedFields.nameType(realm.realm(), name), realm.realm() + " " + name.name(), token).replaceWithVoid());
				return install;
			})).replaceWith(true);
	}

}
