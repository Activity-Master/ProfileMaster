package com.guicedee.activitymaster.profiles;

import com.google.inject.Singleton;
import com.guicedee.activitymaster.core.services.dto.IInvolvedParty;
import com.guicedee.activitymaster.core.services.dto.ISystems;
import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;
import com.guicedee.activitymaster.profiles.services.interfaces.IRolesService;
import com.guicedee.activitymaster.profiles.services.interfaces.IUserRole;
import io.github.classgraph.ClassInfo;

import javax.cache.annotation.CacheKey;
import javax.cache.annotation.CacheResult;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static com.guicedee.activitymaster.profiles.enumerations.ProfileClassifications.*;
import static com.guicedee.guicedinjection.GuiceContext.*;

@Singleton
public class RolesService
		implements IRolesService<RolesService>
{
	private static final Logger log = Logger.getLogger(RolesService.class.getName());

	@Override
	//@CacheResult(cacheName = "UserRolesGetRoles")
	public List<IUserRole<?>> getRoles(@CacheKey IInvolvedParty<?> ip, ISystems<?> systems, @CacheKey UUID... identityToken)
	{
		List<IUserRole<?>> roles = findAllRoles();
		List<IUserRole<?>> myRoles = new ArrayList<>();
		List<String> assignedRoles = new ArrayList<>();
		if (systems == null)
		{
			systems = get(ProfileSystem.class).getSystem(ip.getEnterprise());
		}
		if (ip == null)
		{
			return new ArrayList<>();
		}
		for (Object classifications2 : ip.getValues(UserRoles, null, systems, identityToken))
		{
			assignedRoles.add(classifications2.toString());
		}
		for (String assignedRole : assignedRoles)
		{
			for (IUserRole<?> role : roles)
			{
				if (role.toString()
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
	public List<IUserRole<?>> addRole(
			@CacheKey IInvolvedParty<?> ip, IUserRole<?> role, ProfileServiceDTO<?> dto, ISystems<?> systems, @CacheKey UUID... identityToken)
	{
		List<IUserRole<?>> roles = getRoles(ip, systems, identityToken);
		if (!roles.contains(role))
		{
			ip.add(UserRoles, role.toString(), systems, identityToken);
			roles.add(role);
		}
		return roles;
	}

	@CacheResult(cacheName = "RolesServiceFindAllRoles")
	@Override
	public List<IUserRole<?>> findAllRoles()
	{
		List<IUserRole<?>> roles = new ArrayList<>();
		for (ClassInfo classInfo : instance().getScanResult()
		                                     .getClassesImplementing(IUserRole.class.getCanonicalName()))
		{
			for (Object enumConstant : classInfo.loadClass()
			                                    .getEnumConstants())
			{
				IUserRole<?> role = (IUserRole<?>) enumConstant;
				roles.add(role);
			}
		}
		return roles;
	}
}
