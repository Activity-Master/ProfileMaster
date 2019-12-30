package com.guicedee.activitymaster.profiles.events.visits;

import com.guicedee.activitymaster.core.services.classifications.resourceitems.ResourceItemClassifications;
import com.guicedee.activitymaster.core.services.dto.*;
import com.guicedee.activitymaster.core.threads.TransactionalIdentifiedThread;
import com.guicedee.activitymaster.profiles.ProfileSystem;
import com.guicedee.activitymaster.profiles.dto.UserDTO;
import com.guicedee.guicedinjection.GuiceContext;
import net.sf.uadetector.ReadableUserAgent;

import java.util.Objects;
import java.util.UUID;

import static com.guicedee.activitymaster.core.services.classifications.resourceitems.ResourceItemClassifications.*;
import static com.guicedee.activitymaster.core.services.classifications.resourceitems.ResourceItemTypes.*;
import static com.jwebmp.core.utilities.StaticStrings.*;

public class ConfigureFromReadableUserAgentEvent
		extends TransactionalIdentifiedThread
{
	private static final String JobServiceName = "ConfigureFromReadableUserAgent";

	private IEvent<?> event;
	private UserDTO<?> dto;
	private IInvolvedParty<?> ip;
	private ReadableUserAgent readableUserAgent;
	private ISystems profileSystem;
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
		IRelationshipValue<IInvolvedParty<?>, IResourceItem<?>, ?> resourceItem = ip.add(AddedANewDevice, BrowserDeviceCategory,
		                                                                                 "Device Category",
		                                                                                 readableUserAgent.getDeviceCategory()
		                                                                                                  .getName()
		                                                                                                  .getBytes(),
		                                                                                 "application/text", profileSystem, systemID);

		resourceItem.getSecondary()
		            .add(AddedANewDevice, BrowserDeviceCategory.classificationName(), profileSystem, systemID);
		resourceItem.getSecondary()
		            .add(ResourceItemClassifications.Size, Long.toString(readableUserAgent.getDeviceCategory()
		                                                                                  .getName()
		                                                                                  .length()), profileSystem, systemID);
		event.add(Added, resourceItem.getSecondary(), STRING_EMPTY, profileSystem, identityToken);

		IRelationshipValue<IInvolvedParty<?>, IResourceItem<?>, ?> resourceItemName = ip.add(AddedANewDevice, BrowserDeviceName
				, "Browser Device",
				                                                                             readableUserAgent.getDeviceCategory()
				                                                                                              .getCategory()
				                                                                                              .getName()
				                                                                                              .getBytes(),
				                                                                             "application/text", profileSystem, systemID);
		event.add(Added, resourceItemName.getSecondary(), STRING_EMPTY, profileSystem, identityToken);

		resourceItemName.getSecondary()
		                .add(AddedANewDevice, BrowserDeviceName.classificationName(), profileSystem, systemID);
		resourceItemName.getSecondary()
		                .add(ResourceItemClassifications.Size, Long.toString(readableUserAgent.getDeviceCategory()
		                                                                                      .getName()
		                                                                                      .length()), profileSystem, systemID);

		IRelationshipValue<IInvolvedParty<?>, IResourceItem<?>, ?> resourceItemIcon = ip.add(AddedANewDevice, BrowserDeviceIcon,
		                                                                                     "Browser Icon",
		                                                                                     readableUserAgent.getDeviceCategory()
		                                                                                                      .getIcon()
		                                                                                                      .getBytes(),
		                                                                                     "application/text", profileSystem, systemID);
		event.add(Added, resourceItemIcon.getSecondary(), STRING_EMPTY, profileSystem, identityToken);

		resourceItemIcon.getSecondary()
		                .add(AddedANewDevice, BrowserDeviceIcon.classificationName(), profileSystem, systemID);
		resourceItemIcon.getSecondary()
		                .add(ResourceItemClassifications.Size, Long.toString(readableUserAgent.getDeviceCategory()
		                                                                                      .getIcon()
		                                                                                      .length()), profileSystem, systemID);

		IRelationshipValue<IInvolvedParty<?>, IResourceItem<?>, ?> resourceItemOperatingSystem = ip.add(AddedANewDevice, OperatingSystem,
		                                                                                                "Operating System",
		                                                                                                readableUserAgent.getOperatingSystem()
		                                                                                                                 .getName()
		                                                                                                                 .getBytes(),
		                                                                                                "application/text", profileSystem, systemID);
		event.add(Added, resourceItemOperatingSystem.getSecondary(), STRING_EMPTY, profileSystem, identityToken);
		resourceItemOperatingSystem.getSecondary()
		                           .add(AddedANewDevice, OperatingSystem.classificationName(), profileSystem, systemID);
		resourceItemOperatingSystem.getSecondary()
		                           .add(ResourceItemClassifications.Size, Long.toString(readableUserAgent.getOperatingSystem()
		                                                                                                 .getName()
		                                                                                                 .length()), profileSystem, systemID);
		IRelationshipValue<IInvolvedParty<?>, IResourceItem<?>, ?> resourceItemFamily = ip.add(AddedANewDevice, OperatingSystemFamily,
		                                                                                       "Operating System Family",
		                                                                                       readableUserAgent.getOperatingSystem()
		                                                                                                        .getFamily()
		                                                                                                        .getName()
		                                                                                                        .getBytes(),
		                                                                                       "application/text", profileSystem, identityToken);
		event.add(Added, resourceItemFamily.getSecondary(), STRING_EMPTY, profileSystem, identityToken);

		resourceItemFamily.getSecondary()
		                  .add(AddedANewDevice, OperatingSystemFamily.classificationName(), profileSystem, systemID);
		resourceItemFamily.getSecondary()
		                  .add(Size, Long.toString(readableUserAgent.getOperatingSystem()
		                                                            .getFamily()
		                                                            .getName()
		                                                            .length()), profileSystem, systemID);
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
		ConfigureFromReadableUserAgentEvent that = (ConfigureFromReadableUserAgentEvent) o;
		return Objects.equals(getEvent(), that.getEvent());
	}

	public IEvent<?> getEvent()
	{
		return this.event;
	}

	public ConfigureFromReadableUserAgentEvent setEvent(IEvent<?> event)
	{
		this.event = event;
		return this;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(super.hashCode(), getEvent());
	}
}
