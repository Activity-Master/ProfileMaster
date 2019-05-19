package com.armineasy.activitymaster.profiles.enumerations;

import com.armineasy.activitymaster.activitymaster.services.IIdentificationType;
import com.armineasy.activitymaster.activitymaster.services.types.IdentificationTypes;

public enum ProfileIdentificationTypes
		implements IIdentificationType<IdentificationTypes>
{
	IdentificationTypeWebClientUUID("Web Client UUID"),

	;
	private String classificationValue;

	ProfileIdentificationTypes(String classificationValue)
	{
		this.classificationValue = classificationValue;
	}

	@Override
	public String classificationValue()
	{
		return name();
	}

	@Override
	public String classificationDescription()
	{
		return classificationValue;
	}
}
