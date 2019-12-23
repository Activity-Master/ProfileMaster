package com.guicedee.activitymaster.profiles.webdto;

import com.guicedee.activitymaster.profiles.dto.UserDTO;
import com.guicedee.activitymaster.profiles.services.interfaces.IUserRole;

public class UserProfileBasicDTO<J extends UserProfileBasicDTO<J>>
		extends UserDTO<J>
{
	private String fullName;
	private IUserRole<?> role;
}
