package com.guicedee.activitymaster.profiles;

import com.guicedee.activitymaster.core.ActivityMasterConfiguration;
import com.guicedee.activitymaster.core.services.dto.*;
import com.guicedee.activitymaster.core.services.enumtypes.IIdentificationType;
import com.guicedee.activitymaster.core.services.exceptions.SecurityAccessException;
import com.guicedee.activitymaster.core.services.security.Passwords;
import com.guicedee.activitymaster.core.services.system.IEventService;
import com.guicedee.activitymaster.core.services.system.IInvolvedPartyService;
import com.guicedee.activitymaster.core.services.system.ISecurityTokenService;
import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;
import com.guicedee.activitymaster.profiles.dto.UserDTO;
import com.guicedee.activitymaster.profiles.dto.UserSecurityDTO;
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
import com.guicedee.activitymaster.profiles.services.interfaces.IUserRole;
import com.guicedee.activitymaster.profiles.webdto.UserConfirmationKeyDTO;
import com.guicedee.activitymaster.profiles.dto.UserLoginDTO;
import com.guicedee.activitymaster.profiles.webdto.UserRegistrationDTO;
import com.guicedee.activitymaster.sessions.services.ISession;
import com.guicedee.activitymaster.sessions.services.ISessionMasterService;
import com.guicedee.guicedinjection.interfaces.JobService;
import com.guicedee.guicedinjection.pairing.Pair;
import com.guicedee.guicedservlets.GuicedServletKeys;
import com.jwebmp.core.base.ajax.AjaxCall;
import com.jwebmp.core.utilities.StaticStrings;
import net.sf.uadetector.ReadableUserAgent;

