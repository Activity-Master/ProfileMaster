package com.guicedee.activitymaster.profiles.events.visits;

import com.guicedee.activitymaster.core.services.dto.*;
import com.guicedee.activitymaster.core.services.system.IAddressService;
import com.guicedee.activitymaster.core.threads.TransactionalIdentifiedThread;
import com.guicedee.activitymaster.profiles.ProfileSystem;
import com.guicedee.activitymaster.profiles.dto.UserDTO;
import com.guicedee.guicedinjection.GuiceContext;
import com.guicedee.logger.LogFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.UUID;
import java.util.logging.Level;

import static com.guicedee.activitymaster.core.services.classifications.address.AddressLocalSystemClassifications.*;
import static com.guicedee.activitymaster.core.services.classifications.address.AddressRemoteSystemClassifications.*;
import static com.guicedee.activitymaster.core.services.classifications.address.AddressWebClassifications.*;
import static com.guicedee.guicedinjection.json.StaticStrings.*;

public class ConfigureFromServletRequestEvent
		extends TransactionalIdentifiedThread
{
	private static final String JobServiceName = "ConfigureFromServletRequestEvent";

	//private IEvent<?> event;
	private UserDTO<?> dto;
	private IInvolvedParty<?> ip;
	private ISystems<?> profileSystem;
	private HttpServletRequest servletRequest;
	private IEnterprise<?> enterprise;

	public ConfigureFromServletRequestEvent()
	{
	}

	public static String getJobServiceName()
	{
		return ConfigureFromServletRequestEvent.JobServiceName;
	}

	@Override
	public void perform()
	{
		UUID systemID = GuiceContext.get(ProfileSystem.class)
		                            .getSystemToken(enterprise);

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

		IAddressService<?> addressService = GuiceContext.get(IAddressService.class);
		String ipReal = servletRequest.getRemoteAddr();
		if (ipReal.equalsIgnoreCase("0:0:0:0:0:0:0:1"))
		{
			InetAddress inetAddress = null;
			try
			{
				inetAddress = InetAddress.getLocalHost();
			}
			catch (UnknownHostException e)
			{
				LogFactory.getLog("ConfigureFromServletRequest")
				          .log(Level.SEVERE, "Unknown host in getting INet Address for localhost ipv6", e);
			}
			String ipAddress = inetAddress.getHostAddress();
			ipReal = ipAddress;
		}
		IAddress<?> ipAddress = addressService.addOrFindIPAddress(ipReal, profileSystem, systemID);
		ip.addOrReuse(RemoteAddressIPAddress,ipAddress,STRING_EMPTY,STRING_EMPTY, profileSystem, systemID);
	//	event.add(ipAddress, RemoteAddressIPAddress, profileSystem, systemID);
		IAddress<?> hostName = addressService.addOrFindHostName(servletRequest.getRemoteHost(), profileSystem, systemID);
		ip.addOrReuse(RemoteAddressHostName,hostName,STRING_EMPTY,STRING_EMPTY, profileSystem, systemID);
	//	event.add(hostName, RemoteAddressHostName, profileSystem, systemID);
		IAddress<?> localIpAddress = addressService.addOrFindHostName(servletRequest.getLocalAddr(), profileSystem, systemID);
		ip.addOrReuse(LocalAddressIPAddress,localIpAddress,STRING_EMPTY,STRING_EMPTY, profileSystem, systemID);
	//	event.add(localIpAddress, LocalAddressIPAddress, profileSystem, systemID);
		IAddress<?> localHostName = addressService.addOrFindHostName(servletRequest.getLocalName(), profileSystem, systemID);
		ip.addOrReuse(LocalAddressHostName,localHostName,STRING_EMPTY,STRING_EMPTY, profileSystem, systemID);
	//	event.add(localHostName, LocalAddressHostName, profileSystem, systemID);
		IAddress<?> webAddress = addressService.addOrFindWebAddress(servletRequest.getRequestURL()
		                                                                          .toString(), profileSystem, systemID);
		ip.addOrReuse(WebAddress,webAddress,STRING_EMPTY,STRING_EMPTY, profileSystem, systemID);
	//	event.add(webAddress, WebAddress, profileSystem, systemID);

	//	event.addResourceItem(Added, BrowserInformation, "", sb.toString()
	//	                                                       .getBytes(), "application/json", profileSystem, systemID);
	}

	
	//public IEvent<?> getEvent()
/*	{
		return this.event;
	}*/

	public UserDTO<?> getDto()
	{
		return this.dto;
	}

	public IInvolvedParty<?> getIp()
	{
		return this.ip;
	}

	public ISystems getProfileSystem()
	{
		return this.profileSystem;
	}

	public HttpServletRequest getServletRequest()
	{
		return this.servletRequest;
	}

	public IEnterprise<?> getEnterprise()
	{
		return this.enterprise;
	}

	public ConfigureFromServletRequestEvent setEnterprise(IEnterprise<?> enterprise)
	{
		this.enterprise = enterprise;
		return this;
	}

	public ConfigureFromServletRequestEvent setServletRequest(HttpServletRequest servletRequest)
	{
		this.servletRequest = servletRequest;
		return this;
	}

	public ConfigureFromServletRequestEvent setProfileSystem(ISystems profileSystem)
	{
		this.profileSystem = profileSystem;
		return this;
	}

	public ConfigureFromServletRequestEvent setIp(IInvolvedParty<?> ip)
	{
		this.ip = ip;
		return this;
	}

	public ConfigureFromServletRequestEvent setDto(UserDTO<?> dto)
	{
		this.dto = dto;
		return this;
	}

/*	public ConfigureFromServletRequestEvent setEvent(IEvent<?> event)
	{
		this.event = event;
		return this;
	}*/

	@Override
	protected boolean canEqual(final Object other)
	{
		return other instanceof ConfigureFromServletRequestEvent;
	}

}
