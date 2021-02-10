package com.guicedee.activitymaster.profiles.implementations;

import com.guicedee.activitymaster.core.services.dto.IEnterprise;
import com.guicedee.activitymaster.profiles.ProfileService;
import com.guicedee.activitymaster.profiles.RolesService;
import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;
import com.guicedee.activitymaster.profiles.dto.UserSecurityDTO;
import com.guicedee.activitymaster.profiles.services.interfaces.IProfileService;
import com.guicedee.activitymaster.profiles.services.interfaces.IRolesService;
import com.google.inject.PrivateModule;
import com.guicedee.guicedinjection.interfaces.IGuiceModule;

public class ProfileServiceBinder
		extends PrivateModule
		implements IGuiceModule<ProfileServiceBinder>
{
	@Override
	protected void configure()
	{
		bind(IProfileService.class).to(ProfileService.class);
		expose(IProfileService.class);
		
		bind(IRolesService.class).to(RolesService.class);
		expose(IRolesService.class);
		
		bind(ProfileServiceDTO.class).toProvider(ProfileServiceDTOProvider.class);
		expose(ProfileServiceDTO.class);
		
		bind(UserSecurityDTO.class).toProvider(UserSecurityProvider.class);
		expose(UserSecurityDTO.class);
		
		bind(IEnterprise.class).toProvider(EnterpriseProvider.class);
		expose(IEnterprise.class);
		
	}
}
