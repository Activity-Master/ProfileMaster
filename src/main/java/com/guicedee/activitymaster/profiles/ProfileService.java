package com.guicedee.activitymaster.profiles;

import com.guicedee.activitymaster.client.services.IInvolvedPartyService;
import com.guicedee.activitymaster.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.profiles.services.interfaces.IProfileService;

import java.util.UUID;
import java.util.logging.Logger;

import static com.guicedee.activitymaster.profiles.enumerations.ProfileIdentificationTypes.*;
import static com.guicedee.guicedinjection.GuiceContext.*;

@SuppressWarnings("Duplicates")
public class ProfileService
		implements IProfileService<ProfileService>
{
	private static final Logger log = Logger.getLogger(ProfileService.class.getName());

	@Override
	public IInvolvedParty<?,?> findInvolvedParty(UUID webClientToken, ISystems<?,?> system)
	{
		IInvolvedPartyService<?> involvedPartyService = get(IInvolvedPartyService.class);
		IEnterprise<?,?> enterprise = system.getEnterprise();
		ISystems<?,?> profileSystem = get(ProfileSystem.class)
				.getSystem(enterprise);
		UUID profileSystemUUID = get(ProfileSystem.class)
				.getSystemToken(enterprise);
		IInvolvedParty<?,?> party = involvedPartyService.findByIdentificationType(IdentificationTypeWebClientUUID.toString(),
				webClientToken.toString(),
				profileSystem,
				profileSystemUUID);
		return party;
	}
	
	@Override
	public IInvolvedParty<?,?> findInvolvedParty(UUID webClientToken)
	{
		IInvolvedPartyService<?> involvedPartyService = get(IInvolvedPartyService.class);
		return involvedPartyService.findByIdentificationType(IdentificationTypeWebClientUUID.toString(), webClientToken.toString());
	}
	
	
}