import jakarta.servlet.http.HttpServletRequest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.guicedee.activitymaster.core.services.classifications.classification.Classifications.*;
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
public class ProfileService
		implements IProfileService
{
	private static final Logger log = Logger.getLogger(ProfileService.class.getName());
	
	@Override
	public ProfileServiceDTO<?> loginUser(UserLoginDTO<?> profileServiceDTO, ISystems<?> system, UUID... identityToken) throws ProfileServiceException
	{
		IInvolvedPartyService<?> involvedPartyService = get(IInvolvedPartyService.class);
		IEnterprise<?> enterprise = system.getEnterprise();
		
		ISystems<?> profileSystem = get(ProfileSystem.class)
				.getSystem(enterprise);
		UUID profileSystemUUID = get(ProfileSystem.class)
				.getSystemToken(enterprise);
		
		if ((identityToken == null || identityToken.length == 0) && profileServiceDTO.getIdentityToken() == null)
		{
			identityToken = new UUID[]{profileSystemUUID};
		}
		IInvolvedParty<?> currentIp = get(ISession.class)
				.getInvolvedParty();
		IInvolvedParty<?> newIp = null;
		try
		{
			IInvolvedParty<?> foundParty = involvedPartyService.findByUsernameAndPassword(profileServiceDTO.getUserName(),
					profileServiceDTO.getPassword(),
					profileSystem,
					true,
					profileSystemUUID);
			profileServiceDTO.setIdentityToken(foundParty.getId());
			newIp = foundParty;
			
			if (currentIp != null && !currentIp.equals(newIp))
			{
				var idWebClient = currentIp.findIdentificationType(IdentificationTypeWebClientUUID, profileSystem,
						profileSystemUUID);
				
				if (idWebClient.isPresent() &&
						!currentIp.getId()
						          .equals(newIp.getId()))
				{
					ISessionMasterService<?> sessionMasterService = get(ISessionMasterService.class);
					ISession<?> session = sessionMasterService.getSession(currentIp, profileSystem, profileSystemUUID);
					sessionMasterService.expireSession(currentIp, session, system, profileSystemUUID);
					currentIp.moveWebClientUUIDToNewInvolvedParty(newIp,
							idWebClient.get()
							           .getValueAsUUID());
				}
			}
			setUserLoggedIn(newIp, profileServiceDTO, profileServiceDTO.isRememberMe(), system, profileSystemUUID);
			
		}
		catch (SecurityAccessException e)
		{
			throw new ProfileServiceException("Invalid username or password");
		}
		profileServiceDTO.setPassword(null);
		return profileServiceDTO;
	}
	
	@Override
	public ProfileServiceDTO<?> logoutUser(ProfileServiceDTO<?> profileServiceDTO, ISystems<?> system, UUID... identityToken) throws ProfileServiceException
	{
		//	IInvolvedPartyService<?> involvedPartyService = get(IInvolvedPartyService.class);
		IEnterprise<?> enterprise = system.getEnterprise();
		UUID profileSystemUUID = get(ProfileSystem.class)
				.getSystemToken(enterprise);
		
		if ((identityToken == null || identityToken.length == 0) && profileServiceDTO.getIdentityToken() == null)
		{
			identityToken = new UUID[]{profileSystemUUID};
		}
		
		//		IInvolvedParty<?> newIp = involvedPartyService.findByIdentificationType(IdentificationTypeWebClientUUID, profileServiceDTO.getWebClientUUID()
		//		                                                                                                                          .toString(), profileSystem, profileSystemUUID);
		ISession<?> iSess = get(ISession.class);
		UserSecurityDTO us = iSess.as("user-security", UserSecurityDTO.class);
		us.setRememberMe(false);
		us.setLoggedIn(false);
		us.setLoginExpiresOn(LocalDateTime.now());
		iSess.addValue("user-security", us);
		iSess.removeValue("user-roles");
		return profileServiceDTO;
	}
	
	@Override
	public ProfileServiceDTO<?> loginVisitor(ProfileServiceDTO<?> profileServiceDTO, ISystems<?> system, UUID... identityToken) throws ProfileServiceException
	{
		IInvolvedPartyService<?> involvedPartyService = get(IInvolvedPartyService.class);
		IEnterprise<?> enterprise = system.getEnterprise();
		
		ISystems<?> profileSystem = get(ProfileSystem.class)
				.getSystem(enterprise);
		UUID profileSystemUUID = get(ProfileSystem.class)
				.getSystemToken(enterprise);
		
		if ((identityToken == null || identityToken.length == 0) && profileServiceDTO.getIdentityToken() == null)
		{
			identityToken = new UUID[]{profileSystemUUID};
		}
		
		AjaxCall<?> ajaxCall = get(AjaxCall.class);
		Map<String, String> stringStringMap = ajaxCall.getVariable(StaticStrings.LOCAL_STORAGE_VARIABLE_KEY)
		                                              .asMap();
		
		IInvolvedParty<?> guestExists = involvedPartyService.findByIdentificationType(IdentificationTypeWebClientUUID, stringStringMap.get(StaticStrings.LOCAL_STORAGE_PARAMETER_KEY), profileSystem, identityToken);
		final UUID[] identityToken1Final = identityToken;
		IEventService<?> eventService = get(IEventService.class);
		IEvent<?> event = eventService.createEvent(ProfileEventTypes.SiteVisit, profileSystem, profileSystemUUID);
		IInvolvedParty<?> newIp = null;
		if (guestExists == null)
		{
			newIp = createNewVisitor(event, profileServiceDTO, enterprise, profileSystem, profileSystemUUID);
		}
		else
		{
			//Otherwise guess
			if (profileServiceDTO instanceof UserLoginDTO)
			{
				UserLoginDTO<?> userDTO = (UserLoginDTO<?>) profileServiceDTO;
				newIp = involvedPartyService.findByUsernameAndPassword(userDTO.getUserName()
						, userDTO.getPassword()
						, profileSystem
						, true
						, identityToken);
			}
			else
			{
				newIp = guestExists;
			}
		}
		
		ISessionMasterService<?> sessionMasterService = get(ISessionMasterService.class);
		ISession<?> session = null;
		session = sessionMasterService.getSession(newIp, profileSystem, profileSystemUUID);
		
		
		session.setInvolvedParty(newIp);
		sessionMasterService.updateSession(newIp, session, profileSystem, profileSystemUUID);
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
		
		if (guestExists != null && !guestExists.equals(newIp))
		{
			Optional<IRelationshipValue<IInvolvedParty<?>, IInvolvedPartyIdentificationType<?>, ?>> idWebClient = guestExists.findIdentificationType(IdentificationTypeWebClientUUID, profileSystem,
					profileSystemUUID);
			if (idWebClient.isPresent())
			{
				idWebClient.get()
				           .expire();
				newIp.addIdentificationType(IdentificationTypeWebClientUUID, idWebClient.get()
				                                                                        .getValue(), profileSystem, profileSystemUUID);
			}
		}
		profileServiceDTO.setInvolvedParty(newIp);
		session.setSystem(system);
		session.setInvolvedParty(newIp);
		
		Set<String> roles = new TreeSet<>();
		session.setInvolvedParty(newIp);
		UserSecurityDTO us;
		if (session.hasValue("user-security"))
		{
			us = session.as("user-security", UserSecurityDTO.class);
		}
		else
		{
			us = new UserSecurityDTO();
			session.addValue("user-security", us);
		}
		
		if (us.isLoggedIn())
		{
			//setUserLoggedIn(newIp, profileServiceDTO, us.isRememberMe(), system, identityToken);
			IRolesService<?> rolesService = get(IRolesService.class);
			roles.addAll(rolesService.getRoles(session.getInvolvedParty(), profileSystem, profileSystemUUID));
		}
		else
		{
			roles.addAll(Set.of(Visitor.toString()));
		}
		session.setSystem(system);
		session.addValue("user-roles", roles);
		session.addValue("user-security", us);
		sessionMasterService.updateSession(newIp, session, profileSystem, profileSystemUUID);
		
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
				.getVisitorsGuestsFolder(profileSystem, identityToken);
		
		ISecurityToken<?> myToken = get(ISecurityTokenService.class).create(Identity,
				profileServiceDTO.getWebClientUUID()
				                 .toString(),
				"A new visitor device",
				profileSystem,
				visitorsGroup,
				identityToken);
		newIp.addOrUpdateIdentificationType(IdentificationTypeUUID, NoClassification.classificationName(), myToken.getSecurityToken(), profileSystem, identityToken);
		
		profileServiceDTO.setIdentityToken(newIp.getId());
		UpdateNewVisitEvent visitEvent = get(UpdateNewVisitEvent.class);
		visitEvent.setEnterprise(enterprise)
		          .setEvent(event)
		          .setProfileServiceDTO(profileServiceDTO)
		          .setIdentityToken(new UUID[]{newIp.getId()})
		          .setNewIp(newIp)
		          .setProfileSystem(profileSystem);
		
		UUID profileSystemUUID = get(ProfileSystem.class)
				.getSystemToken(enterprise);
		
		get(IRolesService.class).addRole(newIp, Visitor.toString(), profileServiceDTO, profileSystem, profileSystemUUID);
		
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
	
	@Override
	public UserConfirmationKeyDTO<?> registerVisitor(UserRegistrationDTO<?> userRegistrationDTO, ISystems<?> system, UUID... identityToken) throws UserExistsException, WaitingForConfirmationKeyException
	{
		IInvolvedPartyService<?> involvedPartyService = get(IInvolvedPartyService.class);
		IEnterprise<?> enterprise = system.getEnterprise();
		ISystems<?> profileSystem = get(ProfileSystem.class)
				.getSystem(enterprise);
		UUID profileSystemUUID = get(ProfileSystem.class)
				.getSystemToken(enterprise);
		
		IEvent<?> registerEvent = get(IEventService.class)
				.createEvent(UserRegistered, profileSystem, profileSystemUUID);
		
		IInvolvedParty<?> ipExists = involvedPartyService.findByIdentificationType(IdentificationTypeEmailAddress,
				new Passwords().integerEncrypt(userRegistrationDTO.getUserName()
				                                                  .getBytes())
				, profileSystem, profileSystemUUID);
		if (ipExists != null)
		{
			if (ipExists.hasClassifications(ConfirmationKey, profileSystem, identityToken))
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
		var idType
				= newIp.addOrUpdateIdentificationType(IdentificationTypeEmailAddress,
				NoClassification.classificationName(),
				new Passwords().integerEncrypt(userRegistrationDTO.getUserName()
				                                                  .getBytes()),
				profileSystem,
				profileSystemUUID);
		
		newIp.expireIdentificationType(idType, Duration.of(2, HOURS));
		
		involvedPartyService.addUpdateUsernamePassword(registerEvent, userRegistrationDTO.getUserName(), userRegistrationDTO.getPassword(), newIp, profileSystem,
				profileSystemUUID);
		
		userRegistrationDTO.setPassword(null);
		var idUserNameType
				= newIp.addOrUpdateIdentificationType(IdentificationTypeUserName,
				NoClassification.classificationName(),
				new Passwords().integerEncrypt(userRegistrationDTO.getUserName()
				                                                  .getBytes()),
				profileSystem,
				profileSystemUUID);
		newIp.expireIdentificationType(idUserNameType, Duration.of(2, HOURS));
		
		newIp.expire(SecurityPassword, Duration.of(2, HOURS), profileSystem, profileSystemUUID);
		newIp.expire(SecurityPasswordSalt, Duration.of(2, HOURS), profileSystem, profileSystemUUID);
		
		UserConfirmationKeyDTO confirmationKeyDTO = (UserConfirmationKeyDTO) new UserConfirmationKeyDTO()
				.setWebClientUUID(userRegistrationDTO.getWebClientUUID())
				.setIdentityToken(newIp.getId());
		confirmationKeyDTO.setConfirmationKey(UUID.randomUUID());
		IRelationshipValue<IInvolvedParty<?>, IClassification<?>, ?> x = newIp.addOrUpdate(ConfirmationKey, null, confirmationKeyDTO.getConfirmationKey()
		                                                                                                                            .toString(), profileSystem, profileSystemUUID);
		
		x.expire(Duration.of(2, HOURS), profileSystem, profileSystemUUID);
		registerEvent.addInvolvedParty(ConfirmationKey, profileSystem, confirmationKeyDTO.getConfirmationKey()
		                                                                                 .toString(), profileSystemUUID);
		
		return confirmationKeyDTO;
	}
	
	@Override
	public IInvolvedParty<?> findInvolvedParty(UUID webClientToken, ISystems<?> system)
	{
		IInvolvedPartyService<?> involvedPartyService = get(IInvolvedPartyService.class);
		IEnterprise<?> enterprise = system.getEnterprise();
		ISystems<?> profileSystem = get(ProfileSystem.class)
				.getSystem(enterprise);
		UUID profileSystemUUID = get(ProfileSystem.class)
				.getSystemToken(enterprise);
		IInvolvedParty<?> party = involvedPartyService.findByIdentificationType(IdentificationTypeWebClientUUID,
				webClientToken.toString(),
				profileSystem,
				profileSystemUUID);
		return party;
	}
	
	@Override
	public IInvolvedParty<?> findInvolvedParty(UUID webClientToken)
	{
		IInvolvedPartyService<?> involvedPartyService = get(IInvolvedPartyService.class);
		return involvedPartyService.findByIdentificationType(IdentificationTypeWebClientUUID.toString(), webClientToken.toString());
	}
	
	
	private void setUserLoggedIn(IInvolvedParty<?> newIp, ProfileServiceDTO<?> profileServiceDTO, boolean rememberMe, ISystems<?> system, UUID... identityToken)
	{
		IInvolvedPartyService<?> involvedPartyService = get(IInvolvedPartyService.class);
		IEnterprise<?> enterprise = system.getEnterprise();
		ISystems<?> profileSystem = get(ProfileSystem.class)
				.getSystem(enterprise);
		UUID profileSystemUUID = get(ProfileSystem.class)
				.getSystemToken(enterprise);
		
		if ((identityToken == null || identityToken.length == 0) && profileServiceDTO.getIdentityToken() == null)
		{
			identityToken = new UUID[]{profileSystemUUID};
		}
		
		ISession<?> iSess = get(ISession.class);
		iSess.setInvolvedParty(newIp);
		
		ProfileServiceDTO<?> identityTokensDTO = new ProfileServiceDTO<>();
		identityTokensDTO.setWebClientUUID(profileServiceDTO.getWebClientUUID());
		identityTokensDTO.setIdentityToken(profileServiceDTO.getIdentityToken());
		identityTokensDTO.setEnterprise(system.getEnterprise());
		iSess.addValue("identity", identityTokensDTO);
		
		UserSecurityDTO us = null;
		if (iSess.hasValue("user-security"))
		{
			us = iSess.as("user-security", UserSecurityDTO.class);
		}
		else
		{
			iSess.addValue("user-security", us = new UserSecurityDTO());
		}
		us = us.setLoggedIn(true)
		       .setLoginExpiresOn(rememberMe
				       ? LocalDateTime.MAX
				       : LocalDateTime.now()
				                      .plusMinutes(20))
		       .setRememberMe(rememberMe);
		iSess.addValue("user-security", us);
		IRolesService<?> rolesService = get(IRolesService.class);
		Set<String> roles = rolesService.getRoles(newIp, profileSystem, profileSystemUUID);
		iSess.addValue("user-roles", roles);
		
		profileServiceDTO.setEnterprise(enterprise);
		profileServiceDTO.setInvolvedParty(newIp);
	}
}
