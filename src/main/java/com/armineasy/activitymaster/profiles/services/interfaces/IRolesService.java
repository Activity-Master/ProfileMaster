package com.armineasy.activitymaster.profiles.services.interfaces;

import com.armineasy.activitymaster.activitymaster.services.dto.ISystems;
import com.armineasy.activitymaster.profiles.dto.ProfileServiceDTO;

import java.util.List;
import java.util.UUID;

public interface IRolesService
{
	List<IUserRole<?>> getRoles(ProfileServiceDTO<?> dto, ISystems<?> systems, UUID... identityToken);
	List<IUserRole<?>> addRole(IUserRole<?> role, ProfileServiceDTO<?> dto, ISystems<?> systems, UUID... identityToken);
	List<IUserRole<?>> findAllRoles();
}
