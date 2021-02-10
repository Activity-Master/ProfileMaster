package com.guicedee.activitymaster.profiles.services.interfaces;

import com.guicedee.activitymaster.core.services.dto.IInvolvedParty;
import com.guicedee.activitymaster.core.services.dto.ISystems;
import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface IRolesService<J extends IRolesService<J>>
{
	Set<String> getRoles(IInvolvedParty<?> ip, ISystems<?> systems, UUID... identityToken);
	
	Set<String> addRole(IInvolvedParty<?> ip, String role, ProfileServiceDTO<?> dto, ISystems<?> systems, UUID... identityToken);
	
	Set<String> findAllRoles();
}
