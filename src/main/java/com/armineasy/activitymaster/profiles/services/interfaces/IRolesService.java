package com.armineasy.activitymaster.profiles.services.interfaces;

import com.armineasy.activitymaster.activitymaster.services.dto.ISystems;
import com.armineasy.activitymaster.profiles.dto.UserDTO;
import com.armineasy.activitymaster.profiles.services.interfaces.IUserRole;

import javax.cache.annotation.CacheKey;
import javax.cache.annotation.CachePut;
import javax.cache.annotation.CacheResult;
import java.util.List;
import java.util.UUID;

public interface IRolesService
{
	@CacheResult(cacheName = "UserRolesService")
	List<IUserRole<?>> getRoles(@CacheKey UserDTO<?> dto, @CacheKey ISystems systems, @CacheKey UUID... identityToken);

	@CachePut(cacheName = "UserRolesService")
	@CacheResult(cacheName = "UserRolesService")
	List<IUserRole<?>> addRole(IUserRole<?> role, @CacheKey UserDTO<?> dto, @CacheKey ISystems systems, @CacheKey UUID... identityToken);
}
