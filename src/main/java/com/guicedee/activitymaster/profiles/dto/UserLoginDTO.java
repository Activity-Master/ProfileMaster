package com.guicedee.activitymaster.profiles.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.*;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = ANY,getterVisibility = NONE,setterVisibility = NONE)
public class UserLoginDTO<J extends UserLoginDTO<J>>
		extends ProfileServiceDTO<J>
		implements Serializable
{
	private String userName;
	private String password;
	private boolean rememberMe;

	public UserLoginDTO()
	{
	}

	public String getUserName()
	{
		return this.userName;
	}

	public String getPassword()
	{
		return this.password;
	}

	public boolean isRememberMe()
	{
		return this.rememberMe;
	}

	public UserLoginDTO<J> setUserName(String userName)
	{
		this.userName = userName;
		return this;
	}

	public UserLoginDTO<J> setPassword(String password)
	{
		this.password = password;
		return this;
	}

	public UserLoginDTO<J> setRememberMe(boolean rememberMe)
	{
		this.rememberMe = rememberMe;
		return this;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}
		if (!super.equals(o))
		{
			return false;
		}
		UserLoginDTO<?> that = (UserLoginDTO<?>) o;
		return Objects.equals(getUserName(), that.getUserName());
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(super.hashCode(), getUserName());
	}

	public String toString()
	{
		return "UserLoginDTO(userName=" + this.getUserName() + ", password=" + this.getPassword() + ", rememberMe=" + this.isRememberMe() + ")";
	}
}
