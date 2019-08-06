package com.armineasy.activitymaster.profiles;

import com.armineasy.activitymaster.activitymaster.services.dto.IEnterprise;
import com.armineasy.activitymaster.activitymaster.services.dto.IInvolvedParty;
import com.armineasy.activitymaster.activitymaster.services.dto.ISystems;
import com.armineasy.activitymaster.activitymaster.services.system.IInvolvedPartyService;
import com.armineasy.activitymaster.activitymaster.services.types.IdentificationTypes;
import com.armineasy.activitymaster.profiles.dto.ProfileServiceDTO;
import com.armineasy.activitymaster.profiles.services.interfaces.IRolesService;
import com.armineasy.activitymaster.profiles.services.interfaces.IUserRole;
import com.google.inject.Singleton;
import io.github.classgraph.ClassInfo;

import javax.cache.annotation.CacheKey;
import javax.cache.annotation.CacheRemoveAll;
import javax.cache.annotation.CacheResult;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static com.armineasy.activitymaster.profiles.enumerations.ProfileClassifications.*;
import static com.jwebmp.guicedinjection.GuiceContext.*;

@Singleton
public class RolesService
		implements IRolesService
{
	private static final Logger log = Logger.getLogger(RolesService.class.getName());

	@Override
	@CacheResult(cacheName = "UserRolesGetRoles")
	public List<IUserRole<?>> getRoles(@CacheKey IInvolvedParty<?> ip,@CacheKey ProfileServiceDTO<?> dto, @CacheKey ISystems<?> systems, @CacheKey UUID... identityToken)
	{
		List<IUserRole<?>> roles = findAllRoles();
		List<IUserRole<?>> myRoles = new ArrayList<>();
		List<String> assignedRoles = new ArrayList<>();
		for (Object classifications2 : ip.getValues(UserRoles,null, systems, identityToken[0]))
		{
			assignedRoles.add(classifications2.toString());
		}
		for (String assignedRole : assignedRoles)
		{
			for (IUserRole<?> role : roles)
			{
				if(role.toString().equalsIgnoreCase(assignedRole))
				{
					myRoles.add(role);
				}
			}
		}

		dto.setRoles(new HashSet<>(myRoles));
		return myRoles;
	}

	@Override
	@CacheRemoveAll(cacheName = "UserRolesGetRoles")
	public List<IUserRole<?>> addRole( @CacheKey IInvolvedParty<?> ip,IUserRole<?> role, @CacheKey ProfileServiceDTO<?> dto, @CacheKey ISystems<?> systems, @CacheKey UUID... identityToken)
	{
		List<IUserRole<?>> roles = getRoles(ip,dto, systems, identityToken);
		if(!roles.contains(role))
		{
			ip.add(UserRoles, role.toString(), systems, identityToken);
			roles.add(role);
		}
		return getRoles(ip,dto,systems,identityToken);
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
