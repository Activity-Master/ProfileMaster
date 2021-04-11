package com.guicedee.activitymaster.profiles.enumerations;

import static com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts.*;

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
	private com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts concept;

	ProfileClassifications(String description, com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts concept)
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

	public com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts concept()
	{
		return concept;
	}
}
