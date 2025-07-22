package com.guicedee.activitymaster.profiles.services.interfaces;

import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface IProfileService<J extends IProfileService<J>>
{
	String ProfileSystemName = "Profiles Master";
	
	Uni<List<ProfileServiceDTO<?>>> listUsers(String... roles);
	
	Uni<List<ProfileServiceDTO<?>>> allUsers();
	
	Uni<Void> clearCache();
}
