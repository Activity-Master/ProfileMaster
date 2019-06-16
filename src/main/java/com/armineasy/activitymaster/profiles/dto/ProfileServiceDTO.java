package com.armineasy.activitymaster.profiles.dto;

import com.armineasy.activitymaster.activitymaster.services.dto.IInvolvedParty;
import com.armineasy.activitymaster.activitymaster.services.dto.ISystems;
import com.armineasy.activitymaster.activitymaster.services.system.IInvolvedPartyService;
import com.armineasy.activitymaster.profiles.ProfileSystem;
import com.armineasy.activitymaster.profiles.services.interfaces.IRolesService;
import com.armineasy.activitymaster.profiles.services.interfaces.IUserRole;
import com.fasterxml.jackson.annotation.*;
import com.jwebmp.guicedinjection.GuiceContext;

import java.util.*;

import static com.armineasy.activitymaster.profiles.enumerations.ProfileClassifications.*;
import static com.armineasy.activitymaster.profiles.enumerations.ProfileIdentificationTypes.*;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.*;
import static com.jwebmp.guicedinjection.GuiceContext.*;

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

	@SuppressWarnings("unchecked")
	@Override
	public J fromIP(IInvolvedParty<?> ip)
	{
		super.fromIP(ip);
		if (ip.has(IdentificationTypeWebClientUUID,
		           ProfileSystem.getNewSystem()
		                        .get(ip.getEnterpriseID()),
		           ProfileSystem.getSystemTokens()
		                        .get(ip.getEnterpriseID())
		          ))
		{
			webClientUUID = UUID.fromString(ip.find(IdentificationTypeWebClientUUID,
			                                        ProfileSystem.getNewSystem()
			                                                     .get(ip.getEnterpriseID()),
			                                        ProfileSystem.getSystemTokens()
			                                                     .get(ip.getEnterpriseID()))
			                                  .orElseThrow()
			                                  .getValue());
		}
		return (J) this;
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

	@JsonIgnore
	public boolean isLoggedIn(boolean asVisitor)
	{
		ISystems profileSystem = ProfileSystem.getNewSystem()
		                                      .get(getEnterprise());
		UUID profileSystemUUID = ProfileSystem.getSystemTokens()
		                                      .get(getEnterprise());

		IInvolvedParty<?> newIp = get(IInvolvedPartyService.class).findByIdentificationType(IdentificationTypeWebClientUUID, getWebClientUUID()
				                                                                                                                     .toString(), profileSystem, profileSystemUUID);
		boolean loggedOn = false;
		try
		{
			loggedOn = newIp.find(LoggedOn, profileSystem, profileSystemUUID)
			                .get()
			                .getValueAsBoolean();
		}catch(NoSuchElementException nsfe)
		{

		}

		if(loggedOn && !asVisitor)
		{
			return true;
		}else
		{

		}

		if (newIp.has(RememberMe, profileSystem, profileSystemUUID))
		{
			if (newIp.find(RememberMe, profileSystem, profileSystemUUID)
			         .get()
			         .getValueAsBoolean())
			{
				return loggedOn;
			}
		}
		return false;
	}

	public Set<IUserRole<?>> findRoles()
	{
		if (getRoles() != null)
		{
			getRoles().clear();
		}
		IRolesService rolesService = GuiceContext.get(IRolesService.class);
		List<IUserRole<?>> rolesss = rolesService.getRoles(this, ProfileSystem.getNewSystem()
		                                                                      .get(getEnterprise()), ProfileSystem.getSystemTokens()
		                                                                                                          .get(getEnterprise()));
		setRoles(new LinkedHashSet<>(rolesss));

		return getRoles();
	}

}
