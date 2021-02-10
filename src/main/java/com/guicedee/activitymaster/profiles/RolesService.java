package com.guicedee.activitymaster.profiles;

import com.google.inject.Singleton;
import com.guicedee.activitymaster.core.services.dto.IInvolvedParty;
import com.guicedee.activitymaster.core.services.dto.ISystems;
import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;
import com.guicedee.activitymaster.profiles.services.interfaces.IRolesService;
import com.guicedee.activitymaster.profiles.services.interfaces.IUserRole;
import io.github.classgraph.ClassInfo;

import jakarta.cache.annotation.CacheKey;
import jakarta.cache.annotation.CacheResult;

import java.util.*;
import java.util.logging.Logger;

import static com.guicedee.activitymaster.profiles.enumerations.ProfileClassifications.*;
import static com.guicedee.guicedinjection.GuiceContext.*;

public class RolesService
		implements IRolesService<RolesService>
{
	@Override
	@CacheResult(cacheName = "UserRolesGetRoles")
	public Set<String> getRoles(@CacheKey IInvolvedParty<?> ip, ISystems<?> systems, @CacheKey UUID... identityToken)
	{
		Set<String> roles = findAllRoles();
		Set<String> myRoles = new TreeSet<>();
		Set<String> assignedRoles = new TreeSet<>();
		if (systems == null)
		{
			systems = get(ProfileSystem.class).getSystem(ip.getEnterprise());
		}
		if (ip == null)
		{
			return new TreeSet<>();
		}
		for (Object classifications2 : ip.getValues(UserRoles, null, systems, identityToken))
		{
			assignedRoles.add(classifications2.toString());
		}
		for (String assignedRole : assignedRoles)
		{
			for (String role : roles)
			{
				if (role
				        .equalsIgnoreCase(assignedRole))
				{
					myRoles.add(role);
				}
			}
		}
		return myRoles;
	}
	
	@Override
	@CacheResult(cacheName = "UserRolesGetRoles",
	             skipGet = true)
	public Set<String> addRole(
			@CacheKey IInvolvedParty<?> ip, String role, ProfileServiceDTO<?> dto, ISystems<?> systems, @CacheKey UUID... identityToken)
	{
		Set<String> roles = getRoles(ip, systems, identityToken);
		if (!roles.contains(role))
		{
			ip.add(UserRoles, role, systems, identityToken);
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
