package com.guicedee.activitymaster.profiles.events;

import com.guicedee.activitymaster.core.services.classifications.involvedparty.IInvolvedPartyClassification;
import com.guicedee.activitymaster.core.services.dto.IEnterprise;
import com.guicedee.activitymaster.core.services.dto.IEvent;
import com.guicedee.activitymaster.core.services.dto.IInvolvedParty;
import com.guicedee.activitymaster.core.services.dto.ISystems;
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
	private IEvent<?> event;
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
		event.addOrReuse(PerformedBy,
		                 newIp.getSecurityIdentity()
		                      .toString(),
		                 profileSystem,
		                 identityToken);
	}
	
	public IInvolvedParty<?> getNewIp()
	{
		return this.newIp;
	}
	
	public IEvent<?> getEvent()
	{
		return this.event;
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
	
	public UpdateNewVisitEvent setEvent(IEvent<?> event)
	{
		this.event = event;
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
	public boolean equals(final Object o)
	{
		if (o == this)
		{
			return true;
		}
		if (!(o instanceof UpdateNewVisitEvent))
		{
			return false;
		}
		final UpdateNewVisitEvent other = (UpdateNewVisitEvent) o;
		if (!other.canEqual((Object) this))
		{
			return false;
		}
		final Object this$newIp = this.getNewIp();
		final Object other$newIp = other.getNewIp();
		if (this$newIp == null ? other$newIp != null : !this$newIp.equals(other$newIp))
		{
			return false;
		}
		final Object this$event = this.getEvent();
		final Object other$event = other.getEvent();
		if (this$event == null ? other$event != null : !this$event.equals(other$event))
		{
			return false;
		}
		final Object this$profileServiceDTO = this.getProfileServiceDTO();
		final Object other$profileServiceDTO = other.getProfileServiceDTO();
		if (this$profileServiceDTO == null ? other$profileServiceDTO != null : !this$profileServiceDTO.equals(other$profileServiceDTO))
		{
			return false;
		}
		final Object this$enterprise = this.getEnterprise();
		final Object other$enterprise = other.getEnterprise();
		if (this$enterprise == null ? other$enterprise != null : !this$enterprise.equals(other$enterprise))
		{
			return false;
		}
		final Object this$profileSystem = this.getProfileSystem();
		final Object other$profileSystem = other.getProfileSystem();
		if (this$profileSystem == null ? other$profileSystem != null : !this$profileSystem.equals(other$profileSystem))
		{
			return false;
		}
		if (!java.util.Arrays.deepEquals(this.getIdentityToken(), other.getIdentityToken()))
		{
			return false;
		}
		return true;
	}
	
	@Override
	protected boolean canEqual(final Object other)
	{
		return other instanceof UpdateNewVisitEvent;
	}
	
	@Override
	public int hashCode()
	{
		final int PRIME = 59;
		int result = 1;
		final Object $newIp = this.getNewIp();
		result = result * PRIME + ($newIp == null ? 43 : $newIp.hashCode());
		final Object $event = this.getEvent();
		result = result * PRIME + ($event == null ? 43 : $event.hashCode());
		final Object $profileServiceDTO = this.getProfileServiceDTO();
		result = result * PRIME + ($profileServiceDTO == null ? 43 : $profileServiceDTO.hashCode());
		final Object $enterprise = this.getEnterprise();
		result = result * PRIME + ($enterprise == null ? 43 : $enterprise.hashCode());
		final Object $profileSystem = this.getProfileSystem();
		result = result * PRIME + ($profileSystem == null ? 43 : $profileSystem.hashCode());
		result = result * PRIME + java.util.Arrays.deepHashCode(this.getIdentityToken());
		return result;
	}
	
	@Override
	public String toString()
	{
		return "UpdateNewVisitEvent(newIp=" + this.getNewIp() + ", event=" + this.getEvent() + ", profileServiceDTO=" + this.getProfileServiceDTO() + ", enterprise=" +
				this.getEnterprise() + ", profileSystem=" + this.getProfileSystem() + ", identityToken=" + java.util.Arrays.deepToString(this.getIdentityToken()) + ")";
	}
}
