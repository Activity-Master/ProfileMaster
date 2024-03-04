package com.guicedee.activitymaster.profiles.implementations.providers;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.name.Names;
import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;

import java.util.UUID;


public class ProfileServiceDTOProvider
		implements Provider<ProfileServiceDTO<?>>
{
	@Override
	public ProfileServiceDTO<?> get()
	{
		UUID localStorageKey = com.guicedee.client.IGuiceContext.get(Key.get(UUID.class, Names.named("localstorage")));
		if (localStorageKey == null)
		{
			return null;
		}
		
		ProfileServiceDTO<?> pro = new ProfileServiceDTO<>();
		com.guicedee.client.IGuiceContext.instance().inject()
		            .injectMembers(pro);
		
		pro.setWebClientUUID(localStorageKey);
		return pro;
	}
}
