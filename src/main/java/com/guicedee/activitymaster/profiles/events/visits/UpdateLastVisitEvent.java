package com.guicedee.activitymaster.profiles.events.visits;

import com.guicedee.activitymaster.client.services.annotations.ActivityMasterDB;
import com.guicedee.activitymaster.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.client.services.builders.warehouse.events.IEvent;
import com.guicedee.activitymaster.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.profiles.ProfileSystem;
import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;
import com.guicedee.guicedinjection.json.LocalDateTimeDeserializer;
import com.guicedee.guicedpersistence.db.annotations.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;

import static com.guicedee.activitymaster.profiles.enumerations.ProfileClassifications.*;
import static com.guicedee.guicedinjection.GuiceContext.*;

public class UpdateLastVisitEvent
		extends Thread
{
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");
	private static final String JobServiceName = "UpdateLastVisitEvent";

	private IEvent<?,?> event;
	private ProfileServiceDTO<?> profileServiceDTO;
	private IEnterprise<?,?> enterprise;
	private IInvolvedParty<?,?> newIp;
	private UUID[] identityToken;

	public UpdateLastVisitEvent()
	{
	}

	public static String getJobServiceName()
	{
		return UpdateLastVisitEvent.JobServiceName;
	}

	@Override
	@Transactional(entityManagerAnnotation = ActivityMasterDB.class)
	public void run()
	{
		ISystems<?,?> profileSystem = get(ProfileSystem.class)
				                            .getSystem(enterprise);
		//Add last login time
		String lastVisit = LocalDateTimeDeserializer.formats[0].format(LocalDateTime.now());
		newIp.addOrUpdateClassification(LastVisitTime,
				null,
		                  lastVisit,
		                  profileSystem,
		                  identityToken);
	}

	public IEvent<?,?> getEvent()
	{
		return this.event;
	}

	public UpdateLastVisitEvent setEvent(IEvent<?,?> event)
	{
		this.event = event;
		return this;
	}

	public ProfileServiceDTO<?> getProfileServiceDTO()
	{
		return this.profileServiceDTO;
	}

	public UpdateLastVisitEvent setProfileServiceDTO(ProfileServiceDTO<?> profileServiceDTO)
	{
		this.profileServiceDTO = profileServiceDTO;
		return this;
	}

	public IEnterprise<?,?> getEnterprise()
	{
		return this.enterprise;
	}

	public UpdateLastVisitEvent setEnterprise(IEnterprise<?,?> enterprise)
	{
		this.enterprise = enterprise;
		return this;
	}

	public IInvolvedParty<?,?> getNewIp()
	{
		return this.newIp;
	}

	public UpdateLastVisitEvent setNewIp(IInvolvedParty<?,?> newIp)
	{
		this.newIp = newIp;
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

	public UUID[] getIdentityToken()
	{
		return this.identityToken;
	}

	public UpdateLastVisitEvent setIdentityToken(UUID[] identityToken)
	{
		this.identityToken = identityToken;
		return this;
	}

	@Override
	public int hashCode()
	{
		int result = super.hashCode();
		result = 31 * result + Arrays.hashCode(getIdentityToken());
		return result;
	}

}
