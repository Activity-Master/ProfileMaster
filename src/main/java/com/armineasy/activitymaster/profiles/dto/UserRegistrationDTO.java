package com.armineasy.activitymaster.profiles.dto;

import lombok.experimental.Accessors;

import java.io.Serializable;

@Accessors(chain = true)
public class UserRegistrationDTO<J extends UserRegistrationDTO<J>>
		extends ProfileServiceDTO<J>
		implements Serializable
{
	private String userName;
	private String password;
	private boolean termsandconditions;

	public String getUserName()
	{
		return this.userName;
	}

	public String getPassword()
	{
		return this.password;
	}

	public boolean isTermsandconditions()
	{
		return this.termsandconditions;
	}

	public UserRegistrationDTO<J> setUserName(String userName)
	{
		this.userName = userName;
		return this;
	}

	public UserRegistrationDTO<J> setPassword(String password)
	{
		this.password = password;
		return this;
	}

	public UserRegistrationDTO<J> setTermsandconditions(boolean termsandconditions)
	{
		this.termsandconditions = termsandconditions;
		return this;
	}
}
