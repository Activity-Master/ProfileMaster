package com.guicedee.activitymaster.profiles.implementations;

import com.guicedee.guicedinjection.interfaces.IGuiceScanModuleInclusions;
import jakarta.validation.constraints.NotNull;

import java.util.HashSet;
import java.util.Set;

public class ProfileMasterModuleInclusion implements IGuiceScanModuleInclusions<ProfileMasterModuleInclusion>
{
	@Override
	public @NotNull Set<String> includeModules()
	{
		Set<String> set = new HashSet<>();
		set.add("com.guicedee.activitymaster.profiles");
		return set;
	}
}
