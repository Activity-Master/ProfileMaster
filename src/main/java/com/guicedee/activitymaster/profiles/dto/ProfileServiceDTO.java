package com.guicedee.activitymaster.profiles.dto;

import com.fasterxml.jackson.annotation.*;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.guicedee.activitymaster.fsdm.client.services.IInvolvedPartyService;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.exceptions.ActivityMasterException;
import com.guicedee.activitymaster.profiles.ProfileSystem;
import com.guicedee.activitymaster.profiles.services.interfaces.IRolesService;
import com.guicedee.guicedinjection.GuiceContext;
import com.guicedee.guicedservlets.services.scopes.CallScope;
import lombok.*;

import java.util.*;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.*;
import static com.guicedee.activitymaster.profiles.enumerations.ProfileIdentificationTypes.*;
import static com.guicedee.activitymaster.profiles.services.interfaces.IProfileService.*;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = ANY,
                getterVisibility = NONE,
                setterVisibility = NONE)
@EqualsAndHashCode(of = {"webClientUUID"}, callSuper = false)
@CallScope
public class ProfileServiceDTO<J extends ProfileServiceDTO<J>>
		extends UserDTO<J>
{
	public static final String IDENTITY_SESSION_NAME = "identity";
	
	@JsonProperty
	private UUID webClientUUID;
	@JsonIgnore
	private transient IInvolvedParty<?, ?> involvedParty;
	
	@JsonIgnore
	@Getter
	@Setter
	private transient Map<String, Object> sessionUpdates;
	
	@Inject
	@JsonIgnore
	private ProfileSystem profileSystem;
	@Inject
	@JsonIgnore
	private IRolesService<?> rolesService;
	
	@Inject
	@JsonIgnore
	private IInvolvedPartyService<?> involvedPartyService;
	
	@Inject
	@Named(ProfileSystemName)
	@JsonIgnore
	private ISystems<?, ?> system;
	@Inject
	@Named(ProfileSystemName)
	@JsonIgnore
	private UUID  identityToken;
	
	public UUID getWebClientUUID()
	{
		return webClientUUID;
	}
	
	public J setWebClientUUID(UUID webClientUUID)
	{
		this.webClientUUID = webClientUUID;
		return (J) this;
	}
	
	public Set<String> findRoles()
	{
		if (profileSystem == null)
		{
			GuiceContext.inject()
			            .injectMembers(this);
		}
		ISystems<?, ?> system = profileSystem.getSystem(getEnterprise());
		UUID systemToken = profileSystem.getSystemToken(getEnterprise());
		
		if (this.involvedParty == null)
		{
			this.involvedParty = findInvolvedParty(system, systemToken);
		}
		Set<String> rolesss = rolesService.getRoles(this.involvedParty, system, systemToken);
		return rolesss;
	}
	
	public IInvolvedParty<?, ?> findInvolvedParty(ISystems<?, ?> system, UUID identityToken)
	{
		if (involvedPartyService == null)
		{
			GuiceContext.inject()
			            .injectMembers(this);
		}
		if (this.involvedParty == null)
		{
			if (webClientUUID != null)
			{
				
				this.involvedParty =
						involvedPartyService.get()
						                    .builder()
						                    .findByIdentificationType(IdentificationTypeWebClientUUID.toString(), getWebClientUUID().toString(),
								                    system, identityToken)
						                    .get()
						                    .orElse(null);
				
				setIdentityToken(involvedParty.getId());
			}
		}
		return this.involvedParty;
	}
	
	public IInvolvedParty<?, ?> findInvolvedParty()
	{
		if (involvedPartyService == null)
		{
			GuiceContext.inject()
			            .injectMembers(this);
		}
		if (this.involvedParty == null)
		{
			if (webClientUUID != null)
			{
				try
				{
					this.involvedParty = involvedPartyService.get()
					                                         .builder()
					                                         .findByIdentificationType(IdentificationTypeWebClientUUID.toString(),
							                                         getWebClientUUID().toString(),
							                                         system,
							                                         identityToken)
					                                         .get()
					                                         .orElse(null);
				}catch (ActivityMasterException  e)
				{
					//
				}
				if (this.involvedParty == null)
				{
					return null;
				}
				setIdentityToken(involvedParty.getId());
			}
		}
		return this.involvedParty;
	}
	
	public J setInvolvedParty(IInvolvedParty<?, ?> involvedParty)
	{
		this.involvedParty = involvedParty;
		return (J) this;
	}
}
