package com.guicedee.activitymaster.profiles.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.guicedee.guicedinjection.json.LocalDateTimeDeserializer;
import com.guicedee.guicedinjection.json.LocalDateTimeSerializer;

import java.io.Serializable;
import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.*;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = ANY,
		getterVisibility = NONE,
		setterVisibility = NONE)
public class UserSecurity implements Serializable
{
	private static final long serialVersionUID = 1L;

	private boolean loggedIn;
	private boolean rememberMe;
	private String lastIpAddress;
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	private LocalDateTime loginExpiresOn= LocalDateTime.now()
	                                                   .plusMinutes(20);

	public UserSecurity()
	{
		//No config required
	}

	public boolean isLoggedIn()
	{
		return loggedIn;
	}

	public UserSecurity setLoggedIn(boolean loggedIn)
	{
		this.loggedIn = loggedIn;
		return this;
	}

	public boolean isRememberMe()
	{
		return rememberMe;
	}

	public UserSecurity setRememberMe(boolean rememberMe)
	{
		this.rememberMe = rememberMe;
		return this;
	}

	public String getLastIpAddress()
	{
		return lastIpAddress;
	}

	public UserSecurity setLastIpAddress(String lastIpAddress)
	{
		this.lastIpAddress = lastIpAddress;
		return this;
	}

	public LocalDateTime getLoginExpiresOn()
	{
		return loginExpiresOn;
	}

	public UserSecurity setLoginExpiresOn(LocalDateTime loginExpiresOn)
	{
		this.loginExpiresOn = loginExpiresOn;
		return this;
	}
}
