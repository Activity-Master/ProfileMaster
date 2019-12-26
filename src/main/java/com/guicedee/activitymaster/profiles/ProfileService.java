package com.guicedee.activitymaster.profiles;

import com.guicedee.activitymaster.core.ActivityMasterConfiguration;
import com.guicedee.activitymaster.core.services.classifications.enterprise.IEnterpriseName;
import com.guicedee.activitymaster.core.services.dto.*;
import com.guicedee.activitymaster.core.services.enumtypes.IIdentificationType;
import com.guicedee.activitymaster.core.services.exceptions.SecurityAccessException;
import com.guicedee.activitymaster.core.services.security.Passwords;
import com.guicedee.activitymaster.core.services.system.IEnterpriseService;
import com.guicedee.activitymaster.core.services.system.IEventService;
import com.guicedee.activitymaster.core.services.system.IInvolvedPartyService;
import com.guicedee.activitymaster.core.services.system.ISecurityTokenService;
import com.guicedee.activitymaster.profiles.dto.*;
import com.guicedee.activitymaster.profiles.enumerations.ProfileEventTypes;
import com.guicedee.activitymaster.profiles.events.UpdateNewVisitEvent;
import com.guicedee.activitymaster.profiles.events.visits.ConfigureFromReadableUserAgentEvent;
import com.guicedee.activitymaster.profiles.events.visits.ConfigureFromServletRequestEvent;
import com.guicedee.activitymaster.profiles.events.visits.UpdateLastVisitEvent;
import com.guicedee.activitymaster.profiles.exceptions.ProfileServiceException;
import com.guicedee.activitymaster.profiles.exceptions.UserExistsException;
import com.guicedee.activitymaster.profiles.exceptions.WaitingForConfirmationKeyException;
import com.guicedee.activitymaster.profiles.services.interfaces.IProfileService;
import com.guicedee.activitymaster.profiles.services.interfaces.IRolesService;
import com.google.inject.Singleton;
import com.guicedee.activitymaster.profiles.services.interfaces.IUserRole;
import com.guicedee.activitymaster.profiles.webdto.UserConfirmationKeyDTO;
import com.guicedee.activitymaster.profiles.webdto.UserLoginDTO;
import com.guicedee.activitymaster.profiles.webdto.UserRegistrationDTO;
import com.guicedee.activitymaster.sessions.services.ISession;
import com.guicedee.guicedinjection.GuiceContext;
import com.guicedee.guicedinjection.interfaces.JobService;
import com.guicedee.guicedinjection.pairing.Pair;
import com.guicedee.guicedservlets.GuicedServletKeys;
import net.sf.uadetector.ReadableUserAgent;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.guicedee.activitymaster.core.services.classifications.involvedparty.InvolvedPartyClassifications.*;
import static com.guicedee.activitymaster.core.services.classifications.securitytokens.SecurityTokenClassifications.*;
import static com.guicedee.activitymaster.core.services.types.IdentificationTypes.*;
import static com.guicedee.activitymaster.profiles.enumerations.ProfileClassifications.*;
import static com.guicedee.activitymaster.profiles.enumerations.ProfileEventTypes.*;
import static com.guicedee.activitymaster.profiles.enumerations.ProfileIdentificationTypes.*;
import static com.guicedee.activitymaster.profiles.services.enumerations.UserRoles.*;
import static com.guicedee.guicedinjection.GuiceContext.*;
import static java.time.temporal.ChronoUnit.*;

