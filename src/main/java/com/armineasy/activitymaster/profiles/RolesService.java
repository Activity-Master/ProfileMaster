package com.armineasy.activitymaster.profiles;

import com.armineasy.activitymaster.activitymaster.db.entities.involvedparty.InvolvedParty;
import com.armineasy.activitymaster.activitymaster.db.entities.involvedparty.InvolvedPartyXClassification;
import com.armineasy.activitymaster.activitymaster.services.dto.ISystems;
import com.armineasy.activitymaster.activitymaster.services.system.IInvolvedPartyService;
import com.armineasy.activitymaster.activitymaster.services.types.IdentificationTypes;
import com.armineasy.activitymaster.profiles.dto.UserDTO;
import com.armineasy.activitymaster.profiles.enumerations.ProfileClassifications;
import com.armineasy.activitymaster.profiles.services.interfaces.IRolesService;
import com.armineasy.activitymaster.profiles.services.interfaces.IUserRole;
import com.google.inject.Singleton;
import lombok.extern.java.Log;

import javax.cache.annotation.CacheKey;
import javax.cache.annotation.CachePut;
import javax.cache.annotation.CacheResult;
import java.util.List;
import java.util.UUID;

import static com.jwebmp.guicedinjection.GuiceContext.*;

@Singleton
@Log
public class RolesService
		implements IRolesService
{
	@Override
	@CacheResult(cacheName = "UserRolesService")
	public List<IUserRole<?>> getRoles(@CacheKey UserDTO<?> dto, @CacheKey ISystems systems, @CacheKey UUID... identityToken)
	{
		return null;
	}

	@Override
	@CachePut(cacheName = "UserRolesService")
	@CacheResult(cacheName = "UserRolesService")
	public List<IUserRole<?>> addRole(IUserRole<?> role, @CacheKey UserDTO<?> dto, @CacheKey ISystems systems, @CacheKey UUID... identityToken)
	{
		//List<IUserRole<?>>
		InvolvedParty ip = get(IInvolvedPartyService.class).findByIdentificationType(IdentificationTypes.IdentificationTypeUUID, dto.getIdentityToken()
		                                                                                                                       .toString(), systems, identityToken);
		if(ip.has(ProfileClassifications.UserRoles, systems, identityToken))
		{
			for (InvolvedPartyXClassification classification : ip.findAll(ProfileClassifications.UserRoles, systems, identityToken))
			{

			}
		}
		return null;
	}
}
