package com.guicedee.activitymaster.profiles;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.fsdm.client.services.events.IOnCreateUser;

public class CreateUserProfile implements IOnCreateUser<CreateUserProfile>
{
	@Override
	public IInvolvedParty<?, ?> createUser(String domain, String username, String password)
	{
		return null;
	}
	
}
