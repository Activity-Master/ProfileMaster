package com.guicedee.activitymaster.profiles;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.types.AuthenticationConfiguration;
import com.guicedee.activitymaster.fsdm.client.types.Classifications;
import com.guicedee.activitymaster.fsdm.client.types.structures.Party;
import com.guicedee.activitymaster.fsdm.communicator.endpoints.PartyCall;
import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;
import com.guicedee.activitymaster.profiles.services.interfaces.IRolesService;
import jakarta.cache.annotation.CacheKey;
import jakarta.cache.annotation.CacheResult;

import java.util.*;

import static com.guicedee.activitymaster.profiles.enumerations.ProfileClassifications.*;

public class RolesService
		implements IRolesService<RolesService>
{

	//@Transactional(entityManagerAnnotation = ActivityMasterDB.class)
	@Override
	@CacheResult(cacheName = "UserRolesGetRoles")
	public Set<String> getRoles(@CacheKey IInvolvedParty<?, ?> ip, ISystems<?, ?> systems, java.util.UUID... identityToken)
	{
		Set<String> assignedRoles = new TreeSet<>();
		if (ip == null)
		{
			return new TreeSet<>();
		}
		
		Party party = new PartyCall(new AuthenticationConfiguration()).find(UUID.fromString(ip.getId()), null,UserRoles.toString());
		for (Classifications classification : party.getClassifications())
		{
			assignedRoles.add(classification.getValue());
		}
		if (assignedRoles.isEmpty())
		{
			assignedRoles.add("Guest");
		}
		return assignedRoles;
	}
	
	@Override
	@CacheResult(cacheName = "UserRolesGetRoles",
	             skipGet = true)
	//@Transactional(entityManagerAnnotation = ActivityMasterDB.class)
	public Set<String> addRole(
			@CacheKey IInvolvedParty<?, ?> ip, String role, ProfileServiceDTO<?> dto, ISystems<?, ?> systems, java.util.UUID... identityToken)
	{
		Set<String> roles = getRoles(ip, systems, identityToken);
		if (!roles.contains(role))
		{
			ip.addClassification(UserRoles.toString(), role, systems, identityToken);
			roles.add(role);
		}
		return roles;
	}
	
}
