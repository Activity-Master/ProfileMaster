package com.armineasy.activitymaster.profiles;

import com.armineasy.activitymaster.activitymaster.ActivityMasterConfiguration;
import com.armineasy.activitymaster.activitymaster.db.entities.events.Event;
import com.armineasy.activitymaster.activitymaster.db.entities.involvedparty.InvolvedParty;
import com.armineasy.activitymaster.activitymaster.db.entities.involvedparty.InvolvedPartyXClassification;
import com.armineasy.activitymaster.activitymaster.db.entities.involvedparty.InvolvedPartyXInvolvedPartyIdentificationType;
import com.armineasy.activitymaster.activitymaster.db.entities.security.SecurityToken;
import com.armineasy.activitymaster.activitymaster.implementations.InvolvedPartyService;
import com.armineasy.activitymaster.activitymaster.implementations.SecurityTokenService;
import com.armineasy.activitymaster.activitymaster.services.IIdentificationType;
import com.armineasy.activitymaster.activitymaster.services.classifications.enterprise.IEnterpriseName;
import com.armineasy.activitymaster.activitymaster.services.dto.IEnterprise;
import com.armineasy.activitymaster.activitymaster.services.dto.ISystems;
import com.armineasy.activitymaster.activitymaster.services.system.IEnterpriseService;
import com.armineasy.activitymaster.activitymaster.services.system.IEventService;
import com.armineasy.activitymaster.profiles.dto.ProfileServiceDTO;
import com.armineasy.activitymaster.profiles.dto.UserConfirmationKeyDTO;
import com.armineasy.activitymaster.profiles.dto.UserDTO;
import com.armineasy.activitymaster.profiles.dto.UserRegistrationDTO;
import com.armineasy.activitymaster.profiles.enumerations.ProfileEventTypes;
import com.armineasy.activitymaster.profiles.events.UpdateNewVisitEvent;
import com.armineasy.activitymaster.profiles.events.visits.ConfigureFromReadableUserAgentEvent;
import com.armineasy.activitymaster.profiles.events.visits.ConfigureFromServletRequestEvent;
import com.armineasy.activitymaster.profiles.events.visits.UpdateLastVisitEvent;
import com.armineasy.activitymaster.profiles.exceptions.ProfileServiceException;
import com.armineasy.activitymaster.profiles.exceptions.UserExistsException;
import com.armineasy.activitymaster.profiles.exceptions.WaitingForConfirmationKeyException;
import com.google.inject.Singleton;
import com.jwebmp.guicedinjection.GuiceContext;
import com.jwebmp.guicedinjection.interfaces.JobService;
import com.jwebmp.guicedinjection.pairing.Pair;
import com.jwebmp.guicedservlets.GuicedServletKeys;
import lombok.extern.java.Log;
import net.sf.uadetector.ReadableUserAgent;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

import static com.armineasy.activitymaster.activitymaster.services.classifications.securitytokens.SecurityTokenClassifications.*;
import static com.armineasy.activitymaster.activitymaster.services.types.IdentificationTypes.*;
import static com.armineasy.activitymaster.profiles.enumerations.ProfileClassifications.*;
import static com.armineasy.activitymaster.profiles.enumerations.ProfileEventTypes.*;
import static com.armineasy.activitymaster.profiles.enumerations.ProfileIdentificationTypes.*;
import static com.jwebmp.guicedinjection.GuiceContext.*;
import static java.time.temporal.ChronoUnit.*;

