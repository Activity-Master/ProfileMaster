package com.armineasy.activitymaster.profiles.services;

import com.armineasy.activitymaster.activitymaster.db.ActivityMasterDB;
import com.armineasy.activitymaster.activitymaster.db.entities.address.Address;
import com.armineasy.activitymaster.activitymaster.db.entities.enterprise.Enterprise;
import com.armineasy.activitymaster.activitymaster.db.entities.involvedparty.InvolvedParty;
import com.armineasy.activitymaster.activitymaster.db.entities.resourceitem.ResourceItem;
import com.armineasy.activitymaster.activitymaster.db.entities.security.SecurityToken;
import com.armineasy.activitymaster.activitymaster.db.entities.systems.Systems;
import com.armineasy.activitymaster.activitymaster.implementations.*;
import com.armineasy.activitymaster.activitymaster.services.IIdentificationType;
import com.armineasy.activitymaster.activitymaster.services.classifications.resourceitems.ResourceItemClassifications;
import com.armineasy.activitymaster.activitymaster.services.classifications.resourceitems.ResourceItemTypes;
import com.armineasy.activitymaster.activitymaster.services.exceptions.ActivityMasterException;
import com.armineasy.activitymaster.activitymaster.services.system.IInvolvedPartyService;
import com.armineasy.activitymaster.profiles.ProfileSystem;
import com.armineasy.activitymaster.profiles.dto.GuestDTO;
import com.armineasy.activitymaster.profiles.dto.UserDTO;
import com.armineasy.activitymaster.profiles.dto.UserLoginDTO;
import com.armineasy.activitymaster.profiles.dto.UserProfileBasicDTO;
import com.armineasy.activitymaster.profiles.exceptions.ProfileServiceException;
import com.google.common.base.Strings;
import com.google.inject.Singleton;
import com.jwebmp.guicedinjection.GuiceContext;
import com.jwebmp.guicedinjection.pairing.Pair;
import com.jwebmp.guicedpersistence.db.annotations.Transactional;
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

import static com.armineasy.activitymaster.activitymaster.services.classifications.events.EventInvolvedPartiesClassifications.*;
import static com.armineasy.activitymaster.activitymaster.services.classifications.resourceitems.ResourceItemTypes.*;
import static com.armineasy.activitymaster.activitymaster.services.classifications.securitytokens.SecurityTokenClassifications.*;
import static com.armineasy.activitymaster.activitymaster.services.types.IdentificationTypes.*;
import static com.armineasy.activitymaster.activitymaster.services.types.NameTypes.*;
import static com.armineasy.activitymaster.profiles.enumerations.ProfileClassifications.*;
import static com.armineasy.activitymaster.profiles.enumerations.ProfileIdentificationTypes.*;
import static com.jwebmp.guicedinjection.GuiceContext.*;

@Singleton
@Log
public class ProfileService
{

	public GuestDTO<?> loginVisitor(GuestDTO<?> guestDTO, Enterprise enterprise, UUID... identityToken) throws ProfileServiceException
	{
		InvolvedPartyService involvedPartyService = GuiceContext.get(InvolvedPartyService.class);

		Optional<UserDTO<?>> guestExists = findByKey(IdentificationTypeWebClientUUID, guestDTO.getWebClientUUID(), enterprise, identityToken);

		Systems profileSystem = ProfileSystem.getNewSystem()
		                                     .get(enterprise);
		UUID profileSystemUUID = ProfileSystem.getSystemTokens()
		                                      .get(enterprise);

		InvolvedParty newIp = null;
		if (guestExists.isEmpty())
		{
			newIp = createNewVisitor(guestDTO, enterprise, identityToken);
			//Create new guest record
		}
		else
		{
			newIp = involvedPartyService.findByIdentificationType(IdentificationTypeWebClientUUID, guestDTO.getWebClientUUID()
			                                                                                               .toString(), profileSystem, identityToken);
		}
		newIp = updateLatestVisit(guestDTO, enterprise, newIp, identityToken);

		return guestDTO;
	}

