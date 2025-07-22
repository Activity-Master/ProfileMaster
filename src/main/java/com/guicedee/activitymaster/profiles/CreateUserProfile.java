package com.guicedee.activitymaster.profiles;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.fsdm.client.services.events.IOnCreateUser;
import io.smallrye.mutiny.Uni;

public class CreateUserProfile implements IOnCreateUser<CreateUserProfile>
{
	@Override
	public Uni<IInvolvedParty<?, ?>> createUser(String domain, String username, String password)
	{
		// This method should be implemented to create a user profile
		// For now, return an empty Uni
		return Uni.createFrom().nullItem();
	}
	
}
