package com.armineasy.activitymaster.profiles.dto;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserDTOMapper
{
	UserDTOMapper INSTANCE = Mappers.getMapper(UserDTOMapper.class);

	UserProfileBasicDTO<?> downcast(ProfileServiceDTO<?> userDTO);

	UserProfileBasicDTO<?> downcast(UserDTO<?> userDTO);
	UserConfirmationKeyDTO<?> downcast(UserRegistrationDTO<?> userDTO);
	UserRegistrationDTO<?> downcast(UserConfirmationKeyDTO<?> userDTO);

}
