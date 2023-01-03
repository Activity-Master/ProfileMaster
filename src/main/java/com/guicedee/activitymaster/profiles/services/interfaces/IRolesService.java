package com.guicedee.activitymaster.profiles.services.interfaces;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;

import java.util.Set;


public interface IRolesService<J extends IRolesService<J>>
{
	String USER_ROLES_SESSION_NAME = "user-roles";
	
	Set<String> getRoles(IInvolvedParty<?,?> ip, ISystems<?,?> systems, java.util.UUID... identityToken);
	
	Set<String> addRole(IInvolvedParty<?,?> ip, String role, ProfileServiceDTO<?> dto, ISystems<?,?> systems, java.util.UUID... identityToken);
	
}
