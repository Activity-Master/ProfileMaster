package com.guicedee.activitymaster.profiles.events;

import com.guicedee.activitymaster.core.services.dto.*;
import com.guicedee.activitymaster.core.threads.TransactionalIdentifiedThread;
import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;

import java.util.UUID;

import static com.guicedee.activitymaster.core.services.classifications.classification.Classifications.*;
import static com.guicedee.activitymaster.core.services.classifications.events.EventInvolvedPartiesClassifications.*;
import static com.guicedee.activitymaster.core.services.types.NameTypes.*;

public class UpdateNewVisitEvent extends TransactionalIdentifiedThread
{
	private static final String JobServiceName = "NewVisitorCustomIdentifiersAndItems";
	
	private IInvolvedParty<?> newIp;
	private ProfileServiceDTO<?> profileServiceDTO;
	private IEnterprise<?> enterprise;
	private ISystems<?> profileSystem;
	
	private UUID[] identityToken;
	
	public UpdateNewVisitEvent()
	{
	}
	
	public static String getJobServiceName()
	{
		return UpdateNewVisitEvent.JobServiceName;
	}
	
	@Override
	public void perform()
	{
		newIp.addOrReuseNameType(PreferredNameType,NoClassification.name(), "Guest", profileSystem, identityToken);
		newIp.addOrReuse(CreatedBy, newIp.getId().toString(), profileSystem, identityToken);
	/*	event.addOrReuse(PerformedBy,
		                 newIp.getSecurityIdentity()
		                      .toString(),
		                 profileSystem,
		                 identityToken);*/
	}
	
	public IInvolvedParty<?> getNewIp()
	{
		return this.newIp;
	}
	
	public ProfileServiceDTO<?> getProfileServiceDTO()
	{
		return this.profileServiceDTO;
	}
	
	public IEnterprise<?> getEnterprise()
	{
		return this.enterprise;
	}
	
	public ISystems<?> getProfileSystem()
	{
		return this.profileSystem;
	}
	
	public UUID[] getIdentityToken()
	{
		return this.identityToken;
	}
	
	public UpdateNewVisitEvent setNewIp(IInvolvedParty<?> newIp)
	{
		this.newIp = newIp;
		return this;
	}
	
	public UpdateNewVisitEvent setProfileServiceDTO(ProfileServiceDTO<?> profileServiceDTO)
	{
		this.profileServiceDTO = profileServiceDTO;
		return this;
	}
	
	public UpdateNewVisitEvent setEnterprise(IEnterprise<?> enterprise)
	{
		this.enterprise = enterprise;
		return this;
	}
	
	public UpdateNewVisitEvent setProfileSystem(ISystems<?> profileSystem)
	{
		this.profileSystem = profileSystem;
		return this;
	}
	
	public UpdateNewVisitEvent setIdentityToken(UUID[] identityToken)
	{
		this.identityToken = identityToken;
		return this;
	}
	
	
	@Override
	public String toString()
	{
		return "UpdateNewVisitEvent(newIp=" + this.getNewIp() + ", profileServiceDTO=" + this.getProfileServiceDTO() + ", enterprise=" +
				this.getEnterprise() + ", profileSystem=" + this.getProfileSystem() + ", identityToken=" + java.util.Arrays.deepToString(this.getIdentityToken()) + ")";
	}
}
