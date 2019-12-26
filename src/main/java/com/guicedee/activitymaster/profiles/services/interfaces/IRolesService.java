package com.guicedee.activitymaster.profiles.services.interfaces;

import com.guicedee.activitymaster.core.services.dto.IInvolvedParty;
import com.guicedee.activitymaster.core.services.dto.ISystems;
import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;

import java.util.List;
import java.util.UUID;

public interface IRolesService
{
	List<IUserRole<?>> getRoles(IInvolvedParty<?> ip, ISystems<?> systems, UUID... identityToken);

	List<IUserRole<?>> addRole(IInvolvedParty<?> ip, IUserRole<?> role, ProfileServiceDTO<?> dto, ISystems<?> systems, UUID... identityToken);

	List<IUserRole<?>> findAllRoles();
}
