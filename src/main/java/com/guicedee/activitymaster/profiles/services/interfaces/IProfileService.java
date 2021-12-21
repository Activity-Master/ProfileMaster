package com.guicedee.activitymaster.profiles.services.interfaces;

import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;

import java.util.List;

public interface IProfileService<J extends IProfileService<J>>
{
	String ProfileSystemName = "Profiles Master";
	
	List<ProfileServiceDTO<?>> listUsers(String... roles);
	
	List<ProfileServiceDTO<?>> allUsers();
	
	void clearCache();
}