	@Transactional(entityManagerAnnotation = ActivityMasterDB.class)
	InvolvedParty createNewVisitor(GuestDTO<?> guestDTO, Enterprise enterprise, UUID... identityToken)
	{
		InvolvedPartyService involvedPartyService = GuiceContext.get(InvolvedPartyService.class);
		UUID profileSystemUUID = ProfileSystem.getSystemTokens()
		                                      .get(enterprise);
		Systems profileSystem = ProfileSystem.getNewSystem()
		                                     .get(enterprise);

		InvolvedParty newIp;
		//Create new guest record
		Pair<IIdentificationType, String> guestIDType = new Pair<>();
		guestIDType.setKey(IdentificationTypeWebClientUUID)
		           .setValue(guestDTO.getWebClientUUID()
		                             .toString());

		newIp = involvedPartyService.create(profileSystem, guestIDType, true, profileSystemUUID);
		SecurityToken visitorsGroup = GuiceContext.get(SecurityTokenService.class)
		                                          .getVisitorsGuestsFolder(enterprise, profileSystemUUID);

		SecurityToken myToken = get(SecurityTokenService.class).create(Identity,
		                                                               guestDTO.getWebClientUUID()
		                                                                       .toString(),
		                                                               "A new visitor device",
		                                                               profileSystem,
		                                                               visitorsGroup,
		                                                               profileSystemUUID);
		guestDTO.setIdentityToken(UUID.fromString(myToken.getSecurityToken()));

		newIp.addIdentificationType(IdentificationTypeUUID, profileSystem, myToken.getSecurityToken(), identityToken);
		newIp.addNameType(PreferredNameType, profileSystem, "Guest", profileSystemUUID);
		newIp.addClassification(CreatedBy, Long.toString(newIp.getId()), profileSystem, profileSystemUUID);
		return newIp;
	}
	@Transactional(entityManagerAnnotation = ActivityMasterDB.class)
	public InvolvedParty configureFromReadableUserAgent(UserDTO<?> dto, ReadableUserAgent readableUserAgent, Enterprise enterprise, UUID... identityToken)
	{
		UUID systemID = ProfileSystem.getSystemTokens()
		                             .get(enterprise);
		Systems profileSystem = GuiceContext.get(SystemsService.class)
		                                    .findSystem(enterprise, systemID, systemID);

		IInvolvedPartyService involvedPartyService = GuiceContext.get(IInvolvedPartyService.class);
		InvolvedParty ip = involvedPartyService.findByIdentificationType(IdentificationTypeUUID, dto.getIdentityToken()
		                                                                                            .toString(), profileSystem, identityToken);

		ResourceItemService resourceItemService = GuiceContext.get(ResourceItemService.class);
		ResourceItem resourceItem = ip.addResourceItem(BrowserDeviceCategory,ResourceItemClassifications.AddedANewDevice,
		                                               readableUserAgent.getDeviceCategory()
		                                                                .getName()
		                                                                .getBytes(),
		                                               "application/text", profileSystem, identityToken);

		resourceItem.addClassification(ResourceItemClassifications.AddedANewDevice, BrowserDeviceCategory.classificationName(), profileSystem, systemID);
		resourceItem.addClassification(ResourceItemClassifications.Size, Long.toString(readableUserAgent.getDeviceCategory()
		                                                                                                .getName()
		                                                                                                .length()), profileSystem, systemID);
		ResourceItem resourceItemName = ip.addResourceItem(BrowserDeviceName,ResourceItemClassifications.AddedANewDevice,
		                                                   readableUserAgent.getDeviceCategory()
		                                                                    .getCategory()
		                                                                    .getName()
		                                                                    .getBytes(),
		                                                   "application/text", profileSystem, identityToken);
		resourceItemName.addClassification(ResourceItemClassifications.AddedANewDevice, BrowserDeviceName.classificationName(), profileSystem, systemID);
		resourceItemName.addClassification(ResourceItemClassifications.Size, Long.toString(readableUserAgent.getDeviceCategory()
		                                                                                                    .getName()
		                                                                                                    .length()), profileSystem, systemID);
		ResourceItem resourceItemIcon = ip.addResourceItem(BrowserDeviceIcon,ResourceItemClassifications.AddedANewDevice,
		                                                   readableUserAgent.getDeviceCategory()
		                                                                    .getIcon()
		                                                                    .getBytes(),
		                                                   "application/text", profileSystem, identityToken);
		resourceItemIcon.addClassification(ResourceItemClassifications.AddedANewDevice, BrowserDeviceIcon.classificationName(), profileSystem, systemID);
		resourceItemIcon.addClassification(ResourceItemClassifications.Size, Long.toString(readableUserAgent.getDeviceCategory()
		                                                                                                    .getIcon()
		                                                                                                    .length()), profileSystem, systemID);

		ResourceItem resourceItemOperatingSystem = ip.addResourceItem(OperatingSystem,ResourceItemClassifications.AddedANewDevice,
		                                                              readableUserAgent.getOperatingSystem()
		                                                                               .getName()
		                                                                               .getBytes(),
		                                                              "application/text", profileSystem, identityToken);
		resourceItemOperatingSystem.addClassification(ResourceItemClassifications.AddedANewDevice, OperatingSystem.classificationName(), profileSystem, systemID);
		resourceItemOperatingSystem.addClassification(ResourceItemClassifications.Size, Long.toString(readableUserAgent.getOperatingSystem()
		                                                                                                               .getName()
		                                                                                                               .length()), profileSystem, systemID);

		ResourceItem resourceItemFamily = ip.addResourceItem(OperatingSystemFamily,ResourceItemClassifications.AddedANewDevice,
		                                                     readableUserAgent.getOperatingSystem()
		                                                                      .getFamily()
		                                                                      .getName()
		                                                                      .getBytes(),
		                                                     "application/text", profileSystem, identityToken);
		resourceItemFamily.addClassification(ResourceItemClassifications.AddedANewDevice, OperatingSystemFamily.classificationName(), profileSystem, systemID);
		resourceItemFamily.addClassification(ResourceItemClassifications.Size, Long.toString(readableUserAgent.getOperatingSystem()
		                                                                                                      .getFamily()
		                                                                                                      .getName()
		                                                                                                      .length()), profileSystem, systemID);


		return ip;
	}

