package com.armineasy.activitymaster.profiles.implementations;

import com.armineasy.activitymaster.profiles.ProfileService;
import com.armineasy.activitymaster.profiles.RolesService;
import com.armineasy.activitymaster.profiles.services.interfaces.IProfileService;
import com.armineasy.activitymaster.profiles.services.interfaces.IRolesService;
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
