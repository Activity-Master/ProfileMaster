package com.guicedee.activitymaster.profiles.enumerations;

import static com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts.*;

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
	private com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts concept;

	SiteClientClassifications(String description, com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts concept)
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

	public com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts concept()
	{
		return concept;
	}
}
