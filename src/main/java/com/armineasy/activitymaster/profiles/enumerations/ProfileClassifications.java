package com.armineasy.activitymaster.profiles.enumerations;

import com.armineasy.activitymaster.activitymaster.services.IClassificationValue;
import com.armineasy.activitymaster.activitymaster.services.IClassificationDataConceptValue;
import com.armineasy.activitymaster.activitymaster.services.classifications.involvedparty.IInvolvedPartyClassification;

import static com.armineasy.activitymaster.activitymaster.services.concepts.EnterpriseClassificationDataConcepts.*;

public enum ProfileClassifications
		implements IClassificationValue<ProfileClassifications>
				           , IInvolvedPartyClassification<ProfileClassifications>
{
	LastLoginTime("The last time the Involved Party was logged in", EventXInvolvedParty),
	LastVisitTime("The last time the Involved Party visited the site", EventXInvolvedParty),
	UserRoles("A set list of User Roles for an Involved Party", InvolvedPartyXClassification),
	
	ConfirmationKey("A confirmation key to identify a user registration", InvolvedPartyXClassification),
	;

	private String description;
	private IClassificationDataConceptValue<?> concept;

	ProfileClassifications(String description, IClassificationDataConceptValue<?> concept)
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
	public IClassificationDataConceptValue<?> concept()
	{
		return concept;
	}
}
