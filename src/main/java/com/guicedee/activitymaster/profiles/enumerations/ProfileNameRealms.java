package com.guicedee.activitymaster.profiles.enumerations;

import com.guicedee.activitymaster.fsdm.client.services.classifications.types.NameTypes;

import java.util.List;

import static com.guicedee.activitymaster.fsdm.client.services.classifications.types.NameTypes.*;

/**
 * Classifications on the involved-party name-type link that separate the names a person uses in
 * social and work settings from their personal (unclassified) names.
 *
 * <p>The classification name is the enum constant name. Personal names keep the
 * {@code NoClassification} link so existing profile data remains the personal realm.</p>
 */
public enum ProfileNameRealms
{
	ProfileSocialName("Social", "Names used on social profiles and in community spaces",
			PreferredNameType, CommonNameType, FullNameType, InitialsType, SuffixType),
	ProfileWorkName("Work", "Names used in work and professional settings",
			SalutationType, PreferredNameType, FullNameType, InitialsType, SuffixType, QualificationType),
	;

	private final String realm;
	private final String description;
	private final List<NameTypes> nameTypes;

	ProfileNameRealms(String realm, String description, NameTypes... nameTypes)
	{
		this.realm = realm;
		this.description = description;
		this.nameTypes = List.of(nameTypes);
	}

	/** The profile realm, {@code Social} or {@code Work}. */
	public String realm()
	{
		return realm;
	}

	public String classificationDescription()
	{
		return description;
	}

	/** The name types a profile may hold in this realm. */
	public List<NameTypes> nameTypes()
	{
		return nameTypes;
	}

	/** Resolves a realm by its {@link #realm()} or classification name, ignoring case. */
	public static ProfileNameRealms fromRealm(String value)
	{
		if (value != null)
		{
			for (ProfileNameRealms candidate : values())
			{
				if (candidate.realm.equalsIgnoreCase(value) || candidate.name().equalsIgnoreCase(value))
				{
					return candidate;
				}
			}
		}
		return null;
	}
}
