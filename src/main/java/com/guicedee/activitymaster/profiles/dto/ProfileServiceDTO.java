package com.guicedee.activitymaster.profiles.dto;

import com.fasterxml.jackson.annotation.*;
import com.guicedee.activitymaster.core.services.dto.IEnterprise;
import com.guicedee.activitymaster.core.services.dto.IInvolvedParty;
import com.guicedee.activitymaster.core.services.dto.ISystems;
import com.guicedee.activitymaster.core.services.system.IInvolvedPartyService;
import com.guicedee.activitymaster.profiles.ProfileSystem;
import com.guicedee.activitymaster.profiles.services.interfaces.IRolesService;
import com.guicedee.guicedinjection.GuiceContext;
import lombok.*;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.guicedee.activitymaster.profiles.enumerations.ProfileIdentificationTypes.IdentificationTypeWebClientUUID;
import static com.guicedee.guicedinjection.GuiceContext.get;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = ANY,
                getterVisibility = NONE,
                setterVisibility = NONE)
@EqualsAndHashCode(of = {"webClientUUID"},callSuper = false)
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
	
	public UUID getWebClientUUID()
	{
		return webClientUUID;
	}
	
	public J setWebClientUUID(UUID webClientUUID)
	{
		this.webClientUUID = webClientUUID;
		return (J) this;
	}
	
	@JsonGetter
	public Set<String> findRoles()
	{
		ISystems<?> system = get(ProfileSystem.class).getSystem((IEnterprise<?>) getEnterprise());
		UUID systemToken = get(ProfileSystem.class).getSystemToken((IEnterprise<?>) getEnterprise());
		
		if (this.involvedParty == null)
		{
			this.involvedParty = findInvolvedParty(system, systemToken);
		}
		
		IRolesService<?> rolesService = GuiceContext.get(IRolesService.class);
		Set<String> rolesss = rolesService.getRoles(this.involvedParty, system, systemToken);
		
		return rolesss;
	}
	
	public IInvolvedParty<?> findInvolvedParty(ISystems<?> system, UUID identityToken)
	{
		if (this.involvedParty == null)
		{
			IInvolvedPartyService<?> involvedPartyService = get(IInvolvedPartyService.class);
			if (webClientUUID != null)
			{
				this.involvedParty = involvedPartyService.findByIdentificationType(IdentificationTypeWebClientUUID, getWebClientUUID()
								.toString(),
						system, identityToken);
				
				setIdentityToken(involvedParty.getId());
			}
			setEnterprise(this.involvedParty.getEnterprise());
		}
		return this.involvedParty;
	}
	
	public IInvolvedParty<?> findInvolvedParty()
	{
		if (this.involvedParty == null)
		{
			IInvolvedPartyService<?> involvedPartyService = get(IInvolvedPartyService.class);
			if (webClientUUID != null)
			{
				this.involvedParty = involvedPartyService.findByIdentificationType(IdentificationTypeWebClientUUID.toString(), getWebClientUUID().toString());
				if (this.involvedParty == null)
				{
					return null;
				}
				setIdentityToken(involvedParty.getId());
			}
			setEnterprise(this.involvedParty.getEnterprise());
		}
		return this.involvedParty;
	}
	
	public J setInvolvedParty(IInvolvedParty<?> involvedParty)
	{
		this.involvedParty = involvedParty;
		return (J) this;
	}
}
