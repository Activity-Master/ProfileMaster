package com.armineasy.activitymaster.profiles.enumerations;

import com.armineasy.activitymaster.profiles.services.interfaces.IUserRole;

import java.util.EnumSet;

public enum UserRoles
		implements IUserRole<UserRoles>
{
	EnterpriseCreator,
	Administrator,
	Visitor,
	None,
	;

	public static final EnumSet<UserRoles> AdministratorsRoles = EnumSet.of(EnterpriseCreator, Administrator);
	public static final EnumSet<UserRoles> VisitorRoles = EnumSet.of(Visitor);
	public static final EnumSet<UserRoles> NoRole = EnumSet.of(None);

}
