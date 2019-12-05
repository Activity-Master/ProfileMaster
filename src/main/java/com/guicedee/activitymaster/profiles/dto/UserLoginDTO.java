package com.guicedee.activitymaster.profiles.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

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

	public boolean equals(final Object o)
	{
		if (o == this)
		{
			return true;
		}
		if (!(o instanceof UserLoginDTO))
		{
			return false;
		}
		final UserLoginDTO<?> other = (UserLoginDTO<?>) o;
		if (!other.canEqual((Object) this))
		{
			return false;
		}
		final Object this$userName = this.getUserName();
		final Object other$userName = other.getUserName();
		if (this$userName == null ? other$userName != null : !this$userName.equals(other$userName))
		{
			return false;
		}
		final Object this$password = this.getPassword();
		final Object other$password = other.getPassword();
		if (this$password == null ? other$password != null : !this$password.equals(other$password))
		{
			return false;
		}
		if (this.isRememberMe() != other.isRememberMe())
		{
			return false;
		}
		return true;
	}

	protected boolean canEqual(final Object other)
	{
		return other instanceof UserLoginDTO;
	}

	public int hashCode()
	{
		final int PRIME = 59;
		int result = 1;
		final Object $userName = this.getUserName();
		result = result * PRIME + ($userName == null ? 43 : $userName.hashCode());
		final Object $password = this.getPassword();
		result = result * PRIME + ($password == null ? 43 : $password.hashCode());
		result = result * PRIME + (this.isRememberMe() ? 79 : 97);
		return result;
	}

	public String toString()
	{
		return "UserLoginDTO(userName=" + this.getUserName() + ", password=" + this.getPassword() + ", rememberMe=" + this.isRememberMe() + ")";
	}
}
