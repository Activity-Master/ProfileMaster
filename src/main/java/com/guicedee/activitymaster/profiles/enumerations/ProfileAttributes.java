package com.guicedee.activitymaster.profiles.enumerations;

import com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts;

import static com.guicedee.activitymaster.fsdm.client.services.classifications.EnterpriseClassificationDataConcepts.InvolvedPartyXClassification;

/**
 * The comprehensive set of profile attributes an individual's full profile can hold.
 *
 * <p>Each constant maps to an ActivityMaster {@code Classification} (created at install time by
 * {@code ProfileMasterInstall}) and is stored against the person's {@code InvolvedParty} as a
 * classification value. Names (first name, surname, etc.) are stored separately through the FSDM
 * {@code NameTypes} mechanism — everything else (occupation, contact details, demographics,
 * identification, social handles, etc.) is captured here.</p>
 *
 * <p>The {@link #name()} of each constant is the canonical classification name used for both writing
 * ({@code addOrUpdateClassification}) and reading ({@code findClassificationValues}).</p>
 */
public enum ProfileAttributes
{
	// ---- Demographics ----
	DateOfBirth("The individual's date of birth (ISO-8601, yyyy-MM-dd)"),
	Gender("The individual's gender"),
	Pronouns("The individual's preferred pronouns"),
	MaritalStatus("The individual's marital status"),
	Nationality("The individual's nationality"),
	CountryOfBirth("The country the individual was born in"),
	PlaceOfBirth("The town/city the individual was born in"),
	Ethnicity("The individual's ethnicity"),
	Religion("The individual's religion"),
	HomeLanguage("The individual's primary/home language"),
	SpokenLanguages("A comma-separated list of languages the individual speaks"),
	BloodType("The individual's blood type"),

	// ---- Contact ----
	PrimaryEmail("The individual's primary email address"),
	SecondaryEmail("The individual's secondary email address"),
	MobileNumber("The individual's mobile phone number"),
	HomeNumber("The individual's home phone number"),
	WorkNumber("The individual's work phone number"),
	FaxNumber("The individual's fax number"),
	Website("The individual's personal website"),

	// ---- Address ----
	ResidentialAddress("The individual's residential street address"),
	PostalAddress("The individual's postal address"),
	City("The individual's city/town"),
	Province("The individual's province/state"),
	PostalCode("The individual's postal/zip code"),
	Country("The individual's country of residence"),

	// ---- Occupation / Employment ----
	Occupation("The individual's occupation"),
	JobTitle("The individual's current job title"),
	Employer("The individual's current employer"),
	Department("The department the individual works in"),
	EmploymentStatus("The individual's employment status"),
	Industry("The industry the individual works in"),
	YearsOfExperience("The individual's total years of professional experience"),
	WorkEmail("The individual's work email address"),
	EmployeeNumber("The individual's employee number"),
	Manager("The name of the individual's line manager"),
	OfficeLocation("The individual's office location"),

	// ---- Education ----
	HighestQualification("The individual's highest qualification"),
	FieldOfStudy("The individual's field of study"),
	Institution("The institution the individual studied at"),
	GraduationYear("The year the individual graduated"),

	// ---- Identification ----
	IdNumber("The individual's national identity number"),
	PassportNumber("The individual's passport number"),
	TaxNumber("The individual's tax reference number"),
	DriversLicenseNumber("The individual's driver's licence number"),

	// ---- Social / Online presence ----
	LinkedIn("The individual's LinkedIn profile"),
	Twitter("The individual's Twitter/X handle"),
	Facebook("The individual's Facebook profile"),
	Instagram("The individual's Instagram handle"),
	GitHub("The individual's GitHub profile"),

	// ---- Health ----
	MedicalAidName("The individual's medical aid/health insurance provider"),
	MedicalAidNumber("The individual's medical aid/health insurance number"),
	DietaryRequirements("The individual's dietary requirements"),
	DisabilityStatus("The individual's disability status"),

	// ---- Emergency contact ----
	EmergencyContactName("The name of the individual's emergency contact"),
	EmergencyContactNumber("The phone number of the individual's emergency contact"),
	EmergencyContactRelationship("The relationship of the emergency contact to the individual"),

	// ---- Meta / Preferences ----
	PreferredContactMethod("The individual's preferred contact method"),
	TimeZone("The individual's time zone"),
	Locale("The individual's preferred locale"),
	Biography("A free-text biography of the individual"),
	AvatarUrl("A URL to the individual's avatar/profile picture"),
	Notes("Free-text notes about the individual"),
	;

	private final String description;
	private final EnterpriseClassificationDataConcepts concept;

	ProfileAttributes(String description)
	{
		this(description, InvolvedPartyXClassification);
	}

	ProfileAttributes(String description, EnterpriseClassificationDataConcepts concept)
	{
		this.description = description;
		this.concept = concept;
	}

	public String classificationDescription()
	{
		return description;
	}

	public EnterpriseClassificationDataConcepts concept()
	{
		return concept;
	}
}

