package com.guicedee.activitymaster.profiles.services.interfaces;

@SuppressWarnings("unused")
public interface IUserRole<J extends Enum<J> & IUserRole<J>>
{
	String name();

	@SuppressWarnings("unchecked")
	default J type()
	{
		return (J) this;
	}
}
