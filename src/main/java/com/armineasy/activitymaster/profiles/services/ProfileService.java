package com.armineasy.activitymaster.profiles.services;

import com.armineasy.activitymaster.activitymaster.ActivityMasterConfiguration;
import com.armineasy.activitymaster.activitymaster.db.entities.address.Address;
import com.armineasy.activitymaster.activitymaster.db.entities.enterprise.Enterprise;
import com.armineasy.activitymaster.activitymaster.db.entities.events.Event;
import com.armineasy.activitymaster.activitymaster.db.entities.involvedparty.InvolvedParty;
import com.armineasy.activitymaster.activitymaster.db.entities.involvedparty.InvolvedPartyXInvolvedPartyIdentificationType;
import com.armineasy.activitymaster.activitymaster.db.entities.resourceitem.ResourceItem;
import com.armineasy.activitymaster.activitymaster.db.entities.security.SecurityToken;
import com.armineasy.activitymaster.activitymaster.db.entities.systems.Systems;
import com.armineasy.activitymaster.activitymaster.implementations.AddressService;
import com.armineasy.activitymaster.activitymaster.implementations.InvolvedPartyService;
import com.armineasy.activitymaster.activitymaster.implementations.SecurityTokenService;
import com.armineasy.activitymaster.activitymaster.services.IIdentificationType;
import com.armineasy.activitymaster.activitymaster.services.classifications.enterprise.IEnterpriseName;
import com.armineasy.activitymaster.activitymaster.services.classifications.resourceitems.ResourceItemClassifications;
import com.armineasy.activitymaster.activitymaster.services.classifications.resourceitems.ResourceItemTypes;
import com.armineasy.activitymaster.activitymaster.services.exceptions.ActivityMasterException;
import com.armineasy.activitymaster.activitymaster.services.system.IEnterpriseService;
import com.armineasy.activitymaster.activitymaster.services.system.IEventService;
import com.armineasy.activitymaster.profiles.ProfileSystem;
import com.armineasy.activitymaster.profiles.dto.GuestDTO;
import com.armineasy.activitymaster.profiles.dto.UserDTO;
import com.armineasy.activitymaster.profiles.dto.UserLoginDTO;
import com.armineasy.activitymaster.profiles.dto.UserProfileBasicDTO;
import com.armineasy.activitymaster.profiles.enumerations.ProfileEventTypes;
import com.armineasy.activitymaster.profiles.events.UpdateNewVisitEvent;
import com.armineasy.activitymaster.profiles.events.visits.ConfigureFromReadableUserAgentEvent;
import com.armineasy.activitymaster.profiles.events.visits.ConfigureFromServletRequestEvent;
import com.armineasy.activitymaster.profiles.events.visits.UpdateLastVisitEvent;
import com.armineasy.activitymaster.profiles.exceptions.ProfileServiceException;
import com.google.common.base.Strings;
import com.google.inject.Singleton;
import com.jwebmp.guicedinjection.GuiceContext;
import com.jwebmp.guicedinjection.interfaces.JobService;
import com.jwebmp.guicedinjection.pairing.Pair;
import com.jwebmp.guicedservlets.GuicedServletKeys;
import lombok.extern.java.Log;
import net.sf.uadetector.ReadableUserAgent;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

import static com.armineasy.activitymaster.activitymaster.services.classifications.resourceitems.ResourceItemClassifications.*;
import static com.armineasy.activitymaster.activitymaster.services.classifications.securitytokens.SecurityTokenClassifications.*;
import static com.armineasy.activitymaster.activitymaster.services.types.IdentificationTypes.*;
import static com.armineasy.activitymaster.profiles.enumerations.ProfileClassifications.*;
import static com.armineasy.activitymaster.profiles.enumerations.ProfileIdentificationTypes.*;
import static com.jwebmp.guicedinjection.GuiceContext.*;

@Singleton
@Log
public class ProfileService
{

