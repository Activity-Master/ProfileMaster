package com.guicedee.activitymaster.profiles;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.guicedee.activitymaster.fsdm.client.services.IPasswordsService;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;
import com.guicedee.activitymaster.profiles.services.interfaces.IProfileService;

import javax.cache.annotation.CacheRemove;
import javax.cache.annotation.CacheResult;
import java.util.ArrayList;
import java.util.List;

import static com.guicedee.activitymaster.fsdm.client.services.IActivityMasterService.*;
import static com.guicedee.activitymaster.fsdm.client.services.classifications.DefaultClassifications.*;
import static com.guicedee.activitymaster.profiles.enumerations.ProfileIdentificationTypes.*;

@SuppressWarnings("Duplicates")
public class ProfileService
		implements IProfileService<ProfileService>
{
	@Inject
	private IPasswordsService<?> passwordsService;
	
	//@Transactional()
	@Override
	public List<ProfileServiceDTO<?>> listUsers(String... roles)
	{
		List<ProfileServiceDTO<?>> users = allUsers();
		List<ProfileServiceDTO<?>> filtered = new ArrayList<>();
		for (ProfileServiceDTO<?> user : users)
		{
			for (String role : roles)
			{
				if (user.findRoles()
				        .contains(role))
				{
					filtered.add(user);
				}
			}
		}
		return users;
	}
	
	//@CacheResult(cacheName = "UserProfiles")
	@Override
	//@Transactional()
	public List<ProfileServiceDTO<?>> allUsers()
	{
		List<ProfileServiceDTO<?>> output = new ArrayList<>();
		var allIds = passwordsService.getAllUsers(getISystem(ProfileSystemName), getISystemToken(ProfileSystemName));
		
		for (IInvolvedParty<?, ?> allId : allIds)
		{
			ProfileServiceDTO<?> profileServiceDTO = new ProfileServiceDTO<>();
			var idType = allId.findInvolvedPartyIdentificationType(NoClassification, IdentificationTypeWebClientUUID, null, getISystem(ProfileSystemName),
					true, true, getISystemToken(ProfileSystemName));
			if (idType.isPresent())
			{
				profileServiceDTO.setWebClientUUID(idType.get()
				                                         .getValueAsUUID());
			}
			profileServiceDTO.setInvolvedParty(allId);
			output.add(profileServiceDTO);
		}
		return output;
	}
	
	@CacheRemove(cacheName = "UserProfiles")
	@Override
	public void clearCache()
	{
	
	}
	
}