@Singleton
@Log
public class ProfileService
		implements com.armineasy.activitymaster.profiles.services.interfaces.IProfileService
{

	@Override
	public ProfileServiceDTO<?> loginVisitor(ProfileServiceDTO<?> profileServiceDTO, IEnterpriseName<?> enterpriseName, UUID... identityToken) throws ProfileServiceException
	{
		InvolvedPartyService involvedPartyService = GuiceContext.get(InvolvedPartyService.class);
		IEnterprise<?> enterprise = GuiceContext.get(IEnterpriseService.class)
		                                        .getEnterprise(enterpriseName);

		ISystems profileSystem = ProfileSystem.getNewSystem()
		                                      .get(enterprise);
		UUID profileSystemUUID = ProfileSystem.getSystemTokens()
		                                      .get(enterprise);

		if ((identityToken == null || identityToken.length == 0) && profileServiceDTO.getIdentityToken() == null)
		{
			identityToken = new UUID[]{profileSystemUUID};
		}

		Optional<UserDTO<?>> guestExists = findByKey(IdentificationTypeWebClientUUID, profileServiceDTO.getWebClientUUID(), enterprise, identityToken);
		final UUID[] identityToken1Final = identityToken;
		Event event = get(IEventService.class).createEvent(ProfileEventTypes.SiteVisit, profileSystem, profileSystemUUID);
		InvolvedParty newIp;
		if (guestExists.isEmpty())
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
			HttpServletRequest request = GuiceContext.get(GuicedServletKeys.getHttpServletRequestKey());
			newIp = configureFromHTTPServletRequest(event, profileServiceDTO, newIp, profileSystem, request, enterprise);
			configureFromReadableUserAgent(event, profileServiceDTO, newIp, get(ReadableUserAgent.class), profileSystem, enterprise, identityToken1Final);
		}
		catch (Throwable T)
		{
			log.log(Level.FINER, "Unable to log servlet request information", T);
		}

		Optional<InvolvedPartyXInvolvedPartyIdentificationType> id = newIp.findIdentificationType(IdentificationTypeUUID, profileSystem, profileSystemUUID);
		id.ifPresent(involvedPartyXInvolvedPartyIdentificationType -> profileServiceDTO.setIdentityToken(
				java.util.UUID.fromString(involvedPartyXInvolvedPartyIdentificationType.getValue()))
		            );

		return profileServiceDTO;
	}


	InvolvedParty createNewVisitor(Event event, ProfileServiceDTO<?> profileServiceDTO, IEnterprise<?> enterprise, ISystems profileSystem, UUID... identityToken)
	{
		InvolvedPartyService involvedPartyService = GuiceContext.get(InvolvedPartyService.class);
		InvolvedParty newIp;
		//Create new guest record
		Pair<IIdentificationType, String> guestIDType = new Pair<>();
		guestIDType.setKey(IdentificationTypeWebClientUUID)
		           .setValue(profileServiceDTO.getWebClientUUID()
		                                      .toString());

		newIp = involvedPartyService.create(profileSystem, guestIDType, true, identityToken);


		SecurityToken visitorsGroup = GuiceContext.get(SecurityTokenService.class)
		                                          .getVisitorsGuestsFolder(enterprise, identityToken);

		SecurityToken myToken = get(SecurityTokenService.class).create(Identity,
		                                                               profileServiceDTO.getWebClientUUID()
		                                                                                .toString(),
		                                                               "A new visitor device",
		                                                               profileSystem,
		                                                               visitorsGroup,
		                                                               identityToken);
		newIp.addIdentificationType(IdentificationTypeUUID, profileSystem, myToken.getSecurityToken(), identityToken);

		profileServiceDTO.setIdentityToken(java.util.UUID.fromString(myToken.getSecurityToken()));
		UpdateNewVisitEvent visitEvent = GuiceContext.get(UpdateNewVisitEvent.class);
		visitEvent.setEnterprise(enterprise)
		          .setEvent(event)
		          .setProfileServiceDTO(profileServiceDTO)
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

	InvolvedParty configureFromReadableUserAgent(Event event, UserDTO<?> dto, InvolvedParty ip, ReadableUserAgent readableUserAgent, ISystems profileSystem, IEnterprise<?> enterprise, UUID... identityToken)
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

	InvolvedParty configureFromHTTPServletRequest(Event event, UserDTO<?> dto, InvolvedParty ip, ISystems profileSystem, HttpServletRequest servletRequest, IEnterprise<?> enterprise)
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

	@Override
	public UserConfirmationKeyDTO<?> registerVisitor(UserRegistrationDTO<?> userRegistrationDTO, IEnterpriseName<?> enterpriseName, UUID... identityToken) throws UserExistsException, WaitingForConfirmationKeyException
	{
		InvolvedPartyService involvedPartyService = GuiceContext.get(InvolvedPartyService.class);
		IEnterprise<?> enterprise = GuiceContext.get(IEnterpriseService.class)
		                                    .getEnterprise(enterpriseName);

		ISystems profileSystem = ProfileSystem.getNewSystem()
		                                      .get(enterprise);
		UUID profileSystemUUID = ProfileSystem.getSystemTokens()
		                                      .get(enterprise);


		Event registerEvent = GuiceContext.get(IEventService.class)
		                                  .createEvent(UserRegistered, profileSystem, profileSystemUUID);

		InvolvedParty ipExists = involvedPartyService.findByIdentificationType(IdentificationTypeEmailAddress, userRegistrationDTO.getUserName(), profileSystem, profileSystemUUID);
		if (ipExists != null)
		{
			if(ipExists.has(ConfirmationKey, profileSystem, identityToken))
			{
				throw new WaitingForConfirmationKeyException("The email address is waiting for a confirmation key");
			}
			throw new UserExistsException("That email address is already in use as a valid identifier");
		}
		//ActivityMasterConfiguration.get().setSecurityEnabled(false);
		InvolvedParty newIp;
			newIp = involvedPartyService.findByIdentificationType(IdentificationTypeWebClientUUID, userRegistrationDTO.getWebClientUUID()
			                                                                                                                        .toString(), profileSystem, profileSystemUUID);
		//ActivityMasterConfiguration.get().setSecurityEnabled(true);
		InvolvedPartyXInvolvedPartyIdentificationType typeCreated =	newIp.addIdentificationType(IdentificationTypeEmailAddress, profileSystem, userRegistrationDTO.getUserName(), profileSystemUUID);
		typeCreated.setEffectiveToDate(LocalDateTime.now()
		                                            .plus(2, HOURS));
		typeCreated.updateNow();

		typeCreated = involvedPartyService.addUpdateUsernamePassword(registerEvent,userRegistrationDTO.getUserName(), userRegistrationDTO.getPassword(), newIp, profileSystem, profileSystemUUID);
		userRegistrationDTO.setPassword(null);
		typeCreated.setEffectiveToDate(LocalDateTime.now()
		                                            .plus(2, HOURS));
		typeCreated.updateNow();

		UserConfirmationKeyDTO confirmationKeyDTO = (UserConfirmationKeyDTO) new UserConfirmationKeyDTO()
				                                                                     .setWebClientUUID(userRegistrationDTO.getWebClientUUID())
				                                                                     .setIdentityToken(userRegistrationDTO.getIdentityToken());
		confirmationKeyDTO.setConfirmationKey(UUID.randomUUID());
		InvolvedPartyXClassification x = newIp.addOrUpdate(ConfirmationKey, confirmationKeyDTO.getConfirmationKey()
		                                                                          .toString(), profileSystem, profileSystemUUID);
		x.setEffectiveToDate(LocalDateTime.now()
		                                  .plus(2, HOURS));
		x.updateNow();
		registerEvent.add(ConfirmationKey, profileSystem, confirmationKeyDTO.getConfirmationKey()
		                                                                    .toString(),profileSystemUUID);

		return confirmationKeyDTO;
	}

	InvolvedParty updateLatestVisit(Event event, ProfileServiceDTO<?> profileServiceDTO, IEnterprise<?> enterprise, InvolvedParty newIp,
	                                UUID... identityToken)
	{
		UpdateLastVisitEvent req = GuiceContext.get(UpdateLastVisitEvent.class);
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

	public Optional<UserDTO<?>> findByKey(IIdentificationType<?> identificationType, UUID webClientKey, IEnterprise<?> enterprise, UUID... identityToken) throws ProfileServiceException
	{
		ISystems profileSystem = ProfileSystem.getNewSystem()
		                                      .get(enterprise);
		InvolvedPartyService service = GuiceContext.get(InvolvedPartyService.class);
			InvolvedParty ip = service.findByIdentificationType(identificationType, webClientKey.toString(), profileSystem, identityToken);
			if(ip == null)
				return Optional.empty();
			return Optional.of(new UserDTO<>().fromIP(ip));
	}

}
