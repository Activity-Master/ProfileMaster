package com.guicedee.activitymaster.profiles.implementations;

import com.guicedee.activitymaster.profiles.ProfileService;
import com.guicedee.activitymaster.profiles.RolesService;
import com.guicedee.activitymaster.profiles.services.interfaces.IProfileService;
import com.guicedee.activitymaster.profiles.services.interfaces.IRolesService;
import com.google.inject.PrivateModule;
import com.guicedee.guicedinjection.interfaces.IGuiceModule;

public class ProfileServiceBinder extends PrivateModule
		implements IGuiceModule<ProfileServiceBinder>
{
	@Override
	protected void configure()
	{
		bind(IProfileService.class).to(ProfileService.class);
		expose(IProfileService.class);

		bind(IRolesService.class).to(RolesService.class);
		expose(IRolesService.class);
	}
}
