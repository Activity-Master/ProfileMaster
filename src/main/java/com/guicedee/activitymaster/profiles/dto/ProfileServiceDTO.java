package com.guicedee.activitymaster.profiles.dto;

import com.fasterxml.jackson.annotation.*;
import com.guicedee.activitymaster.core.services.dto.IEnterprise;
import com.guicedee.activitymaster.core.services.dto.IInvolvedParty;
import com.guicedee.activitymaster.core.services.dto.ISystems;
import com.guicedee.activitymaster.core.services.system.IInvolvedPartyService;
import com.guicedee.activitymaster.profiles.ProfileSystem;
import com.guicedee.activitymaster.profiles.services.interfaces.IRolesService;
import com.guicedee.activitymaster.profiles.services.interfaces.IUserRole;
import com.guicedee.activitymaster.sessions.services.ISession;
import com.guicedee.guicedinjection.GuiceContext;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.*;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.*;
import static com.guicedee.activitymaster.profiles.enumerations.ProfileIdentificationTypes.*;
import static com.guicedee.guicedinjection.GuiceContext.*;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = ANY,
                getterVisibility = NONE,
                setterVisibility = NONE)
public class ProfileServiceDTO<J extends ProfileServiceDTO<J>>
		extends UserDTO<J>
{
	@JsonProperty
	private UUID webClientUUID;
	@JsonIgnore
	private transient IInvolvedParty<?> involvedParty;
	@JsonIgnore
	@Getter
	@Setter
	private transient Map<String, Object> sessionUpdates;
	
	@JsonIgnore
	public boolean isLoggedIn(boolean asVisitor)
	{
		ISession<?> session = get(ISession.class);
		
		ISystems<?> system = session.getSystem();
		ISystems<?> profileSystem = get(ProfileSystem.class).getSystem(system.getEnterprise());
		UUID profileSystemUUID = get(ProfileSystem.class).getSystemToken(system.getEnterprise());
		
		IInvolvedPartyService<?> involvedPartyService = get(IInvolvedPartyService.class);
		
		IInvolvedParty<?> newIp = involvedPartyService.findByIdentificationType(IdentificationTypeWebClientUUID, getWebClientUUID()
						.toString(),
				profileSystem, profileSystemUUID);
		if (!session.hasValue("user-security"))
		{
			return false;
		}
		
		UserSecurity us = session.as("user-security", UserSecurity.class);
		if (us == null ||
				us.getLoginExpiresOn() == null ||
				us.getLoginExpiresOn()
				  .isBefore(LocalDateTime.now()))
		{
			return false;
		}
		
		boolean loggedOn = false;
		try
		{
			loggedOn = us.isLoggedIn();
		}
		catch (Exception nsfe)
		{
			loggedOn = false;
		}
		
		if (loggedOn && !asVisitor)
		{
			return true;
		}
		
		if (us.isRememberMe())
		{
			return loggedOn;
		}
		return false;
	}
	
	public UUID getWebClientUUID()
	{
		return webClientUUID;
	}
	
	public J setWebClientUUID(UUID webClientUUID)
	{
		this.webClientUUID = webClientUUID;
		return (J) this;
	}
	
	public Set<IUserRole<?>> findRoles()
	{
		ISession<?> session = get(ISession.class);
		IInvolvedParty<?> newIp = findInvolvedParty();
		IRolesService<?> rolesService = GuiceContext.get(IRolesService.class);
		ISystems<?> profileSystem = get(ProfileSystem.class).getSystem(session.getInvolvedParty()
		                                                                      .getEnterprise());
		UUID profileSystemUUID = get(ProfileSystem.class).getSystemToken(session.getInvolvedParty()
		                                                                        .getEnterprise());
		List<IUserRole<?>> rolesss = rolesService.getRoles(newIp, profileSystem, profileSystemUUID);
		return new LinkedHashSet<>(rolesss);
	}
	
	public IInvolvedParty<?> findInvolvedParty()
	{
		if (this.involvedParty == null)
		{
			ISession<?> session = get(ISession.class);
			ISystems<?> profileSystem = get(ProfileSystem.class).getSystem(session.getInvolvedParty()
			                                                                      .getEnterprise());
			UUID profileSystemUUID = get(ProfileSystem.class).getSystemToken(session.getInvolvedParty()
			                                                                        .getEnterprise());
			IInvolvedPartyService<?> involvedPartyService = get(IInvolvedPartyService.class);
			this.involvedParty = involvedPartyService.findByIdentificationType(IdentificationTypeWebClientUUID, getWebClientUUID()
							.toString(),
					profileSystem, profileSystemUUID);
		}
		return this.involvedParty;
	}
	
	public J setInvolvedParty(IInvolvedParty<?> involvedParty)
	{
		this.involvedParty = involvedParty;
		return (J) this;
	}
}
