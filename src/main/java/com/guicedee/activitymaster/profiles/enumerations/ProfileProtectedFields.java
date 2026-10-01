package com.guicedee.activitymaster.profiles.enumerations;

import com.guicedee.activitymaster.fsdm.client.services.classifications.types.NameTypes;

/** Identification types carrying personal profile values through FSDM column encryption. */
public final class ProfileProtectedFields
{
	private ProfileProtectedFields() {}

	public static String attributeType(String attribute) { return "ProfileAttribute" + attribute; }
	public static String nameType(String realm, NameTypes name) { return "ProfileName" + realm + name.name(); }
}
