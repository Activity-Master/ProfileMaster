package com.guicedee.activitymaster.profiles.services.interfaces;

import com.guicedee.activitymaster.core.services.dto.IInvolvedParty;
import com.guicedee.activitymaster.core.services.dto.ISystems;

import java.util.UUID;

public interface IProfileService<J extends IProfileService<J>>
{
	String ProfileSystemName = "Profiles Master";
	
	IInvolvedParty<?> findInvolvedParty(UUID identityToken, ISystems<?> system);
	
	IInvolvedParty<?> findInvolvedParty(UUID webClientToken);
}
