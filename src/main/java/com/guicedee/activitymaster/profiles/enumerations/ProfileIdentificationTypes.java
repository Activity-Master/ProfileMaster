package com.guicedee.activitymaster.profiles.enumerations;

public enum ProfileIdentificationTypes
{
	IdentificationTypeWebClientUUID("Web Client UUID"),

	;
	private String classificationValue;

	ProfileIdentificationTypes(String classificationValue)
	{
		this.classificationValue = classificationValue;
	}

	public String classificationValue()
	{
		return name();
	}
	public String classificationDescription()
	{
		return classificationValue;
	}
}