	public GuestDTO<?> loginVisitor(GuestDTO<?> guestDTO, IEnterpriseName<?> enterpriseName, UUID... identityToken) throws ProfileServiceException
	{
		InvolvedPartyService involvedPartyService = GuiceContext.get(InvolvedPartyService.class);
		Enterprise enterprise = GuiceContext.get(IEnterpriseService.class)
		                                    .getEnterprise(enterpriseName);

		Systems profileSystem = ProfileSystem.getNewSystem()
		                                     .get(enterprise);
		UUID profileSystemUUID = ProfileSystem.getSystemTokens()
		                                      .get(enterprise);

		if ((identityToken == null || identityToken.length == 0) && guestDTO.getIdentityToken() == null)
		{
			identityToken = new UUID[]{profileSystemUUID};
		}

		Optional<UserDTO<?>> guestExists = findByKey(IdentificationTypeWebClientUUID, guestDTO.getWebClientUUID(), enterprise, identityToken);
		final UUID[] identityToken1Final = identityToken;
		Event event = get(IEventService.class).createEvent(ProfileEventTypes.SiteVisit, profileSystem, profileSystemUUID);
		InvolvedParty newIp;
		if (guestExists.isEmpty())
		{
			newIp = createNewVisitor(event, guestDTO, enterprise, profileSystem, identityToken);
		}
		else
		{
			newIp = involvedPartyService.findByIdentificationType(IdentificationTypeWebClientUUID, guestDTO.getWebClientUUID()
			                                                                                               .toString(), profileSystem, identityToken);
		}
		newIp = updateLatestVisit(event, guestDTO, enterprise, newIp, identityToken);
		try
		{
			HttpServletRequest request = GuiceContext.get(GuicedServletKeys.getHttpServletRequestKey());
			newIp = configureFromHTTPServletRequest(event, guestDTO, newIp, profileSystem, request, enterprise);
			configureFromReadableUserAgent(event, guestDTO, newIp, get(ReadableUserAgent.class), profileSystem, enterprise, identityToken1Final);
		}
		catch (Throwable T)
		{
			log.log(Level.FINER, "Unable to log servlet request information", T);
		}

		Optional<InvolvedPartyXInvolvedPartyIdentificationType> id = newIp.findIdentificationType(IdentificationTypeUUID, profileSystem, profileSystemUUID);
		id.ifPresent(involvedPartyXInvolvedPartyIdentificationType -> guestDTO.setIdentityToken(
				java.util.UUID.fromString(involvedPartyXInvolvedPartyIdentificationType.getValue()))
		            );
		return guestDTO;
	}

	InvolvedParty createNewVisitor(Event event, GuestDTO<?> guestDTO, Enterprise enterprise, Systems profileSystem, UUID... identityToken)
	{
		InvolvedPartyService involvedPartyService = GuiceContext.get(InvolvedPartyService.class);
		InvolvedParty newIp;
		//Create new guest record
		Pair<IIdentificationType, String> guestIDType = new Pair<>();
		guestIDType.setKey(IdentificationTypeWebClientUUID)
		           .setValue(guestDTO.getWebClientUUID()
		                             .toString());

		newIp = involvedPartyService.create(profileSystem, guestIDType, true, identityToken);


		SecurityToken visitorsGroup = GuiceContext.get(SecurityTokenService.class)
		                                          .getVisitorsGuestsFolder(enterprise, identityToken);

		SecurityToken myToken = get(SecurityTokenService.class).create(Identity,
		                                                               guestDTO.getWebClientUUID()
		                                                                       .toString(),
		                                                               "A new visitor device",
		                                                               profileSystem,
		                                                               visitorsGroup,
		                                                               identityToken);
		newIp.addIdentificationType(IdentificationTypeUUID, profileSystem, myToken.getSecurityToken(), identityToken);
		guestDTO.setIdentityToken(java.util.UUID.fromString(myToken.getSecurityToken()));

		UpdateNewVisitEvent visitEvent = GuiceContext.get(UpdateNewVisitEvent.class);
		visitEvent.setEnterprise(enterprise)
		          .setEvent(event)
		          .setGuestDTO(guestDTO)
		          .setIdentityToken(identityToken)
		          .setNewIp(newIp)
		          .setProfileSystem(profileSystem);

		if (ActivityMasterConfiguration.get()
		                               .isAsyncEnabled())
		{
			JobService.getInstance()
			          .addJob(UpdateNewVisitEvent.getJobServiceName(), visitEvent);
		}
		else
		{
			visitEvent.perform();
		}
		return newIp;
	}

