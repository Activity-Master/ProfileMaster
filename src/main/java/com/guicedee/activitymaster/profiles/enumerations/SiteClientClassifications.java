package com.guicedee.activitymaster.profiles.enumerations;

import com.guicedee.activitymaster.fsdm.client.types.classifications.EnterpriseClassificationDataConcepts;

import static com.guicedee.activitymaster.fsdm.client.types.classifications.EnterpriseClassificationDataConcepts.*;

public enum SiteClientClassifications
{
	ClientConnectionDetails("Details for site connection", EventXClassification),
	
	OperatingSystem("The operating system used to browse", EventXClassification),
	OperatingSystemFamily("The operating system family", EventXClassification),
	BrowserDeviceCategory("The browser device category ", EventXClassification),
	BrowserDevice("The browser device", EventXClassification),
	BrowserIcon("The icon for a browser", EventXClassification),

	;

	private String description;
	private EnterpriseClassificationDataConcepts concept;

	SiteClientClassifications(String description, EnterpriseClassificationDataConcepts concept)
	{
		this.description = description;
		this.concept = concept;
	}

	SiteClientClassifications(String description)
	{
		this.description = description;
	}

	public String classificationDescription()
	{
		return this.description;
	}

	public EnterpriseClassificationDataConcepts concept()
	{
		return concept;
	}
}
