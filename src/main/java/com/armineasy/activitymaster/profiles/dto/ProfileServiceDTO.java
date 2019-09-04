package com.armineasy.activitymaster.profiles.dto;

import com.armineasy.activitymaster.activitymaster.services.dto.IEnterprise;
import com.armineasy.activitymaster.activitymaster.services.dto.IInvolvedParty;
import com.armineasy.activitymaster.activitymaster.services.dto.ISystems;
import com.armineasy.activitymaster.activitymaster.services.system.IEnterpriseService;
import com.armineasy.activitymaster.activitymaster.services.system.IInvolvedPartyService;
import com.armineasy.activitymaster.profiles.ProfileSystem;
import com.armineasy.activitymaster.profiles.services.interfaces.IRolesService;
import com.armineasy.activitymaster.profiles.services.interfaces.IUserRole;
import com.fasterxml.jackson.annotation.*;
import com.jwebmp.guicedinjection.GuiceContext;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
	@JsonIgnore
	private transient IInvolvedParty<?> involvedParty;

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
		IEnterprise<?> enterprise = get(IEnterpriseService.class).getEnterprise(getEnterprise());
		ISystems profileSystem = ProfileSystem.getNewSystem()
		                                      .get(enterprise);
		UUID profileSystemUUID = ProfileSystem.getSystemTokens()
		                                      .get(enterprise);

		IInvolvedPartyService<?> involvedPartyService = get(IInvolvedPartyService.class);

		IInvolvedParty<?> newIp = involvedPartyService.findByIdentificationType(IdentificationTypeWebClientUUID, getWebClientUUID()
				                                                                                                         .toString(),
		                                                                        profileSystem, profileSystemUUID);


		boolean loggedOn = false;
		try
		{
			loggedOn = newIp.find(LoggedOn, profileSystem, profileSystemUUID)
			                .get()
			                .getValueAsBoolean();
		}
		catch (Exception nsfe)
		{
			loggedOn = false;
		}

		if (loggedOn && !asVisitor)
		{
			return true;
		}
		else
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
		IInvolvedParty<?> newIp = findInvolvedParty();
		if (getRoles() != null)
		{
			getRoles().clear();
		}
		IRolesService rolesService = GuiceContext.get(IRolesService.class);
		IEnterprise<?> enterprise = get(IEnterpriseService.class).getEnterprise(getEnterprise());
		//involvedPartyService.findByUUID(getIdentityToken(), enterprise, systemUUID);
		List<IUserRole<?>> rolesss = rolesService.getRoles(newIp, this, ProfileSystem.getNewSystem()
		                                                                             .get(enterprise), ProfileSystem.getSystemTokens()
		                                                                                                            .get(enterprise));
		setRoles(new LinkedHashSet<>(rolesss));
		return getRoles();
	}

	public IInvolvedParty<?> findInvolvedParty()
	{
		if (this.involvedParty == null)
		{
			IEnterprise<?> enterprise = get(IEnterpriseService.class).getEnterprise(getEnterprise());
			ISystems profileSystem = ProfileSystem.getNewSystem()
			                                      .get(enterprise);
			UUID profileSystemUUID = ProfileSystem.getSystemTokens()
			                                      .get(enterprise);
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
