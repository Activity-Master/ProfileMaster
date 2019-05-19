package com.armineasy.activitymaster.profiles.enumerations;

import com.armineasy.activitymaster.activitymaster.services.IClassificationValue;
import com.armineasy.activitymaster.activitymaster.services.IDataConceptValue;
import com.armineasy.activitymaster.activitymaster.services.classifications.involvedparty.IInvolvedPartyClassification;

import static com.armineasy.activitymaster.activitymaster.services.concepts.EnterpriseDataConcepts.*;

public enum ProfileClassifications
		implements IClassificationValue<ProfileClassifications>
				           , IInvolvedPartyClassification<ProfileClassifications>
{
	LastLoginTime("The last time the Involved Party was logged in", EventXInvolvedParty),
	LastVisitTime("The last time the Involved Party visited the site", EventXInvolvedParty),
	;

	private String description;
	private IDataConceptValue<?> concept;

	ProfileClassifications(String description, IDataConceptValue<?> concept)
	{
		this.description = description;
		this.concept = concept;
	}

	ProfileClassifications(String description)
	{
		this.description = description;
	}

	@Override
	public String classificationDescription()
	{
		return this.description;
	}

	@Override
	public IDataConceptValue<?> concept()
	{
		return concept;
	}
}
