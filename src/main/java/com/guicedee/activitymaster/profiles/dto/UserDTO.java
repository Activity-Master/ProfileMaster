package com.guicedee.activitymaster.profiles.dto;

import com.guicedee.activitymaster.core.services.classifications.enterprise.IEnterpriseName;
import com.guicedee.activitymaster.core.services.dto.IInvolvedParty;
import com.guicedee.activitymaster.core.services.dto.IInvolvedPartyIdentificationType;
import com.guicedee.activitymaster.core.services.dto.IRelationshipValue;
import com.guicedee.activitymaster.core.services.dto.ISystems;
import com.guicedee.activitymaster.profiles.ProfileSystem;
import com.guicedee.activitymaster.profiles.deserializers.IEnterpriseNameDeserializer;
import com.guicedee.activitymaster.profiles.deserializers.IRolesNameDeserializer;
import com.guicedee.activitymaster.profiles.deserializers.LocalDateTimeDeserializer;
import com.guicedee.activitymaster.profiles.deserializers.LocalDateTimeSerializer;
import com.guicedee.activitymaster.profiles.services.interfaces.IUserRole;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.guicedee.guicedinjection.GuiceContext;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.*;
import static com.guicedee.activitymaster.core.services.types.IdentificationTypes.*;
import static com.guicedee.guicedinjection.interfaces.ObjectBinderKeys.*;


@SuppressWarnings({"MissingClassJavaDoc", "unused"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = ANY,getterVisibility = NONE,setterVisibility = NONE)
public class UserDTO<J extends UserDTO<J>>
		implements Serializable
{
	private static final Logger log = Logger.getLogger(UserDTO.class.getName());
	private UUID identityToken;
	@JsonIgnore
	@JsonDeserialize(using = IRolesNameDeserializer.class)
	private Set<IUserRole<?>> roles;
	@JsonDeserialize(using = IEnterpriseNameDeserializer.class)
	private IEnterpriseName<?> enterprise;
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	private LocalDateTime lastActionTime;
	@JsonIgnore
	private boolean loggedIn;

	@SuppressWarnings("unchecked")
	public J fromIP(IInvolvedParty<?> ip)
	{
		if (identityToken == null)
		{
			UUID systemID = ProfileSystem.getSystemTokens()
			                             .get(ip.getEnterpriseID());
			ISystems<?> profileSystem = ProfileSystem.getNewSystem()
			                                      .get(ip.getEnterpriseID());
			Optional<IRelationshipValue<IInvolvedParty<?>, IInvolvedPartyIdentificationType<?>,?>> ipId = ip.find(IdentificationTypeUUID, profileSystem, systemID);
			if (ipId.isPresent())
			{
				setIdentityToken(ipId.get().getValueAsUUID());
			}
			else
			{
				if (!ip.has(IdentificationTypeUUID, profileSystem, systemID))
				{
					UUID securityIdentityToken = UUID.randomUUID();
					ip.addOrUpdate(IdentificationTypeUUID, securityIdentityToken.toString(), profileSystem, systemID);

				}
				else
				{
					log.log(Level.WARNING, "Involved Party Does Not Exist with token?!?" + ip.getId());
				}
			}
		}
		return (J) this;
	}

	/**
	 * Returns the object presented as a JSON strong
	 *
	 * @param o
	 * 		An object to represent
	 *
	 * @return the string
	 */
	private String objectAsString(Object o) throws JsonProcessingException
	{
		return GuiceContext.get(DefaultObjectMapper)
		                   .writeValueAsString(o);
	}

	@Override
	public String toString()
	{
		try
		{
			return objectAsString(this);
		}
		catch (JsonProcessingException e)
		{
			log.log(Level.SEVERE, "Can't do the string", e);
		}
		return "Can't Convert";
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}
		UserDTO<?> userDTO = (UserDTO<?>) o;
		return Objects.equals(getIdentityToken(), userDTO.getIdentityToken());
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(getIdentityToken());
	}

	public LocalDateTime getLastActionTime()
	{
		return lastActionTime;
	}

	public J setLastActionTime(LocalDateTime lastActionTime)
	{
		this.lastActionTime = lastActionTime;
		return (J)this;
	}

	public UUID getIdentityToken()
	{
		return this.identityToken;
	}

	public Set<IUserRole<?>> getRoles()
	{
		return this.roles;
	}

	public IEnterpriseName<?> getEnterprise()
	{
		return this.enterprise;
	}

	public UserDTO<J> setIdentityToken(UUID identityToken)
	{
		this.identityToken = identityToken;
		setLastActionTime(LocalDateTime.now());
		return this;
	}

	public J setRoles(Set<IUserRole<?>> roles)
	{
		this.roles = roles;
		setLastActionTime(LocalDateTime.now());
		return (J)this;
	}

	public J setEnterprise(IEnterpriseName<?> enterprise)
	{
		this.enterprise = enterprise;
		setLastActionTime(LocalDateTime.now());
		return (J)this;
	}

	public boolean isLoggedIn()
	{
		return loggedIn;
	}

	public UserDTO<J> setLoggedIn(boolean loggedIn)
	{
		this.loggedIn = loggedIn;
		setLastActionTime(LocalDateTime.now());
		return this;
	}
}
