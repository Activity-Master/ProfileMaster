package com.armineasy.activitymaster.profiles.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain=true)
public class UserLoginDTO<J extends UserLoginDTO<J>> extends UserDTO<J>
{
	private String userName;
	private String password;
	private boolean rememberMe;
}
