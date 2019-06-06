package com.armineasy.activitymaster.profiles.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Getter
@Setter

@Accessors(chain = true)
public class UserRegistrationDTO<J extends UserRegistrationDTO<J>>
		extends ProfileServiceDTO<J>
		implements Serializable
{
	private String userName;
	private String password;
	private boolean termsandconditions;
}
