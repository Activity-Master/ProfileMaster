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
import io.smallrye.mutiny.Uni;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.*;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.*;
import static com.guicedee.activitymaster.profiles.enumerations.ProfileIdentificationTypes.*;
import static com.guicedee.activitymaster.profiles.services.interfaces.IProfileService.*;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = ANY,
                getterVisibility = NONE,
                setterVisibility = NONE)
public class ProfileServiceDTO<J extends ProfileServiceDTO<J>>
		extends UserDTO<J>
{
	private static final Logger log = LogManager.getLogger(ProfileServiceDTO.class);
	public static final String IDENTITY_SESSION_NAME = "identity";
	
	@JsonProperty
	private UUID webClientUUID;
	
	@JsonIgnore
	private transient IInvolvedParty<?, ?> involvedParty;
	
	
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
	
	/**
	 * Find roles for the involved party
	 * 
	 * Note: This method is still synchronous for backward compatibility,
	 * but internally uses reactive programming with await().atMost()
	 */
	public Set<String> findRoles()
	{
		if (profileSystem == null)
		{
			com.guicedee.client.IGuiceContext.instance().inject()
			            .injectMembers(this);
		}
		ISystems<?, ?> system = profileSystem.getSystem(session, getEnterprise());
		UUID systemToken = profileSystem.getSystemToken(session, getEnterprise());
		
		if (this.involvedParty == null)
		{
			this.involvedParty = findInvolvedParty(system, systemToken);
		}
		
		// Use reactive getRoles method with await().atMost() for backward compatibility
		return rolesService.getRoles(this.involvedParty, system, systemToken)
			.await().atMost(Duration.ofMinutes(1));
	}
	
	/**
	 * Find roles for the involved party (reactive version)
	 */
	public Uni<Set<String>> findRolesReactive()
	{
		if (profileSystem == null)
		{
			com.guicedee.client.IGuiceContext.instance().inject()
			            .injectMembers(this);
		}
		ISystems<?, ?> system = profileSystem.getSystem(session, getEnterprise());
		UUID systemToken = profileSystem.getSystemToken(session, getEnterprise());
		
		if (this.involvedParty == null)
		{
			// Use reactive findInvolvedPartyReactive method
			return findInvolvedPartyReactive(system, systemToken)
				.chain(ip -> {
					this.involvedParty = ip;
					return rolesService.getRoles(this.involvedParty, system, systemToken);
				})
				.onFailure().invoke(error -> log.error("Error finding roles: {}", error.getMessage(), error))
				.onFailure().recoverWithItem(() -> new TreeSet<>());
		}
		
		return rolesService.getRoles(this.involvedParty, system, systemToken)
			.onFailure().invoke(error -> log.error("Error finding roles: {}", error.getMessage(), error))
			.onFailure().recoverWithItem(() -> new TreeSet<>());
	}
	
	/**
	 * Find involved party
	 * 
	 * Note: This method is still synchronous for backward compatibility,
	 * but internally uses reactive programming with await().atMost()
	 */
	public IInvolvedParty<?, ?> findInvolvedParty(ISystems<?, ?> system, UUID identityToken)
	{
		if (involvedPartyService == null)
		{
			com.guicedee.client.IGuiceContext.instance().inject()
			            .injectMembers(this);
		}
		if (this.involvedParty == null)
		{
			if (webClientUUID != null)
			{
				// Use reactive findInvolvedPartyReactive method with await().atMost() for backward compatibility
				this.involvedParty = findInvolvedPartyReactive(system, identityToken)
					.await().atMost(Duration.ofMinutes(1));
				
				if (this.involvedParty != null) {
					setIdentityToken(involvedParty.getId());
				}
			}
		}
		return this.involvedParty;
	}
	
	/**
	 * Find involved party (reactive version)
	 */
	@SuppressWarnings("unchecked")
	public Uni<IInvolvedParty<?, ?>> findInvolvedPartyReactive(ISystems<?, ?> system, UUID identityToken)
	{
		if (involvedPartyService == null)
		{
			com.guicedee.client.IGuiceContext.instance().inject()
			            .injectMembers(this);
		}
		if (this.involvedParty != null)
		{
			return Uni.createFrom().item(this.involvedParty);
		}
		
		if (webClientUUID != null)
		{
			// Cast the result to the correct type
			return (Uni<IInvolvedParty<?, ?>>) (Uni<?>) involvedPartyService.get()
				.builder()
				.findByIdentificationType(IdentificationTypeWebClientUUID.toString(), getWebClientUUID().toString(),
						system, identityToken)
				.get()
				.onItem().invoke(ip -> {
					if (ip != null) {
						setIdentityToken(((IInvolvedParty<?, ?>) ip).getId());
					}
				})
				.onFailure().invoke(error -> log.error("Error finding involved party: {}", error.getMessage(), error))
				.onFailure().recoverWithItem(() -> null);
		}
		
		return Uni.createFrom().nullItem();
	}
	
	/**
	 * Find involved party
	 * 
	 * Note: This method is still synchronous for backward compatibility,
	 * but internally uses reactive programming with await().atMost()
	 */
	public IInvolvedParty<?, ?> findInvolvedParty()
	{
		if (involvedPartyService == null)
		{
			com.guicedee.client.IGuiceContext.instance().inject()
			            .injectMembers(this);
		}
		if (this.involvedParty == null)
		{
			if (webClientUUID != null)
			{
				try
				{
					// Use reactive findInvolvedPartyReactive method with await().atMost() for backward compatibility
					this.involvedParty = findInvolvedPartyReactive(system, identityToken)
						.await().atMost(Duration.ofMinutes(1));
				}
				catch (ActivityMasterException e)
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
	
	/**
	 * Find involved party (reactive version)
	 */
	public Uni<IInvolvedParty<?, ?>> findInvolvedPartyReactive()
	{
		if (involvedPartyService == null)
		{
			com.guicedee.client.IGuiceContext.instance().inject()
			            .injectMembers(this);
		}
		if (this.involvedParty != null)
		{
			return Uni.createFrom().item(this.involvedParty);
		}
		
		if (webClientUUID != null)
		{
			return findInvolvedPartyReactive(system, identityToken);
		}
		
		return Uni.createFrom().nullItem();
	}
	
	public J setInvolvedParty(IInvolvedParty<?, ?> involvedParty)
	{
		this.involvedParty = involvedParty;
		return (J) this;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof ProfileServiceDTO))
		{
			return false;
		}
		ProfileServiceDTO<?> that = (ProfileServiceDTO<?>) o;
		return Objects.equals(getWebClientUUID(), that.getWebClientUUID());
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(getWebClientUUID());
	}
}