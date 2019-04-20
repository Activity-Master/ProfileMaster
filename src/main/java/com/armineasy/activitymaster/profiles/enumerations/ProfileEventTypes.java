package com.armineasy.activitymaster.profiles.enumerations;

import com.armineasy.activitymaster.activitymaster.services.IEventTypeValue;

public enum ProfileEventTypes
		implements IEventTypeValue<ProfileEventTypes>
{
	GuestVisit("When a guest user visits the site"),

	;

	private String description;

	ProfileEventTypes(String description)
	{
		this.description = description;
	}

	@Override
	public String classificationValue()
	{
		return description;
	}
}
