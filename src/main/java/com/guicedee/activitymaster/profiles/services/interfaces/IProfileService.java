package com.guicedee.activitymaster.profiles.services.interfaces;

import com.guicedee.activitymaster.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.client.services.builders.warehouse.systems.ISystems;

import java.util.UUID;

public interface IProfileService<J extends IProfileService<J>>
{
	String ProfileSystemName = "Profiles Master";
	
	IInvolvedParty<?,?> findInvolvedParty(UUID identityToken, ISystems<?,?> system);
	
	IInvolvedParty<?,?> findInvolvedParty(UUID webClientToken);
}
