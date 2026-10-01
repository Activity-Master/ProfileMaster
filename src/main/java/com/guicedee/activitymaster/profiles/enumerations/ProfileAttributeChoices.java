package com.guicedee.activitymaster.profiles.enumerations;

import java.util.List;

import static com.guicedee.activitymaster.profiles.enumerations.ProfileAttributes.*;

/**
 * The default choices for profile attributes that take one value from a fixed list.
 *
 * <p>Each constant is installed as a classification (named after the constant, described by its
 * {@link #label()}) linked as a child of its attribute's classification, for example
 * {@code MaritalStatus -> MaritalStatusMarried}. The FSDM hierarchy is the source of truth:
 * {@code IProfileService#getAttributeChoices} reads the children back, so an enterprise may add further
 * children without changing this enum. The stored attribute value is the child classification name.</p>
 */
public enum ProfileAttributeChoices
{
	GenderFemale(Gender, "Female"),
	GenderMale(Gender, "Male"),
	GenderNonBinary(Gender, "Non-binary"),
	GenderOther(Gender, "Other"),
	GenderUndisclosed(Gender, "Prefer not to say"),

	PronounsSheHer(Pronouns, "she/her"),
	PronounsHeHim(Pronouns, "he/him"),
	PronounsTheyThem(Pronouns, "they/them"),
	PronounsSheThey(Pronouns, "she/they"),
	PronounsHeThey(Pronouns, "he/they"),
	PronounsAny(Pronouns, "Any pronouns"),
	PronounsUndisclosed(Pronouns, "Prefer not to say"),

	MaritalStatusSingle(MaritalStatus, "Single"),
	MaritalStatusMarried(MaritalStatus, "Married"),
	MaritalStatusPartnered(MaritalStatus, "Domestic partnership"),
	MaritalStatusSeparated(MaritalStatus, "Separated"),
	MaritalStatusDivorced(MaritalStatus, "Divorced"),
	MaritalStatusWidowed(MaritalStatus, "Widowed"),
	MaritalStatusUndisclosed(MaritalStatus, "Prefer not to say"),

	EthnicityAfrican(Ethnicity, "Black / African"),
	EthnicityAsian(Ethnicity, "Asian"),
	EthnicityEuropean(Ethnicity, "Caucasian / White"),
	EthnicityHispanicLatino(Ethnicity, "Hispanic / Latino"),
	EthnicityIndigenous(Ethnicity, "Indigenous"),
	EthnicityPacificIslander(Ethnicity, "Pacific Islander"),
	EthnicityMiddleEasternNorthAfrican(Ethnicity, "Middle Eastern or North African"),
	EthnicityMixed(Ethnicity, "Mixed"),
	EthnicityOther(Ethnicity, "Other"),
	EthnicityUndisclosed(Ethnicity, "Prefer not to say"),

	ReligionChristianity(Religion, "Christianity"),
	ReligionIslam(Religion, "Muslim (Islam)"),
	ReligionHinduism(Religion, "Hinduism"),
	ReligionBuddhism(Religion, "Buddhism"),
	ReligionJudaism(Religion, "Judaism"),
	ReligionChineseAncestral(Religion, "Chinese ancestral religion"),
	ReligionTaoism(Religion, "Taoism"),
	ReligionConfucianism(Religion, "Confucianism"),
	ReligionSikhism(Religion, "Sikhism"),
	ReligionShinto(Religion, "Shinto"),
	ReligionBahai(Religion, "Bahá’í"),
	ReligionWicca(Religion, "Wicca"),
	ReligionPaganism(Religion, "Paganism"),
	ReligionJainism(Religion, "Jainism"),
	ReligionZoroastrianism(Religion, "Zoroastrianism"),
	ReligionAtheism(Religion, "Atheism"),
	ReligionAgnosticism(Religion, "Agnosticism"),
	ReligionTraditional(Religion, "Traditional religion"),
	ReligionOther(Religion, "Other"),
	ReligionNone(Religion, "No religion"),
	ReligionUndisclosed(Religion, "Prefer not to say"),

	BloodTypeOPositive(BloodType, "O+"),
	BloodTypeONegative(BloodType, "O−"),
	BloodTypeAPositive(BloodType, "A+"),
	BloodTypeANegative(BloodType, "A−"),
	BloodTypeBPositive(BloodType, "B+"),
	BloodTypeBNegative(BloodType, "B−"),
	BloodTypeABPositive(BloodType, "AB+"),
	BloodTypeABNegative(BloodType, "AB−"),
	BloodTypeUnknown(BloodType, "Unknown"),
	;

	private final ProfileAttributes attribute;
	private final String label;

	ProfileAttributeChoices(ProfileAttributes attribute, String label)
	{
		this.attribute = attribute;
		this.label = label;
	}

	/** The attribute whose classification is this choice's parent. */
	public ProfileAttributes attribute()
	{
		return attribute;
	}

	/** The display text, stored as the classification description. */
	public String label()
	{
		return label;
	}

	/** The position of this choice within its attribute's default list. */
	public int sequence()
	{
		int position = 1;
		for (ProfileAttributeChoices candidate : values())
		{
			if (candidate == this)
			{
				return position;
			}
			if (candidate.attribute == attribute)
			{
				position++;
			}
		}
		return position;
	}

	/** The attributes that take their value from a list of choices, in declaration order. */
	public static List<ProfileAttributes> attributes()
	{
		return ProfileChoiceConcepts.attributes();
	}

	/** Resolves a default choice by classification name, or {@code null} for an enterprise-added child. */
	public static ProfileAttributeChoices fromName(String name)
	{
		if (name != null)
		{
			for (ProfileAttributeChoices candidate : values())
			{
				if (candidate.name().equals(name))
				{
					return candidate;
				}
			}
		}
		return null;
	}
}