	InvolvedParty configureFromReadableUserAgent(Event event, UserDTO<?> dto, InvolvedParty ip, ReadableUserAgent readableUserAgent, Systems profileSystem, Enterprise enterprise, UUID... identityToken)
	{
		ConfigureFromReadableUserAgentEvent ev = GuiceContext.get(ConfigureFromReadableUserAgentEvent.class);
		ev.setEnterprise(enterprise)
		  .setEvent(event)
		  .setDto(dto)
		  .setIdentityToken(identityToken)
		  .setIp(ip)
		  .setReadableUserAgent(readableUserAgent)
		  .setProfileSystem(profileSystem);

		if (ActivityMasterConfiguration.get()
		                               .isAsyncEnabled())
		{
			JobService.getInstance()
			          .addJob(ConfigureFromReadableUserAgentEvent.getJobServiceName(), ev);
		}
		else
		{
			ev.perform();
		}
		return ip;
	}

	InvolvedParty configureFromHTTPServletRequest(Event event, UserDTO<?> dto, InvolvedParty ip, Systems profileSystem, HttpServletRequest servletRequest, Enterprise enterprise)
	{
		ConfigureFromServletRequestEvent req = GuiceContext.get(ConfigureFromServletRequestEvent.class);
		req.setEvent(event)
		   .setDto(dto)
		   .setIp(ip)
		   .setProfileSystem(profileSystem)
		   .setServletRequest(servletRequest)
		   .setEnterprise(enterprise);
		if (ActivityMasterConfiguration.get()
		                               .isAsyncEnabled())
		{
			JobService.getInstance()
			          .addJob(ConfigureFromServletRequestEvent.getJobServiceName(), req);
		}
		else
		{
			req.perform();
		}
		return ip;
	}



	InvolvedParty updateLatestVisit(Event event, GuestDTO<?> guestDTO, Enterprise enterprise, InvolvedParty newIp,
	                                UUID... identityToken)
	{
		UpdateLastVisitEvent req = GuiceContext.get(UpdateLastVisitEvent.class);
		req.setEvent(event)
		   .setGuestDTO(guestDTO)
		   .setEnterprise(enterprise)
		   .setNewIp(newIp)
		   .setIdentityToken(identityToken);

		if (ActivityMasterConfiguration.get()
		                               .isAsyncEnabled())
		{
			JobService.getInstance()
			          .addJob(UpdateLastVisitEvent.getJobServiceName(), req);
		}
		else
		{
			req.perform();
		}
		return newIp;
	}


	public UserProfileBasicDTO<?> loginUser(UserLoginDTO<?> loginDTO, Enterprise enterprise, UUID... identityToken) throws ProfileServiceException
	{
		verifyUsernameExists(loginDTO, enterprise, identityToken);
		UserLoginDTO dto = verifyPasswordForUser(loginDTO, enterprise, identityToken);

		return null;
	}

	public Optional<UserDTO<?>> findByKey(IIdentificationType<?> identificationType, UUID webClientKey, Enterprise enterprise, UUID... identityToken) throws ProfileServiceException
	{
		Systems profileSystem = ProfileSystem.getNewSystem()
		                                     .get(enterprise);
		InvolvedPartyService service = GuiceContext.get(InvolvedPartyService.class);
		try
		{
			InvolvedParty ip = service.findByIdentificationType(identificationType, webClientKey.toString(), profileSystem, identityToken);
			return Optional.of(new UserDTO<>().fromIP(ip));
		}
		catch (ActivityMasterException e)
		{
			log.log(Level.FINER, "IP Not Found", e);
			return Optional.empty();
		}
	}


	private boolean verifyUsernameExists(UserLoginDTO<?> userLoginDTO, Enterprise enterprise, UUID... identityToken)
	{
		InvolvedPartyService ips = GuiceContext.get(InvolvedPartyService.class);
		if (Strings.isNullOrEmpty(userLoginDTO.getUserName()))
		{
			throw new ProfileServiceException("Username cannot be empty");
		}
		return ips.doesUsernameExist(userLoginDTO.getUserName(), enterprise);
	}

