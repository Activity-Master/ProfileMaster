package com.armineasy.activitymaster.profiles.dto;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserDTOMapper
{
	UserDTOMapper INSTANCE = Mappers.getMapper(UserDTOMapper.class);

	UserProfileBasicDTO<?> downcast(UserDTO<?> userDTO);

}
