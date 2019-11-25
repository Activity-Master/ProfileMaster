package com.armineasy.activitymaster.profiles.events.visits;

import com.armineasy.activitymaster.activitymaster.services.dto.IEnterprise;
import com.armineasy.activitymaster.activitymaster.services.dto.IEvent;
import com.armineasy.activitymaster.activitymaster.services.dto.IInvolvedParty;
import com.armineasy.activitymaster.activitymaster.services.dto.ISystems;
import com.armineasy.activitymaster.activitymaster.threads.TransactionalIdentifiedThread;
import com.armineasy.activitymaster.profiles.ProfileSystem;
import com.armineasy.activitymaster.profiles.deserializers.LocalDateTimeDeserializer;
import com.armineasy.activitymaster.profiles.dto.ProfileServiceDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;

import static com.armineasy.activitymaster.profiles.enumerations.ProfileClassifications.*;


public class UpdateLastVisitEvent
		extends TransactionalIdentifiedThread
{
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");
	private static final String JobServiceName = "UpdateLastVisitEvent";

	private IEvent<?> event;
	private ProfileServiceDTO<?> profileServiceDTO;
	private IEnterprise<?> enterprise;
	private IInvolvedParty<?> newIp;
	private UUID[] identityToken;

	public UpdateLastVisitEvent()
	{
	}

	public static String getJobServiceName()
	{
		return UpdateLastVisitEvent.JobServiceName;
	}

	@Override
	public void perform()
	{
		ISystems<?> profileSystem = ProfileSystem.getNewSystem()
		                                         .get(enterprise);
		//Add last login time
		String lastVisit = DateTimeFormatter.ofPattern(LocalDateTimeDeserializer.LocalDateTimeFormat)
		                                    .format(LocalDateTime.now());
		newIp.addOrUpdate(LastVisitTime,
		                  lastVisit,
		                  profileSystem,
		                  identityToken);
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

	public IInvolvedParty<?> getNewIp()
	{
		return this.newIp;
	}

	public UUID[] getIdentityToken()
	{
		return this.identityToken;
	}

	public UpdateLastVisitEvent setEvent(IEvent<?> event)
	{
		this.event = event;
		return this;
	}

	public UpdateLastVisitEvent setProfileServiceDTO(ProfileServiceDTO<?> profileServiceDTO)
	{
		this.profileServiceDTO = profileServiceDTO;
		return this;
	}

	public UpdateLastVisitEvent setEnterprise(IEnterprise<?> enterprise)
	{
		this.enterprise = enterprise;
		return this;
	}

	public UpdateLastVisitEvent setNewIp(IInvolvedParty<?> newIp)
	{
		this.newIp = newIp;
		return this;
	}

	public UpdateLastVisitEvent setIdentityToken(UUID[] identityToken)
	{
		this.identityToken = identityToken;
		return this;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}
		if (!super.equals(o))
		{
			return false;
		}
		UpdateLastVisitEvent that = (UpdateLastVisitEvent) o;
		return Arrays.equals(getIdentityToken(), that.getIdentityToken());
	}

	@Override
	public int hashCode()
	{
		int result = super.hashCode();
		result = 31 * result + Arrays.hashCode(getIdentityToken());
		return result;
	}

}
