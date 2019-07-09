package com.armineasy.activitymaster.profiles.services.interfaces;

import com.armineasy.activitymaster.activitymaster.services.classifications.enterprise.IEnterpriseName;
import com.armineasy.activitymaster.activitymaster.services.dto.IEnterprise;
import com.armineasy.activitymaster.activitymaster.services.dto.IInvolvedParty;
import com.armineasy.activitymaster.activitymaster.services.dto.ISystems;
import com.armineasy.activitymaster.activitymaster.services.system.IInvolvedPartyService;
import com.armineasy.activitymaster.profiles.ProfileSystem;
import com.armineasy.activitymaster.profiles.dto.ProfileServiceDTO;
import com.armineasy.activitymaster.profiles.dto.UserConfirmationKeyDTO;
import com.armineasy.activitymaster.profiles.dto.UserLoginDTO;
import com.armineasy.activitymaster.profiles.dto.UserRegistrationDTO;
import com.armineasy.activitymaster.profiles.exceptions.ProfileServiceException;
import com.armineasy.activitymaster.profiles.exceptions.UserExistsException;
import com.armineasy.activitymaster.profiles.exceptions.WaitingForConfirmationKeyException;
import com.google.common.base.Strings;
import com.jwebmp.guicedinjection.GuiceContext;

import java.util.Objects;
import java.util.UUID;

public interface IProfileService
{
	ProfileServiceDTO<?> loginUser(UserLoginDTO<?> profileServiceDTO, IEnterpriseName<?> enterpriseName, UUID... identityToken) throws ProfileServiceException;

	ProfileServiceDTO<?> logoutUser(UserLoginDTO<?> profileServiceDTO, IEnterpriseName<?> enterpriseName, UUID... identityToken) throws ProfileServiceException;

	ProfileServiceDTO<?> loginVisitor(ProfileServiceDTO<?> profileServiceDTO, IEnterpriseName<?> enterpriseName, UUID... identityToken) throws ProfileServiceException;

	default boolean verifyUsernameExists(UserLoginDTO<?> userLoginDTO, IEnterprise<?> enterprise, UUID... identityToken)
	{
		IInvolvedPartyService<?> ips = GuiceContext.get(IInvolvedPartyService.class);
		if (Strings.isNullOrEmpty(userLoginDTO.getUserName()))
		{
			throw new ProfileServiceException("Username cannot be empty");
		}
		return ips.doesUsernameExist(userLoginDTO.getUserName(), enterprise);
	}

	default UserLoginDTO<?> verifyPasswordForUser(UserLoginDTO<?> userLoginDTO, IEnterprise<?> enterprise, UUID... identityToken)
	{
		IInvolvedPartyService<?> ips = GuiceContext.get(IInvolvedPartyService.class);
		if (Objects.isNull(userLoginDTO.getIdentityToken()))
		{
			throw new ProfileServiceException("User Login DTO Already needs to have an associated UUID to login with a password");
		}
		if (Strings.isNullOrEmpty(userLoginDTO.getPassword()))
		{
			throw new ProfileServiceException("Passwords cannot be empty");
		}
		ISystems profileSystem = ProfileSystem.getNewSystem()
		                                      .get(enterprise);
		IInvolvedParty<?> ip = ips.findByUsernameAndPassword(userLoginDTO.getUserName(), userLoginDTO.getPassword(), profileSystem, true, identityToken);
		userLoginDTO = new UserLoginDTO<>().fromIP(ip);

		return userLoginDTO;
	}

	UserConfirmationKeyDTO<?> registerVisitor(UserRegistrationDTO<?> userRegistrationDTO, IEnterpriseName<?> enterpriseName, UUID... identityToken) throws UserExistsException, WaitingForConfirmationKeyException;
}
