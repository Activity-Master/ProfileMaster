package com.armineasy.activitymaster.profiles.enumerations;

import com.armineasy.activitymaster.activitymaster.services.IEventTypeValue;

public enum ProfileEventTypes
		implements IEventTypeValue<ProfileEventTypes>
{
	SiteVisit("When a user visits the site"),
	UserRegistered("When a user has registered with the site"),
	UserConfirmedAccount("When the account has been confirmed with the user"),



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
