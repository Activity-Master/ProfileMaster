package com.guicedee.activitymaster.profiles.implementations;

import com.google.inject.*;
import com.guicedee.activitymaster.profiles.ProfileService;
import com.guicedee.activitymaster.profiles.RolesService;
import com.guicedee.activitymaster.profiles.services.interfaces.IProfileService;
import com.guicedee.activitymaster.profiles.services.interfaces.IRolesService;
import com.guicedee.guicedinjection.interfaces.IGuiceModule;

public class ProfileServiceBinder
		extends PrivateModule
		implements IGuiceModule<ProfileServiceBinder>
{
	@Override
	protected void configure()
	{
		@SuppressWarnings("Convert2Diamond")
		Key<IProfileService<?>> genericKey = Key.get(new TypeLiteral<IProfileService<?>>() {});
		@SuppressWarnings("Convert2Diamond")
		Key<IProfileService<ProfileService>> realKey
				= Key.get(new TypeLiteral<IProfileService<ProfileService>>() {});
		
		bind(genericKey).to(realKey);
		bind(realKey).to(ProfileService.class);
		bind(IProfileService.class).to(genericKey);
		
		expose(genericKey);
		expose(IProfileService.class);
		
		
		@SuppressWarnings("Convert2Diamond")
		Key<IRolesService<?>> genericKeyRoles = Key.get(new TypeLiteral<IRolesService<?>>() {});
		@SuppressWarnings("Convert2Diamond")
		Key<IRolesService<RolesService>> realKeyRoles
				= Key.get(new TypeLiteral<IRolesService<RolesService>>() {});
		
		bind(genericKeyRoles).to(realKeyRoles);
		bind(realKeyRoles).to(RolesService.class);
		bind(IRolesService.class).to(genericKeyRoles);
		
		expose(genericKeyRoles);
		expose(IRolesService.class);
	}
}
