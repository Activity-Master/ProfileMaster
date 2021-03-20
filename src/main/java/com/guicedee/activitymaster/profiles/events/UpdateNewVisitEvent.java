package com.guicedee.activitymaster.profiles.events;

import com.guicedee.activitymaster.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.core.threads.TransactionalIdentifiedThread;
import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;

import java.util.UUID;

import static com.guicedee.activitymaster.client.services.classifications.DefaultClassifications.*;
import static com.guicedee.activitymaster.client.services.classifications.EventInvolvedPartiesClassifications.*;
import static com.guicedee.activitymaster.client.services.classifications.types.NameTypes.*;

public class UpdateNewVisitEvent extends TransactionalIdentifiedThread
{
	private static final String JobServiceName = "NewVisitorCustomIdentifiersAndItems";
	
	private IInvolvedParty<?,?> newIp;
	private ProfileServiceDTO<?> profileServiceDTO;
	private IEnterprise<?,?> enterprise;
	private ISystems<?,?> profileSystem;
	
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
		newIp.addOrReuseInvolvedPartyNameType(NoClassification.toString(), PreferredNameType.toString(),"Guest", profileSystem, identityToken);
		newIp.addOrReuseClassification(CreatedBy, newIp.getId().toString(), profileSystem, identityToken);
	/*	event.addOrReuseClassification(PerformedBy,
		                 newIp.getSecurityIdentity()
		                      .toString(),
		                 profileSystem,
		                 identityToken);*/
	}
	
	public IInvolvedParty<?,?> getNewIp()
	{
		return this.newIp;
	}
	
	public ProfileServiceDTO<?> getProfileServiceDTO()
	{
		return this.profileServiceDTO;
	}
	
	public IEnterprise<?,?> getEnterprise()
	{
		return this.enterprise;
	}
	
	public ISystems<?,?> getProfileSystem()
	{
		return this.profileSystem;
	}
	
	public UUID[] getIdentityToken()
	{
		return this.identityToken;
	}
	
	public UpdateNewVisitEvent setNewIp(IInvolvedParty<?,?> newIp)
	{
		this.newIp = newIp;
		return this;
	}
	
	public UpdateNewVisitEvent setProfileServiceDTO(ProfileServiceDTO<?> profileServiceDTO)
	{
		this.profileServiceDTO = profileServiceDTO;
		return this;
	}
	
	public UpdateNewVisitEvent setEnterprise(IEnterprise<?,?> enterprise)
	{
		this.enterprise = enterprise;
		return this;
	}
	
	public UpdateNewVisitEvent setProfileSystem(ISystems<?,?> profileSystem)
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
