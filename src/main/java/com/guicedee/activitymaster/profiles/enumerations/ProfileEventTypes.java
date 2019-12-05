package com.guicedee.activitymaster.profiles.enumerations;

import com.guicedee.activitymaster.core.services.enumtypes.IEventTypeValue;

public enum ProfileEventTypes
		implements IEventTypeValue<ProfileEventTypes>
{
	SiteVisit("When a user visits the site"),
	VisitorRegistered("When a visitor has registered with the site"),
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
