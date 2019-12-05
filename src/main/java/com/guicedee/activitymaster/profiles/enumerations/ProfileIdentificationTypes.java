package com.guicedee.activitymaster.profiles.enumerations;

import com.guicedee.activitymaster.core.services.enumtypes.IIdentificationType;
import com.guicedee.activitymaster.core.services.types.IdentificationTypes;

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
