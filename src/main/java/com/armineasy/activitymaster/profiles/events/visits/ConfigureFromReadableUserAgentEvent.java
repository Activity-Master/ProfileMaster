package com.armineasy.activitymaster.profiles.events.visits;

import com.armineasy.activitymaster.activitymaster.db.entities.events.Event;
import com.armineasy.activitymaster.activitymaster.db.entities.involvedparty.InvolvedParty;
import com.armineasy.activitymaster.activitymaster.db.entities.resourceitem.ResourceItem;
import com.armineasy.activitymaster.activitymaster.services.classifications.resourceitems.ResourceItemClassifications;
import com.armineasy.activitymaster.activitymaster.services.dto.IEnterprise;
import com.armineasy.activitymaster.activitymaster.services.dto.ISystems;
import com.armineasy.activitymaster.activitymaster.threads.TransactionalIdentifiedThread;
import com.armineasy.activitymaster.profiles.ProfileSystem;
import com.armineasy.activitymaster.profiles.dto.UserDTO;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.sf.uadetector.ReadableUserAgent;

import java.util.UUID;

import static com.armineasy.activitymaster.activitymaster.services.classifications.resourceitems.ResourceItemClassifications.*;
import static com.armineasy.activitymaster.activitymaster.services.classifications.resourceitems.ResourceItemTypes.*;

@Data
@Accessors(chain = true)
public class ConfigureFromReadableUserAgentEvent
		extends TransactionalIdentifiedThread
{
	@Getter
	private static final String JobServiceName = "ConfigureFromReadableUserAgent";

	private Event event;
	private UserDTO<?> dto;
	private InvolvedParty ip;
	private ReadableUserAgent readableUserAgent;
	private ISystems profileSystem;
	private IEnterprise<?> enterprise;
	private UUID[] identityToken;

	@Override
	public void perform()
	{
		UUID systemID = ProfileSystem.getSystemTokens()
		                             .get(enterprise);
		ResourceItem resourceItem = ip.add(BrowserDeviceCategory, AddedANewDevice,
		                                   readableUserAgent.getDeviceCategory()
		                                                                .getName()
		                                                                .getBytes(),
		                                   "application/text", profileSystem, systemID);

		resourceItem.add(AddedANewDevice, BrowserDeviceCategory.classificationName(), profileSystem, systemID);
		resourceItem.add(ResourceItemClassifications.Size, Long.toString(readableUserAgent.getDeviceCategory()
		                                                                                  .getName()
		                                                                                  .length()), profileSystem, systemID);
		event.add(resourceItem, Added, profileSystem, identityToken);

		ResourceItem resourceItemName = ip.add(BrowserDeviceName, AddedANewDevice,
		                                       readableUserAgent.getDeviceCategory()
		                                                                    .getCategory()
		                                                                    .getName()
		                                                                    .getBytes(),
		                                       "application/text", profileSystem, systemID);
		event.add(resourceItemName, Added, profileSystem, identityToken);

		resourceItemName.add(AddedANewDevice, BrowserDeviceName.classificationName(), profileSystem, systemID);
		resourceItemName.add(ResourceItemClassifications.Size, Long.toString(readableUserAgent.getDeviceCategory()
		                                                                                      .getName()
		                                                                                      .length()), profileSystem, systemID);

		ResourceItem resourceItemIcon = ip.add(BrowserDeviceIcon, AddedANewDevice,
		                                       readableUserAgent.getDeviceCategory()
		                                                                    .getIcon()
		                                                                    .getBytes(),
		                                       "application/text", profileSystem, systemID);
		event.add(resourceItemIcon, Added, profileSystem, identityToken);

		resourceItemIcon.add(AddedANewDevice, BrowserDeviceIcon.classificationName(), profileSystem, systemID);
		resourceItemIcon.add(ResourceItemClassifications.Size, Long.toString(readableUserAgent.getDeviceCategory()
		                                                                                      .getIcon()
		                                                                                      .length()), profileSystem, systemID);

		ResourceItem resourceItemOperatingSystem = ip.add(OperatingSystem, AddedANewDevice,
		                                                  readableUserAgent.getOperatingSystem()
		                                                                               .getName()
		                                                                               .getBytes(),
		                                                  "application/text", profileSystem, systemID);
		event.add(resourceItemOperatingSystem, Added, profileSystem, identityToken);
		resourceItemOperatingSystem.add(AddedANewDevice, OperatingSystem.classificationName(), profileSystem, systemID);
		resourceItemOperatingSystem.add(ResourceItemClassifications.Size, Long.toString(readableUserAgent.getOperatingSystem()
		                                                                                                 .getName()
		                                                                                                 .length()), profileSystem, systemID);
		ResourceItem resourceItemFamily = ip.add(OperatingSystemFamily, AddedANewDevice,
		                                         readableUserAgent.getOperatingSystem()
		                                                                      .getFamily()
		                                                                      .getName()
		                                                                      .getBytes(),
		                                         "application/text", profileSystem, identityToken);
		event.add(resourceItemFamily, Added, profileSystem, identityToken);

		resourceItemFamily.add(AddedANewDevice, OperatingSystemFamily.classificationName(), profileSystem, systemID);
		resourceItemFamily.add(Size, Long.toString(readableUserAgent.getOperatingSystem()
		                                                            .getFamily()
		                                                            .getName()
		                                                            .length()), profileSystem, systemID);
	}
}
