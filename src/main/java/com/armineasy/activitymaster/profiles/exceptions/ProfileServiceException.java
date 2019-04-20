package com.armineasy.activitymaster.profiles.exceptions;

import com.armineasy.activitymaster.activitymaster.services.exceptions.ActivityMasterException;

public class ProfileServiceException extends ActivityMasterException
{
	public ProfileServiceException()
	{
	}

	public ProfileServiceException(String message)
	{
		super(message);
	}

	public ProfileServiceException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ProfileServiceException(Throwable cause)
	{
		super(cause);
	}

	public ProfileServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
