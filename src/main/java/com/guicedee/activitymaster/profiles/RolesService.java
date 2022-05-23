package com.guicedee.activitymaster.profiles;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;
import com.guicedee.activitymaster.profiles.services.interfaces.IRolesService;
import com.guicedee.activitymaster.profiles.services.interfaces.IUserRole;
import io.github.classgraph.ClassInfo;
import jakarta.cache.annotation.CacheKey;
import jakarta.cache.annotation.CacheResult;

import java.util.Set;
import java.util.TreeSet;

import static com.guicedee.activitymaster.profiles.enumerations.ProfileClassifications.*;
import static com.guicedee.guicedinjection.GuiceContext.*;

public class RolesService
		implements IRolesService<RolesService>
{
	@Override
	@CacheResult(cacheName = "UserRolesGetRoles")
	public Set<String> getRoles(@CacheKey IInvolvedParty<?, ?> ip, ISystems<?, ?> systems, java.util.UUID... identityToken)
	{
		Set<String> assignedRoles = new TreeSet<>();
		if (systems == null)
		{
			systems = get(ProfileSystem.class).getSystem(ip.getOriginalSourceSystemID()
			                                               .getEnterprise());
		}
		if (ip == null)
		{
			return new TreeSet<>();
		}
		for (Object[] classifications2 : ip.builder()
		                                   .getClassificationsValuePivot(UserRoles.toString(), ip.getId() + "", systems, identityToken))
		{
			assignedRoles.add(classifications2[1].toString());
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
	
	@CacheResult(cacheName = "RolesServiceFindAllRoles")
	@Override
	public Set<String> findAllRoles()
	{
		Set<String> roles = new TreeSet<>();
		for (ClassInfo classInfo : instance().getScanResult()
		                                     .getClassesImplementing(IUserRole.class.getCanonicalName()))
		{
			for (Object enumConstant : classInfo.loadClass()
			                                    .getEnumConstants())
			{
				IUserRole<?> role = (IUserRole<?>) enumConstant;
				roles.add(role.toString());
			}
		}
		return roles;
	}
	
	
}