	@Transactional(entityManagerAnnotation = ActivityMasterDB.class)
	public InvolvedParty configureFromHTTPServletRequest(UserDTO<?> dto, HttpServletRequest servletRequest, Enterprise enterprise)
	{
		UUID systemID = ProfileSystem.getSystemTokens()
		                             .get(enterprise);

		Systems profileSystem = ProfileSystem.getNewSystem().get(enterprise);

		IInvolvedPartyService involvedPartyService = GuiceContext.get(IInvolvedPartyService.class);
		InvolvedParty ip = involvedPartyService.findByIdentificationType(IdentificationTypeUUID, dto.getIdentityToken()
		                                                                                            .toString(), profileSystem, systemID);
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
		Address ipAddress = addressService.addOrFindIPAddress(servletRequest.getRemoteAddr(), profileSystem, systemID);
		ip.add(ipAddress, profileSystem, systemID);
		Address hostName = addressService.addOrFindHostName(servletRequest.getRemoteHost(), profileSystem, systemID);
		ip.add(hostName, profileSystem, systemID);
		Address localIpAddress = addressService.addOrFindHostName(servletRequest.getLocalAddr(), profileSystem, systemID);
		ip.add(localIpAddress, profileSystem, systemID);
		Address localHostName = addressService.addOrFindHostName(servletRequest.getLocalName(), profileSystem, systemID);
		ip.add(localHostName, profileSystem, systemID);

		ResourceItemService resourceItemService = GuiceContext.get(ResourceItemService.class);
		ResourceItem resourceItem = ip.addResourceItem(ResourceItemTypes.BrowserInformation,ResourceItemClassifications.AddedANewDevice,
		                                               sb.toString()
		                                                 .getBytes(),
		                                               "application/json", profileSystem, systemID);
		resourceItem.addClassification(ResourceItemClassifications.Size, Long.toString(sb.toString()
		                                                                                 .length()), profileSystem, systemID);


		return ip;
	}

	public UserDTO<?> setReadableUserAgent(UserDTO<?> dto, Enterprise enterprise, HttpServletRequest servletRequest, UUID... identityToken)
	{
		return dto;
	}

	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");

	@Transactional(entityManagerAnnotation = ActivityMasterDB.class)
	InvolvedParty updateLatestVisit(GuestDTO<?> guestDTO, Enterprise enterprise, InvolvedParty newIp,
	                                UUID... identityToken)
	{
		Systems profileSystem = ProfileSystem.getNewSystem()
		                                     .get(enterprise);
		//Add last login time
		String lastVisit = formatter.format(LocalDateTime.now());

		newIp.addOrUpdateClassification(LastVisitTime,
		                                lastVisit,
		                                profileSystem,
		                                identityToken);

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
