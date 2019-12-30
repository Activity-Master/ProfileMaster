package com.guicedee.activitymaster.profiles.services.interfaces;

import com.google.common.base.Strings;
import com.guicedee.activitymaster.core.services.classifications.enterprise.IEnterpriseName;
import com.guicedee.activitymaster.core.services.dto.IEnterprise;
import com.guicedee.activitymaster.core.services.dto.IInvolvedParty;
import com.guicedee.activitymaster.core.services.dto.ISystems;
import com.guicedee.activitymaster.core.services.system.IInvolvedPartyService;
import com.guicedee.activitymaster.profiles.ProfileSystem;
import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;
import com.guicedee.activitymaster.profiles.exceptions.ProfileServiceException;
import com.guicedee.activitymaster.profiles.exceptions.UserExistsException;
import com.guicedee.activitymaster.profiles.exceptions.WaitingForConfirmationKeyException;
import com.guicedee.activitymaster.profiles.webdto.UserConfirmationKeyDTO;
import com.guicedee.activitymaster.profiles.webdto.UserLoginDTO;
import com.guicedee.activitymaster.profiles.webdto.UserRegistrationDTO;
import com.guicedee.guicedinjection.GuiceContext;

import java.util.Objects;
import java.util.UUID;

import static com.guicedee.guicedinjection.GuiceContext.*;

public interface IProfileService
{
	ProfileServiceDTO<?> loginUser(UserLoginDTO<?> profileServiceDTO, IEnterpriseName<?> enterpriseName, UUID... identityToken) throws ProfileServiceException;

	ProfileServiceDTO<?> logoutUser(ProfileServiceDTO<?> profileServiceDTO, IEnterpriseName<?> enterpriseName, UUID... identityToken) throws ProfileServiceException;

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
		ISystems<?> profileSystem = get(ProfileSystem.class)
				                            .getSystem(enterprise);
		IInvolvedParty<?> ip = ips.findByUsernameAndPassword(userLoginDTO.getUserName(), userLoginDTO.getPassword(), profileSystem, true, identityToken);
		userLoginDTO = new UserLoginDTO<>().fromIP(ip);

		return userLoginDTO;
	}

	UserConfirmationKeyDTO<?> registerVisitor(UserRegistrationDTO<?> userRegistrationDTO, IEnterpriseName<?> enterpriseName, UUID... identityToken) throws UserExistsException, WaitingForConfirmationKeyException;

	IInvolvedParty<?> findInvolvedParty(UUID identityToken, IEnterpriseName<?> enterpriseName);
}
