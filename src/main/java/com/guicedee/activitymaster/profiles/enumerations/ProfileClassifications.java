package com.guicedee.activitymaster.profiles.enumerations;

import com.guicedee.activitymaster.core.services.enumtypes.IClassificationValue;
import com.guicedee.activitymaster.core.services.enumtypes.IClassificationDataConceptValue;
import com.guicedee.activitymaster.core.services.classifications.involvedparty.IInvolvedPartyClassification;

import static com.guicedee.activitymaster.core.services.concepts.EnterpriseClassificationDataConcepts.*;

public enum ProfileClassifications
		implements IClassificationValue<ProfileClassifications>
				           , IInvolvedPartyClassification<ProfileClassifications>
{
	LogonDetails("Details for Login", EventXInvolvedParty),
	LastLoginTime("The last time the Involved Party was logged in", EventXInvolvedParty),
	LastVisitTime("The last time the Involved Party visited the site", EventXInvolvedParty),
	UserRoles("A set list of User Roles for an Involved Party", InvolvedPartyXClassification),
	RememberMe("If the user login must be remembered", InvolvedPartyXClassification),
	LoggedOn("If the user is logged on", InvolvedPartyXClassification),

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