@SuppressWarnings("Duplicates")
@Singleton
public class ProfileService
		implements IProfileService
{
	private static final Logger log = Logger.getLogger(ProfileService.class.getName());

	@Override
	public ProfileServiceDTO<?> loginUser(UserLoginDTO<?> profileServiceDTO, IEnterpriseName<?> enterpriseName, UUID... identityToken) throws ProfileServiceException
	{
		IInvolvedPartyService<?> involvedPartyService = get(IInvolvedPartyService.class);
		IEnterprise<?> enterprise = get(IEnterpriseService.class)
				                            .getEnterprise(enterpriseName);

		ISystems<?> profileSystem = ProfileSystem.getSystemsMap()
		                                         .get(enterprise);
		UUID profileSystemUUID = ProfileSystem.getSystemTokens()
		                                      .get(enterprise);

		if ((identityToken == null || identityToken.length == 0) && profileServiceDTO.getIdentityToken() == null)
		{
			identityToken = new UUID[]{profileSystemUUID};
		}

		IInvolvedParty<?> newIp = involvedPartyService.findByIdentificationType(IdentificationTypeWebClientUUID, profileServiceDTO.getWebClientUUID()
		                                                                                                                          .toString(), profileSystem, profileSystemUUID);

		try
		{
			IInvolvedParty<?> foundParty = involvedPartyService.findByUsernameAndPassword(profileServiceDTO.getUserName(),
			                                                                              profileServiceDTO.getPassword(),
			                                                                              profileSystem,
			                                                                              true,
			                                                                              profileSystemUUID);
			profileServiceDTO.setIdentityToken(foundParty.getSecurityIdentity());
			newIp = foundParty;

		/*	if (!newIp.equals(foundParty))
			{
				foundParty.addOrUpdate(IdentificationTypeWebClientUUID,
				                       profileServiceDTO.getWebClientUUID()
				                                        .toString(),
				                       profileSystem, profileSystemUUID);
				newIp.archive(IdentificationTypeWebClientUUID, profileSystem, profileSystemUUID);
				//newIp.archive();
				newIp = foundParty;
			}*/
			ISession<?> iSess = get(ISession.class);
			UserSecurity us = null;
			if (iSess.hasValue("user-security"))
			{
				us = iSess.as("user-security", UserSecurity.class);
			}
			else
			{
				iSess.addValue("user-security", new UserSecurity());
				us = iSess.as("user-security", UserSecurity.class);
			}
			us.setLoggedIn(true)
			  .setLastIpAddress(get(HttpServletRequest.class)
					                    .getRemoteAddr())
			  .setLoginExpiresOn(profileServiceDTO.isRememberMe()
			                     ? LocalDateTime.MAX
			                     : LocalDateTime.now()
			                                    .plusMinutes(20))
			  .setRememberMe(profileServiceDTO.isRememberMe());
			iSess.addValue("user-security", us);
			iSess.setInvolvedParty(newIp);
			if (newIp.has(IdentificationTypeEnterpriseCreatorRole, profileSystem, profileSystemUUID))
			{
				get(IRolesService.class)
						.addRole(newIp, Administrator, profileServiceDTO, profileSystem, identityToken);
				iSess.removeValue("user-roles");
				iSess.addValue("user-roles", get(IRolesService.class)
						                             .getRoles(newIp, profileSystem, identityToken));
			}
		/*	if (!newIp.equals(original))
			{
				original.move(newIp);
			}*/
		}
		catch (SecurityAccessException e)
		{
			throw new ProfileServiceException("Invalid username or password");
		}
		profileServiceDTO.setPassword(null);
		return profileServiceDTO;
	}

	@Override
	public ProfileServiceDTO<?> logoutUser(ProfileServiceDTO<?> profileServiceDTO, IEnterpriseName<?> enterpriseName, UUID... identityToken) throws ProfileServiceException
	{
		//	IInvolvedPartyService<?> involvedPartyService = get(IInvolvedPartyService.class);
		IEnterprise<?> enterprise = get(IEnterpriseService.class)
				                            .getEnterprise(enterpriseName);

	/*	ISystems<?> profileSystem = ProfileSystem.getSystemsMap()
		                                         .get(enterprise);*/
		UUID profileSystemUUID = ProfileSystem.getSystemTokens()
		                                      .get(enterprise);

		if ((identityToken == null || identityToken.length == 0) && profileServiceDTO.getIdentityToken() == null)
		{
			identityToken = new UUID[]{profileSystemUUID};
		}

		//		IInvolvedParty<?> newIp = involvedPartyService.findByIdentificationType(IdentificationTypeWebClientUUID, profileServiceDTO.getWebClientUUID()
		//		                                                                                                                          .toString(), profileSystem, profileSystemUUID);
		ISession<?> iSess = get(ISession.class);
		UserSecurity us = iSess.as("user-security", UserSecurity.class);
		us.setRememberMe(false);
		us.setLoggedIn(false);
		us.setLoginExpiresOn(LocalDateTime.now());
		iSess.addValue("user-security", us);
		iSess.removeValue("user-roles");
		return profileServiceDTO;
	}

	@Override
	public ProfileServiceDTO<?> loginVisitor(ProfileServiceDTO<?> profileServiceDTO, IEnterpriseName<?> enterpriseName, UUID... identityToken) throws ProfileServiceException
	{
		IInvolvedPartyService<?> involvedPartyService = get(IInvolvedPartyService.class);
		IEnterprise<?> enterprise = get(IEnterpriseService.class)
				                            .getEnterprise(enterpriseName);
		profileServiceDTO.setEnterprise(enterpriseName);

		ISystems<?> profileSystem = ProfileSystem.getSystemsMap()
		                                         .get(enterprise);

		UUID profileSystemUUID = ProfileSystem.getSystemTokens()
		                                      .get(enterprise);

		if ((identityToken == null || identityToken.length == 0) && profileServiceDTO.getIdentityToken() == null)
		{
			identityToken = new UUID[]{profileSystemUUID};
		}

		IInvolvedParty<?> guestExists = involvedPartyService.findByIdentificationType(IdentificationTypeWebClientUUID, profileServiceDTO.getWebClientUUID()
		                                                                                                                                .toString(), profileSystem, identityToken);
		final UUID[] identityToken1Final = identityToken;
		IEvent<?> event = get(IEventService.class).createEvent(ProfileEventTypes.SiteVisit, profileSystem, profileSystemUUID);
		IInvolvedParty<?> newIp;
		if (guestExists == null)
		{
			newIp = createNewVisitor(event, profileServiceDTO, enterprise, profileSystem, profileSystemUUID);
		}
		else
		{
			newIp = involvedPartyService.findByIdentificationType(IdentificationTypeWebClientUUID, profileServiceDTO.getWebClientUUID()
			                                                                                                        .toString(), profileSystem, profileSystemUUID);
		}
		newIp = updateLatestVisit(event, profileServiceDTO, enterprise, newIp, identityToken);

		try
		{
			HttpServletRequest request = get(GuicedServletKeys.getHttpServletRequestKey());
			newIp = configureFromHTTPServletRequest(event, profileServiceDTO, newIp, profileSystem, request, enterprise);
			configureFromReadableUserAgent(event, profileServiceDTO, newIp, get(ReadableUserAgent.class), profileSystem, enterprise, identityToken1Final);
		}
		catch (Throwable T)
		{
			log.log(Level.FINER, "Unable to log servlet request information", T);
		}

		Optional<IRelationshipValue<IInvolvedParty<?>, IInvolvedPartyIdentificationType<?>, ?>> id = newIp.find(IdentificationTypeUUID, profileSystem, profileSystemUUID);
		id.ifPresent(involvedPartyXInvolvedPartyIdentificationType -> profileServiceDTO.setIdentityToken(
				involvedPartyXInvolvedPartyIdentificationType.getValueAsUUID())
		            );

		profileServiceDTO.setInvolvedParty(newIp);

		ISession<?> session = get(ISession.class);
		session.setInvolvedParty(newIp);
		List<IUserRole<?>> roles = new ArrayList<>();
		session.setInvolvedParty(newIp);
		UserSecurity us;
		if (session.hasValue("user-security"))
		{
			us = session.as("user-security", UserSecurity.class);
		}
		else
		{
			us = new UserSecurity();
		}

		if (us.isLoggedIn())
		{
			IRolesService rolesService = get(IRolesService.class);
			roles.addAll(rolesService.getRoles(session.getInvolvedParty(), profileSystem, profileSystemUUID));
			if (!roles.contains(Administrator) && newIp.has(IdentificationTypeEnterpriseCreatorRole, profileSystem, profileSystemUUID))
			{
				session.getInvolvedParty()
				       .remove(UserRoles, profileSystem, identityToken);
				roles.clear();
				roles.addAll(rolesService.addRole(session.getInvolvedParty(), Administrator, profileServiceDTO, profileSystem, identityToken));
			}
		}
		else
		{
			roles.addAll(List.of(Visitor));
		}
		session.setEnterpriseName(enterpriseName);
		session.addValue("user-roles", roles);
		session.addValue("user-security", us);
		return profileServiceDTO;
	}

	IInvolvedParty<?> createNewVisitor(IEvent<?> event, ProfileServiceDTO<?> profileServiceDTO, IEnterprise<?> enterprise, ISystems<?> profileSystem, UUID... identityToken)
	{
		IInvolvedPartyService<?> involvedPartyService = get(IInvolvedPartyService.class);
		IInvolvedParty<?> newIp;
		//Create new guest record
		Pair<IIdentificationType<?>, String> guestIDType = new Pair<>();
		guestIDType.setKey(IdentificationTypeWebClientUUID)
		           .setValue(profileServiceDTO.getWebClientUUID()
		                                      .toString());

		newIp = involvedPartyService.create(profileSystem, guestIDType, true, identityToken);

		ISecurityToken<?> visitorsGroup = get(ISecurityTokenService.class)
				                                  .getVisitorsGuestsFolder(enterprise, identityToken);

		ISecurityToken<?> myToken = get(ISecurityTokenService.class).create(Identity,
		                                                                    profileServiceDTO.getWebClientUUID()
		                                                                                     .toString(),
		                                                                    "A new visitor device",
		                                                                    profileSystem,
		                                                                    visitorsGroup,
		                                                                    identityToken);
		newIp.addOrUpdate(IdentificationTypeUUID, myToken.getSecurityToken(), profileSystem, identityToken);

		profileServiceDTO.setIdentityToken(java.util.UUID.fromString(myToken.getSecurityToken()));
		UpdateNewVisitEvent visitEvent = get(UpdateNewVisitEvent.class);
		visitEvent.setEnterprise(enterprise)
		          .setEvent(event)
		          .setProfileServiceDTO(profileServiceDTO)
		          .setIdentityToken(identityToken)
		          .setNewIp(newIp)
		          .setProfileSystem(profileSystem);

		UUID profileSystemUUID = ProfileSystem.getSystemTokens()
		                                      .get(enterprise);

		get(IRolesService.class).addRole(newIp, Visitor, profileServiceDTO, profileSystem, profileSystemUUID);

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

	IInvolvedParty<?> configureFromReadableUserAgent(IEvent<?> event, UserDTO<?> dto, IInvolvedParty<?> ip, ReadableUserAgent readableUserAgent, ISystems<?> profileSystem, IEnterprise<?> enterprise, UUID... identityToken)
	{
		ConfigureFromReadableUserAgentEvent ev = get(ConfigureFromReadableUserAgentEvent.class);
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

	IInvolvedParty<?> configureFromHTTPServletRequest(IEvent<?> event, UserDTO<?> dto, IInvolvedParty<?> ip, ISystems<?> profileSystem, HttpServletRequest servletRequest, IEnterprise<?> enterprise)
	{
		ConfigureFromServletRequestEvent req = get(ConfigureFromServletRequestEvent.class);
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

	@Override
	public UserConfirmationKeyDTO<?> registerVisitor(UserRegistrationDTO<?> userRegistrationDTO, IEnterpriseName<?> enterpriseName, UUID... identityToken) throws UserExistsException, WaitingForConfirmationKeyException
	{
		IInvolvedPartyService<?> involvedPartyService = get(IInvolvedPartyService.class);
		IEnterprise<?> enterprise = get(IEnterpriseService.class)
				                            .getEnterprise(enterpriseName);

		ISystems profileSystem = ProfileSystem.getSystemsMap()
		                                      .get(enterprise);
		UUID profileSystemUUID = ProfileSystem.getSystemTokens()
		                                      .get(enterprise);

		IEvent<?> registerEvent = get(IEventService.class)
				                          .createEvent(UserRegistered, profileSystem, profileSystemUUID);

		IInvolvedParty<?> ipExists = involvedPartyService.findByIdentificationType(IdentificationTypeEmailAddress,
		                                                                           new Passwords().integerEncrypt(userRegistrationDTO.getUserName()
		                                                                                                                             .getBytes())
				, profileSystem, profileSystemUUID);
		if (ipExists != null)
		{
			if (ipExists.has(ConfirmationKey, profileSystem, identityToken))
			{
				throw new WaitingForConfirmationKeyException("The email address is waiting for a confirmation key");
			}
			throw new UserExistsException("That email address is already in use as a valid identifier");
		}
		//ActivityMasterConfiguration.get().setSecurityEnabled(false);
		IInvolvedParty<?> newIp;
		newIp = involvedPartyService.findByIdentificationType(IdentificationTypeWebClientUUID, userRegistrationDTO.getWebClientUUID()
		                                                                                                          .toString(), profileSystem, profileSystemUUID);
		//ActivityMasterConfiguration.get().setSecurityEnabled(true);
		newIp.addOrUpdate(IdentificationTypeEmailAddress,
		                  new Passwords().integerEncrypt(userRegistrationDTO.getUserName()
		                                                                    .getBytes()),
		                  profileSystem,
		                  profileSystemUUID);

		newIp.expire(IdentificationTypeEmailAddress, Duration.of(2, HOURS), profileSystem, profileSystemUUID);

		involvedPartyService.addUpdateUsernamePassword(registerEvent, userRegistrationDTO.getUserName(), userRegistrationDTO.getPassword(), newIp, profileSystem,
		                                               profileSystemUUID);

		userRegistrationDTO.setPassword(null);
		newIp.expire(IdentificationTypeUserName, Duration.of(2, HOURS), profileSystem, profileSystemUUID);
		newIp.expire(SecurityPassword, Duration.of(2, HOURS), profileSystem, profileSystemUUID);
		newIp.expire(SecurityPasswordSalt, Duration.of(2, HOURS), profileSystem, profileSystemUUID);

		UserConfirmationKeyDTO confirmationKeyDTO = (UserConfirmationKeyDTO) new UserConfirmationKeyDTO()
				                                                                     .setWebClientUUID(userRegistrationDTO.getWebClientUUID())
				                                                                     .setIdentityToken(userRegistrationDTO.getIdentityToken());
		confirmationKeyDTO.setConfirmationKey(UUID.randomUUID());
		IRelationshipValue<IInvolvedParty<?>, IClassification<?>, ?> x = newIp.addOrUpdate(ConfirmationKey, confirmationKeyDTO.getConfirmationKey()
		                                                                                                                      .toString(), profileSystem, profileSystemUUID);

		x.expire(Duration.of(2, HOURS), profileSystem, profileSystemUUID);
		registerEvent.add(ConfirmationKey, profileSystem, confirmationKeyDTO.getConfirmationKey()
		                                                                    .toString(), profileSystemUUID);

		return confirmationKeyDTO;
	}

	IInvolvedParty<?> updateLatestVisit(IEvent<?> event, ProfileServiceDTO<?> profileServiceDTO, IEnterprise<?> enterprise, IInvolvedParty<?> newIp,
	                                    UUID... identityToken)
	{
		UpdateLastVisitEvent req = get(UpdateLastVisitEvent.class);
		req.setEvent(event)
		   .setProfileServiceDTO(profileServiceDTO)
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

	@Override
	public IInvolvedParty<?> findInvolvedParty(UUID webClientToken, IEnterpriseName<?> enterpriseName)
	{
		IInvolvedPartyService<?> involvedPartyService = get(IInvolvedPartyService.class);
		IEnterprise<?> enterprise = get(IEnterpriseService.class)
				                            .getEnterprise(enterpriseName);

		ISystems<?> profileSystem = ProfileSystem.getSystemsMap()
		                                         .get(enterprise);
		UUID profileSystemUUID = ProfileSystem.getSystemTokens()
		                                      .get(enterprise);
		IInvolvedParty<?> party = involvedPartyService.findByIdentificationType(IdentificationTypeWebClientUUID,
		                                                                        webClientToken.toString(),
		                                                                        profileSystem,
		                                                                        profileSystemUUID);
		return party;
	}
}
