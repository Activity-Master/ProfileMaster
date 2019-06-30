package com.armineasy.activitymaster.profiles.events.visits;

import com.armineasy.activitymaster.activitymaster.db.entities.resourceitem.ResourceItem;
import com.armineasy.activitymaster.activitymaster.services.classifications.resourceitems.ResourceItemClassifications;
import com.armineasy.activitymaster.activitymaster.services.dto.IEnterprise;
import com.armineasy.activitymaster.activitymaster.services.dto.IEvent;
import com.armineasy.activitymaster.activitymaster.services.dto.IInvolvedParty;
import com.armineasy.activitymaster.activitymaster.services.dto.ISystems;
import com.armineasy.activitymaster.activitymaster.threads.TransactionalIdentifiedThread;
import com.armineasy.activitymaster.profiles.ProfileSystem;
import com.armineasy.activitymaster.profiles.dto.UserDTO;
import lombok.experimental.Accessors;
import net.sf.uadetector.ReadableUserAgent;

import java.util.Objects;
import java.util.UUID;

import static com.armineasy.activitymaster.activitymaster.services.classifications.resourceitems.ResourceItemClassifications.*;
import static com.armineasy.activitymaster.activitymaster.services.classifications.resourceitems.ResourceItemTypes.*;
import static com.jwebmp.core.utilities.StaticStrings.*;

@Accessors(chain = true)
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
		UUID systemID = ProfileSystem.getSystemTokens()
		                             .get(enterprise);
		ResourceItem resourceItem = ip.add(AddedANewDevice, BrowserDeviceCategory,
		                                   readableUserAgent.getDeviceCategory()
		                                                    .getName()
		                                                    .getBytes(),
		                                   "application/text", profileSystem, systemID);

		resourceItem.add(AddedANewDevice, BrowserDeviceCategory.classificationName(), profileSystem, systemID);
		resourceItem.add(ResourceItemClassifications.Size, Long.toString(readableUserAgent.getDeviceCategory()
		                                                                                  .getName()
		                                                                                  .length()), profileSystem, systemID);
		event.add(Added, resourceItem, STRING_EMPTY, profileSystem, identityToken);

		ResourceItem resourceItemName = ip.add(AddedANewDevice, BrowserDeviceName,
		                                       readableUserAgent.getDeviceCategory()
		                                                        .getCategory()
		                                                        .getName()
		                                                        .getBytes(),
		                                       "application/text", profileSystem, systemID);
		event.add(Added, resourceItemName, STRING_EMPTY, profileSystem, identityToken);

		resourceItemName.add(AddedANewDevice, BrowserDeviceName.classificationName(), profileSystem, systemID);
		resourceItemName.add(ResourceItemClassifications.Size, Long.toString(readableUserAgent.getDeviceCategory()
		                                                                                      .getName()
		                                                                                      .length()), profileSystem, systemID);

		ResourceItem resourceItemIcon = ip.add(AddedANewDevice, BrowserDeviceIcon,
		                                       readableUserAgent.getDeviceCategory()
		                                                        .getIcon()
		                                                        .getBytes(),
		                                       "application/text", profileSystem, systemID);
		event.add(Added, resourceItemIcon, STRING_EMPTY, profileSystem, identityToken);

		resourceItemIcon.add(AddedANewDevice, BrowserDeviceIcon.classificationName(), profileSystem, systemID);
		resourceItemIcon.add(ResourceItemClassifications.Size, Long.toString(readableUserAgent.getDeviceCategory()
		                                                                                      .getIcon()
		                                                                                      .length()), profileSystem, systemID);

		ResourceItem resourceItemOperatingSystem = ip.add(AddedANewDevice, OperatingSystem,
		                                                  readableUserAgent.getOperatingSystem()
		                                                                   .getName()
		                                                                   .getBytes(),
		                                                  "application/text", profileSystem, systemID);
		event.add(Added, resourceItemOperatingSystem, STRING_EMPTY, profileSystem, identityToken);
		resourceItemOperatingSystem.add(AddedANewDevice, OperatingSystem.classificationName(), profileSystem, systemID);
		resourceItemOperatingSystem.add(ResourceItemClassifications.Size, Long.toString(readableUserAgent.getOperatingSystem()
		                                                                                                 .getName()
		                                                                                                 .length()), profileSystem, systemID);
		ResourceItem resourceItemFamily = ip.add(AddedANewDevice, OperatingSystemFamily,
		                                         readableUserAgent.getOperatingSystem()
		                                                          .getFamily()
		                                                          .getName()
		                                                          .getBytes(),
		                                         "application/text", profileSystem, identityToken);
		event.add(Added, resourceItemFamily, STRING_EMPTY, profileSystem, identityToken);

		resourceItemFamily.add(AddedANewDevice, OperatingSystemFamily.classificationName(), profileSystem, systemID);
		resourceItemFamily.add(Size, Long.toString(readableUserAgent.getOperatingSystem()
		                                                            .getFamily()
		                                                            .getName()
		                                                            .length()), profileSystem, systemID);
	}

	public IEvent<?> getEvent()
	{
		return this.event;
	}

	public UserDTO<?> getDto()
	{
		return this.dto;
	}

	public IInvolvedParty<?> getIp()
	{
		return this.ip;
	}

	public ReadableUserAgent getReadableUserAgent()
	{
		return this.readableUserAgent;
	}

	public ISystems getProfileSystem()
	{
		return this.profileSystem;
	}

	public IEnterprise<?> getEnterprise()
	{
		return this.enterprise;
	}

	public UUID[] getIdentityToken()
	{
		return this.identityToken;
	}

	public ConfigureFromReadableUserAgentEvent setEvent(IEvent<?> event)
	{
		this.event = event;
		return this;
	}

	public ConfigureFromReadableUserAgentEvent setDto(UserDTO<?> dto)
	{
		this.dto = dto;
		return this;
	}

	public ConfigureFromReadableUserAgentEvent setIp(IInvolvedParty<?> ip)
	{
		this.ip = ip;
		return this;
	}

	public ConfigureFromReadableUserAgentEvent setReadableUserAgent(ReadableUserAgent readableUserAgent)
	{
		this.readableUserAgent = readableUserAgent;
		return this;
	}

	public ConfigureFromReadableUserAgentEvent setProfileSystem(ISystems profileSystem)
	{
		this.profileSystem = profileSystem;
		return this;
	}

	public ConfigureFromReadableUserAgentEvent setEnterprise(IEnterprise<?> enterprise)
	{
		this.enterprise = enterprise;
		return this;
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

	@Override
	public int hashCode()
	{
		return Objects.hash(super.hashCode(), getEvent());
	}
}
