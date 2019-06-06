package com.armineasy.activitymaster.profiles.exceptions;

public class WaitingForConfirmationKeyException
		extends Exception
{
	public WaitingForConfirmationKeyException()
	{
	}

	public WaitingForConfirmationKeyException(String message)
	{
		super(message);
	}

	public WaitingForConfirmationKeyException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public WaitingForConfirmationKeyException(Throwable cause)
	{
		super(cause);
	}

	public WaitingForConfirmationKeyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
