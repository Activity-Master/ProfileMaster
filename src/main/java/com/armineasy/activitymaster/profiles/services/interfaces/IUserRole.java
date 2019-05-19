package com.armineasy.activitymaster.profiles.services.interfaces;

@SuppressWarnings("unused")
public interface IUserRole<J extends Enum & IUserRole<J>>
{
	String name();

	@SuppressWarnings("unchecked")
	default J type()
	{
		return (J) this;
	}
}
