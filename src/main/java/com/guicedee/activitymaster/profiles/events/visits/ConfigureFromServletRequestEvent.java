package com.guicedee.activitymaster.profiles.events.visits;


import com.guicedee.activitymaster.core.services.classifications.resourceitems.ResourceItemClassifications;
import com.guicedee.activitymaster.core.services.classifications.resourceitems.ResourceItemTypes;
import com.guicedee.activitymaster.core.services.dto.*;
import com.guicedee.activitymaster.core.services.system.IAddressService;
import com.guicedee.activitymaster.core.threads.TransactionalIdentifiedThread;
import com.guicedee.activitymaster.profiles.ProfileSystem;
import com.guicedee.activitymaster.profiles.dto.UserDTO;
import com.guicedee.guicedinjection.GuiceContext;
import com.guicedee.logger.LogFactory;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.UUID;
import java.util.logging.Level;

import static com.guicedee.activitymaster.core.services.classifications.resourceitems.ResourceItemClassifications.*;

public class ConfigureFromServletRequestEvent extends TransactionalIdentifiedThread
{
	private static final String JobServiceName = "ConfigureFromServletRequestEvent";

	private IEvent<?> event;
	private UserDTO<?> dto;
	private IInvolvedParty<?> ip;
	private ISystems profileSystem;
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

		IAddressService<?> addressService = GuiceContext.get(IAddressService.class);
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
		IAddress<?> ipAddress = addressService.addOrFindIPAddress(ipReal, profileSystem, systemID);
		ip.add(ipAddress, profileSystem, systemID);
		event.add(ipAddress, profileSystem, systemID);
		IAddress<?> hostName = addressService.addOrFindHostName(servletRequest.getRemoteHost(), profileSystem, systemID);
		ip.add(hostName, profileSystem, systemID);
		event.add(hostName, profileSystem, systemID);
		IAddress<?> localIpAddress = addressService.addOrFindHostName(servletRequest.getLocalAddr(), profileSystem, systemID);
		ip.add(localIpAddress, profileSystem, systemID);
		event.add(localIpAddress, profileSystem, systemID);
		IAddress<?> localHostName = addressService.addOrFindHostName(servletRequest.getLocalName(), profileSystem, systemID);
		ip.add(localHostName, profileSystem, systemID);
		event.add(localHostName, profileSystem, systemID);

		IAddress<?> webAddress = addressService.addOrFindWebAddress(servletRequest.getRequestURL()
		                                                                      .toString(), profileSystem, systemID);
		ip.add(webAddress, profileSystem, systemID);
		event.add(webAddress, profileSystem, systemID);

		IRelationshipValue<IInvolvedParty<?>,IResourceItem<?>,?> resourceItem = ip.add(AddedANewDevice, ResourceItemTypes.BrowserInformation,
		                                                                               "BrowserInformation",
		                                                                               sb.toString()
		                                                 .getBytes(),
		                                                                               "application/json", profileSystem, systemID);

		resourceItem.getSecondary().add(ResourceItemClassifications.Size, Long.toString(sb.toString()
		                                                                   .length()), profileSystem, systemID);
		event.add(Added,resourceItem.getSecondary(),"BrowserInformation",  profileSystem, systemID);
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

	public ConfigureFromServletRequestEvent setEvent(IEvent<?> event)
	{
		this.event = event;
		return this;
	}

	public ConfigureFromServletRequestEvent setDto(UserDTO<?> dto)
	{
		this.dto = dto;
		return this;
	}

	public ConfigureFromServletRequestEvent setIp(IInvolvedParty<?> ip)
	{
		this.ip = ip;
		return this;
	}

	public ConfigureFromServletRequestEvent setProfileSystem(ISystems profileSystem)
	{
		this.profileSystem = profileSystem;
		return this;
	}

	public ConfigureFromServletRequestEvent setServletRequest(HttpServletRequest servletRequest)
	{
		this.servletRequest = servletRequest;
		return this;
	}

	public ConfigureFromServletRequestEvent setEnterprise(IEnterprise<?> enterprise)
	{
		this.enterprise = enterprise;
		return this;
	}

	public boolean equals(final Object o)
	{
		if (o == this)
		{
			return true;
		}
		if (!(o instanceof ConfigureFromServletRequestEvent))
		{
			return false;
		}
		final ConfigureFromServletRequestEvent other = (ConfigureFromServletRequestEvent) o;
		if (!other.canEqual((Object) this))
		{
			return false;
		}
		final Object this$event = this.getEvent();
		final Object other$event = other.getEvent();
		if (this$event == null ? other$event != null : !this$event.equals(other$event))
		{
			return false;
		}
		final Object this$dto = this.getDto();
		final Object other$dto = other.getDto();
		if (this$dto == null ? other$dto != null : !this$dto.equals(other$dto))
		{
			return false;
		}
		final Object this$ip = this.getIp();
		final Object other$ip = other.getIp();
		if (this$ip == null ? other$ip != null : !this$ip.equals(other$ip))
		{
			return false;
		}
		final Object this$profileSystem = this.getProfileSystem();
		final Object other$profileSystem = other.getProfileSystem();
		if (this$profileSystem == null ? other$profileSystem != null : !this$profileSystem.equals(other$profileSystem))
		{
			return false;
		}
		final Object this$servletRequest = this.getServletRequest();
		final Object other$servletRequest = other.getServletRequest();
		if (this$servletRequest == null ? other$servletRequest != null : !this$servletRequest.equals(other$servletRequest))
		{
			return false;
		}
		final Object this$enterprise = this.getEnterprise();
		final Object other$enterprise = other.getEnterprise();
		if (this$enterprise == null ? other$enterprise != null : !this$enterprise.equals(other$enterprise))
		{
			return false;
		}
		return true;
	}

	protected boolean canEqual(final Object other)
	{
		return other instanceof ConfigureFromServletRequestEvent;
	}

	public int hashCode()
	{
		final int PRIME = 59;
		int result = 1;
		final Object $event = this.getEvent();
		result = result * PRIME + ($event == null ? 43 : $event.hashCode());
		final Object $dto = this.getDto();
		result = result * PRIME + ($dto == null ? 43 : $dto.hashCode());
		final Object $ip = this.getIp();
		result = result * PRIME + ($ip == null ? 43 : $ip.hashCode());
		final Object $profileSystem = this.getProfileSystem();
		result = result * PRIME + ($profileSystem == null ? 43 : $profileSystem.hashCode());
		final Object $servletRequest = this.getServletRequest();
		result = result * PRIME + ($servletRequest == null ? 43 : $servletRequest.hashCode());
		final Object $enterprise = this.getEnterprise();
		result = result * PRIME + ($enterprise == null ? 43 : $enterprise.hashCode());
		return result;
	}

	public String toString()
	{
		return "ConfigureFromServletRequestEvent(event=" + this.getEvent() + ", dto=" + this.getDto() + ", ip=" + this.getIp() + ", profileSystem=" + this.getProfileSystem() +
		       ", servletRequest=" + this.getServletRequest() + ", enterprise=" + this.getEnterprise() + ")";
	}
}
