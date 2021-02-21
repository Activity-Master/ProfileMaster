package com.guicedee.activitymaster.profiles.events.visits;

import com.guicedee.activitymaster.core.services.dto.*;
import com.guicedee.activitymaster.core.threads.TransactionalIdentifiedThread;
import com.guicedee.activitymaster.profiles.ProfileSystem;
import com.guicedee.activitymaster.profiles.dto.UserDTO;
import com.guicedee.guicedinjection.GuiceContext;
import net.sf.uadetector.ReadableUserAgent;

import java.util.UUID;

import static com.guicedee.activitymaster.profiles.enumerations.SiteClientClassifications.*;

public class ConfigureFromReadableUserAgentEvent
		extends TransactionalIdentifiedThread
{
	private static final String JobServiceName = "ConfigureFromReadableUserAgent";

//	private IEvent<?> event;
	private UserDTO<?> dto;
	private IInvolvedParty<?> ip;
	private ReadableUserAgent readableUserAgent;
	private ISystems<?> profileSystem;
	private IEnterprise<?> enterprise;
	private UUID[] identityToken;

	public ConfigureFromReadableUserAgentEvent()
	{
	}

	public static String getJobServiceName()
	{
		return ConfigureFromReadableUserAgentEvent.JobServiceName;
	}

	@Override
	public void perform()
	{
		UUID systemID = GuiceContext.get(ProfileSystem.class)
		                            .getSystemToken(enterprise);
		var browserDeviceCategory
				= ip.addOrReuse(BrowserDeviceCategory, readableUserAgent.getDeviceCategory().getName(), profileSystem, systemID);
		//event.add(browserDeviceCategory.getSecondary(),readableUserAgent.getDeviceCategory().getName(),profileSystem, identityToken);

		var browserDevice
				= ip.addOrReuse(BrowserDevice, readableUserAgent.getDeviceCategory().getCategory().getName(), profileSystem, systemID);
	//	event.add(browserDevice.getSecondary(),readableUserAgent.getDeviceCategory().getCategory().getName(),profileSystem, identityToken);

		var operatingSystem
				= ip.addOrReuse(OperatingSystem, readableUserAgent.getOperatingSystem().getName(), profileSystem, systemID);
	//	event.add(operatingSystem.getSecondary(),readableUserAgent.getOperatingSystem().getName(),profileSystem, identityToken);

		var operatingSystemFamily
				= ip.addOrReuse(OperatingSystemFamily, readableUserAgent.getOperatingSystem().getFamily().getName(), profileSystem, systemID);
	//	event.add(operatingSystemFamily.getSecondary(),readableUserAgent.getOperatingSystem().getFamily().getName(),profileSystem, identityToken);
	}

	public UserDTO<?> getDto()
	{
		return this.dto;
	}

	public ConfigureFromReadableUserAgentEvent setDto(UserDTO<?> dto)
	{
		this.dto = dto;
		return this;
	}

	public IInvolvedParty<?> getIp()
	{
		return this.ip;
	}

	public ConfigureFromReadableUserAgentEvent setIp(IInvolvedParty<?> ip)
	{
		this.ip = ip;
		return this;
	}

	public ReadableUserAgent getReadableUserAgent()
	{
		return this.readableUserAgent;
	}

	public ConfigureFromReadableUserAgentEvent setReadableUserAgent(ReadableUserAgent readableUserAgent)
	{
		this.readableUserAgent = readableUserAgent;
		return this;
	}

	public ISystems getProfileSystem()
	{
		return this.profileSystem;
	}

	public ConfigureFromReadableUserAgentEvent setProfileSystem(ISystems profileSystem)
	{
		this.profileSystem = profileSystem;
		return this;
	}

	public IEnterprise<?> getEnterprise()
	{
		return this.enterprise;
	}

	public ConfigureFromReadableUserAgentEvent setEnterprise(IEnterprise<?> enterprise)
	{
		this.enterprise = enterprise;
		return this;
	}

	public UUID[] getIdentityToken()
	{
		return this.identityToken;
	}

	public ConfigureFromReadableUserAgentEvent setIdentityToken(UUID[] identityToken)
	{
		this.identityToken = identityToken;
		return this;
	}
	
/*	public IEvent<?> getEvent()
	{
		return this.event;
	}

	public ConfigureFromReadableUserAgentEvent setEvent(IEvent<?> event)
	{
		this.event = event;
		return this;
	}*/

}
