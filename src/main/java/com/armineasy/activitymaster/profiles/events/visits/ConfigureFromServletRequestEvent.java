package com.armineasy.activitymaster.profiles.events.visits;

import com.armineasy.activitymaster.activitymaster.db.entities.address.Address;
import com.armineasy.activitymaster.activitymaster.db.entities.involvedparty.InvolvedParty;
import com.armineasy.activitymaster.activitymaster.db.entities.resourceitem.ResourceItem;
import com.armineasy.activitymaster.activitymaster.implementations.AddressService;
import com.armineasy.activitymaster.activitymaster.services.classifications.resourceitems.ResourceItemClassifications;
import com.armineasy.activitymaster.activitymaster.services.classifications.resourceitems.ResourceItemTypes;
import com.armineasy.activitymaster.activitymaster.services.dto.IEnterprise;
import com.armineasy.activitymaster.activitymaster.services.dto.IEvent;
import com.armineasy.activitymaster.activitymaster.services.dto.IInvolvedParty;
import com.armineasy.activitymaster.activitymaster.services.dto.ISystems;
import com.armineasy.activitymaster.activitymaster.threads.TransactionalIdentifiedThread;
import com.armineasy.activitymaster.profiles.ProfileSystem;
import com.armineasy.activitymaster.profiles.dto.UserDTO;
import com.jwebmp.guicedinjection.GuiceContext;
import com.jwebmp.logger.LogFactory;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.UUID;
import java.util.logging.Level;

import static com.armineasy.activitymaster.activitymaster.services.classifications.resourceitems.ResourceItemClassifications.*;

@Data
@Accessors(chain = true)
public class ConfigureFromServletRequestEvent extends TransactionalIdentifiedThread
{
	@Getter
	private static final String JobServiceName = "ConfigureFromServletRequestEvent";

	private IEvent<?> event;
	private UserDTO<?> dto;
	private IInvolvedParty<?> ip;
	private ISystems profileSystem;
	private HttpServletRequest servletRequest;
	private IEnterprise<?> enterprise;

	@Override
	public void perform()
	{
		UUID systemID = ProfileSystem.getSystemTokens()
		                             .get(enterprise);

		StringBuilder sb = new StringBuilder();
		Enumeration<String> headerNames = servletRequest.getHeaderNames();
		while (headerNames.hasMoreElements())
		{
			String h = headerNames.nextElement();
			String v = servletRequest.getHeader(h);
			JSONObject jsonObject = new JSONObject();
			jsonObject.put(h, v);
			sb.append(jsonObject.toString());
		}

		AddressService addressService = GuiceContext.get(AddressService.class);
		String ipReal = servletRequest.getRemoteAddr();
		if (ipReal.equalsIgnoreCase("0:0:0:0:0:0:0:1")) {
			InetAddress inetAddress = null;
			try
			{
				inetAddress = InetAddress.getLocalHost();
			}
			catch (UnknownHostException e)
			{
				LogFactory.getLog("ConfigureFromServletRequest").log(Level.SEVERE, "Unknown host in getting INet Address for localhost ipv6",e);
			}
			String ipAddress = inetAddress.getHostAddress();
			ipReal = ipAddress;
		}
		Address ipAddress = addressService.addOrFindIPAddress(ipReal, profileSystem, systemID);
		ip.add(ipAddress, profileSystem, systemID);
		event.add(ipAddress, profileSystem, systemID);
		Address hostName = addressService.addOrFindHostName(servletRequest.getRemoteHost(), profileSystem, systemID);
		ip.add(hostName, profileSystem, systemID);
		event.add(hostName, profileSystem, systemID);
		Address localIpAddress = addressService.addOrFindHostName(servletRequest.getLocalAddr(), profileSystem, systemID);
		ip.add(localIpAddress, profileSystem, systemID);
		event.add(localIpAddress, profileSystem, systemID);
		Address localHostName = addressService.addOrFindHostName(servletRequest.getLocalName(), profileSystem, systemID);
		ip.add(localHostName, profileSystem, systemID);
		event.add(localHostName, profileSystem, systemID);

		Address webAddress = addressService.addOrFindWebAddress(servletRequest.getRequestURL()
		                                                                      .toString(), profileSystem, systemID);
		ip.add(webAddress, profileSystem, systemID);
		event.add(webAddress, profileSystem, systemID);

		ResourceItem resourceItem = ip.add(ResourceItemTypes.BrowserInformation, AddedANewDevice,
		                                   sb.toString()
		                                                 .getBytes(),
		                                   "application/json", profileSystem, systemID);
		resourceItem.add(ResourceItemClassifications.Size, Long.toString(sb.toString()
		                                                                   .length()), profileSystem, systemID);
		event.add(resourceItem, Added, profileSystem, systemID);
	}
}
