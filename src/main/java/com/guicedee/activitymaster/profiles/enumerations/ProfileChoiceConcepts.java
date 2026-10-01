package com.guicedee.activitymaster.profiles.enumerations;

import java.util.List;

/** Profile-owned data concepts for fields whose values are selected from published classifications. */
public enum ProfileChoiceConcepts
{
	Gender(ProfileAttributes.Gender, "ProfileGenderChoices"),
	Pronouns(ProfileAttributes.Pronouns, "ProfilePronounChoices", LinkKind.NAME),
	MaritalStatus(ProfileAttributes.MaritalStatus, "ProfileMaritalStatusChoices"),
	Occupation(ProfileAttributes.Occupation, "ProfileOccupations"),
	Ethnicity(ProfileAttributes.Ethnicity, "ProfileEthnicityChoices"),
	Religion(ProfileAttributes.Religion, "ProfileReligionChoices"),
	BloodType(ProfileAttributes.BloodType, "ProfileBloodTypeChoices"),
	HomeLanguage(ProfileAttributes.HomeLanguage, "ProfileLanguages"),
	SpokenLanguages(ProfileAttributes.SpokenLanguages, "ProfileLanguages");

	/** Legacy link metadata used when reading and retiring pre-encryption selected values. */
	public enum LinkKind { PARTY, NAME }

	private final ProfileAttributes attribute;
	private final String conceptName;
	private final LinkKind linkKind;

	ProfileChoiceConcepts(ProfileAttributes attribute, String conceptName)
	{
		this(attribute, conceptName, LinkKind.PARTY);
	}

	ProfileChoiceConcepts(ProfileAttributes attribute, String conceptName, LinkKind linkKind)
	{
		this.attribute = attribute;
		this.conceptName = conceptName;
		this.linkKind = linkKind;
	}

	public ProfileAttributes attribute() { return attribute; }
	public String conceptName() { return conceptName; }
	public LinkKind linkKind() { return linkKind; }
	public String linkTypeName() { return "Profile" + attribute.name(); }

	public static ProfileChoiceConcepts forAttribute(ProfileAttributes attribute)
	{
		for (ProfileChoiceConcepts concept : values())
			if (concept.attribute == attribute) return concept;
		return null;
	}

	public static ProfileChoiceConcepts forAttributeName(String attributeName)
	{
		for (ProfileChoiceConcepts concept : values())
			if (concept.attribute.name().equals(attributeName)) return concept;
		return null;
	}

	public static List<ProfileAttributes> attributes()
	{
		return List.of(values()).stream().map(ProfileChoiceConcepts::attribute).toList();
	}
}
