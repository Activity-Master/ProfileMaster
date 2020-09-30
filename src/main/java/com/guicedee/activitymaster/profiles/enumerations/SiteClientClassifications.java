package com.guicedee.activitymaster.profiles.enumerations;

import com.guicedee.activitymaster.core.services.classifications.involvedparty.IInvolvedPartyClassification;
import com.guicedee.activitymaster.core.services.enumtypes.IClassificationDataConceptValue;
import com.guicedee.activitymaster.core.services.enumtypes.IClassificationValue;

import static com.guicedee.activitymaster.core.services.concepts.EnterpriseClassificationDataConcepts.*;

public enum SiteClientClassifications
		implements IClassificationValue<SiteClientClassifications>
				           , IInvolvedPartyClassification<SiteClientClassifications>
{
	ClientConnectionDetails("Details for web site connection", EventXClassification),
	OperatingSystem("The operating system used to browse", EventXClassification),
	OperatingSystemFamily("The operating system family", EventXClassification),
	BrowserDeviceCategory("The browser device category ", EventXClassification),
	BrowserDevice("The browser device", EventXClassification),
	BrowserIcon("The icon for a browser", EventXClassification),

	;

	private String description;
	private IClassificationDataConceptValue<?> concept;

	SiteClientClassifications(String description, IClassificationDataConceptValue<?> concept)
	{
		this.description = description;
		this.concept = concept;
	}

	SiteClientClassifications(String description)
	{
		this.description = description;
	}

	@Override
	public String classificationDescription()
	{
		return this.description;
	}

	@Override
	public IClassificationDataConceptValue<?> concept()
	{
		return concept;
	}
}
