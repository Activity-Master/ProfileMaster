package com.guicedee.activitymaster.profiles.enumerations;

import com.guicedee.activitymaster.fsdm.client.types.classifications.EnterpriseClassificationDataConcepts;

import static com.guicedee.activitymaster.fsdm.client.types.classifications.EnterpriseClassificationDataConcepts.*;

public enum ProfileClassifications
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
	private EnterpriseClassificationDataConcepts concept;

	ProfileClassifications(String description, EnterpriseClassificationDataConcepts concept)
	{
		this.description = description;
		this.concept = concept;
	}

	ProfileClassifications(String description)
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
