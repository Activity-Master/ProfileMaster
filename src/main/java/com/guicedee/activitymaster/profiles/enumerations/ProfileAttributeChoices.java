package com.guicedee.activitymaster.profiles.enumerations;

import java.util.ArrayList;
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
		List<ProfileAttributes> attributes = new ArrayList<>();
		for (ProfileAttributeChoices choice : values())
		{
			if (!attributes.contains(choice.attribute))
			{
				attributes.add(choice.attribute);
			}
		}
		return attributes;
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