	private UserLoginDTO<?> verifyPasswordForUser(UserLoginDTO<?> userLoginDTO, Enterprise enterprise, UUID... identityToken)
	{
		InvolvedPartyService ips = GuiceContext.get(InvolvedPartyService.class);
		if (Objects.isNull(userLoginDTO.getIdentityToken()))
		{
			throw new ProfileServiceException("User Login DTO Already needs to have an associated UUID to login with a password");
		}
		if (Strings.isNullOrEmpty(userLoginDTO.getPassword()))
		{
			throw new ProfileServiceException("Passwords cannot be empty");
		}
		Systems profileSystem = ProfileSystem.getNewSystem()
		                                     .get(enterprise);
		InvolvedParty ip = ips.findByUsernameAndPassword(userLoginDTO.getUserName(), userLoginDTO.getPassword(), profileSystem, true, identityToken);
		userLoginDTO = new UserLoginDTO<>().fromIP(ip);
		return userLoginDTO;
	}


	/**
	 * Returns an <code>InetAddress</code> object encapsulating what is most likely the machine's LAN IP address.
	 * <p/>
	 * This method is intended for use as a replacement of JDK method <code>InetAddress.getLocalHost</code>, because
	 * that method is ambiguous on Linux systems. Linux systems enumerate the loopback network interface the same
	 * way as regular LAN network interfaces, but the JDK <code>InetAddress.getLocalHost</code> method does not
	 * specify the algorithm used to select the address returned under such circumstances, and will often return the
	 * loopback address, which is not valid for network communication. Details
	 * <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4665037">here</a>.
	 * <p/>
	 * This method will scan all IP addresses on all network interfaces on the host machine to determine the IP address
	 * most likely to be the machine's LAN address. If the machine has multiple IP addresses, this method will prefer
	 * a site-local IP address (e.g. 192.168.x.x or 10.10.x.x, usually IPv4) if the machine has one (and will return the
	 * first site-local address if the machine has more than one), but if the machine does not hold a site-local
	 * address, this method will return simply the first non-loopback address found (IPv4 or IPv6).
	 * <p/>
	 * If this method cannot find a non-loopback address using this selection algorithm, it will fall back to
	 * calling and returning the result of JDK method <code>InetAddress.getLocalHost</code>.
	 * <p/>
	 *
	 * @throws UnknownHostException
	 * 		If the LAN address of the machine cannot be found.
	 */
	private static InetAddress getLocalHostLANAddress() throws UnknownHostException
	{
		try
		{
			InetAddress candidateAddress = null;
			// Iterate all NICs (network interface cards)...
			for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); )
			{
				NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
				// Iterate all IP addresses assigned to each card...
				for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); )
				{
					InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
					if (!inetAddr.isLoopbackAddress())
					{

						if (inetAddr.isSiteLocalAddress())
						{
							// Found non-loopback site-local address. Return it immediately...
							return inetAddr;
						}
						else if (candidateAddress == null)
						{
							// Found non-loopback address, but not necessarily site-local.
							// Store it as a candidate to be returned if site-local address is not subsequently found...
							candidateAddress = inetAddr;
							// Note that we don't repeatedly assign non-loopback non-site-local addresses as candidates,
							// only the first. For subsequent iterations, candidate will be non-null.
						}
					}
				}
			}
			if (candidateAddress != null)
			{
				// We did not find a site-local address, but we found some other non-loopback address.
				// Server might have a non-site-local address assigned to its NIC (or it might be running
				// IPv6 which deprecates the "site-local" concept).
				// Return this non-loopback candidate address...
				return candidateAddress;
			}
			// At this point, we did not find a non-loopback address.
			// Fall back to returning whatever InetAddress.getLocalHost() returns...
			InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
			if (jdkSuppliedAddress == null)
			{
				throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
			}
			return jdkSuppliedAddress;
		}
		catch (Exception e)
		{
			UnknownHostException unknownHostException = new UnknownHostException("Failed to determine LAN address: " + e);
			unknownHostException.initCause(e);
			throw unknownHostException;
		}
	}
}
