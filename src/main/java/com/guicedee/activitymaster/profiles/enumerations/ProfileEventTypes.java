package com.guicedee.activitymaster.profiles.enumerations;


public enum ProfileEventTypes
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

	public String classificationValue()
	{
		return description;
	}
}
