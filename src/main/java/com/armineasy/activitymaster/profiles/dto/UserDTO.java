package com.armineasy.activitymaster.profiles.dto;

import com.armineasy.activitymaster.activitymaster.services.dto.IEnterprise;
import com.armineasy.activitymaster.activitymaster.services.dto.IInvolvedParty;
import com.armineasy.activitymaster.activitymaster.services.dto.IRelationshipValue;
import com.armineasy.activitymaster.activitymaster.services.dto.ISystems;
import com.armineasy.activitymaster.profiles.ProfileSystem;
import com.armineasy.activitymaster.profiles.services.interfaces.IUserRole;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwebmp.guicedinjection.GuiceContext;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.armineasy.activitymaster.activitymaster.services.types.IdentificationTypes.*;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.*;

@Accessors(chain = true)
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
	private Set<IUserRole<?>> roles;
	@JsonIgnore
	private IEnterprise<?> enterprise;

	@SuppressWarnings("unchecked")
	public J fromIP(IInvolvedParty<?> ip)
	{
		if (identityToken == null)
		{
			UUID systemID = ProfileSystem.getSystemTokens()
			                             .get(ip.getEnterpriseID());
			ISystems profileSystem = ProfileSystem.getNewSystem()
			                                      .get(ip.getEnterpriseID());
			Optional<IRelationshipValue<?>> ipId = ip.find(IdentificationTypeUUID, profileSystem, systemID);
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
		return GuiceContext.get(ObjectMapper.class)
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

	public boolean equals(final Object o)
	{
		if (o == this)
		{
			return true;
		}
		if (!(o instanceof UserDTO))
		{
			return false;
		}
		final UserDTO<?> other = (UserDTO<?>) o;
		if (!other.canEqual((Object) this))
		{
			return false;
		}
		final Object this$identityToken = this.identityToken;
		final Object other$identityToken = other.identityToken;
		if (this$identityToken == null ? other$identityToken != null : !this$identityToken.equals(other$identityToken))
		{
			return false;
		}
		return true;
	}

	protected boolean canEqual(final Object other)
	{
		return other instanceof UserDTO;
	}

	public int hashCode()
	{
		final int PRIME = 59;
		int result = 1;
		final Object $identityToken = this.identityToken;
		result = result * PRIME + ($identityToken == null ? 43 : $identityToken.hashCode());
		return result;
	}

	public UUID getIdentityToken()
	{
		return this.identityToken;
	}

	public Set<IUserRole<?>> getRoles()
	{
		return this.roles;
	}

	public IEnterprise<?> getEnterprise()
	{
		return this.enterprise;
	}

	public UserDTO<J> setIdentityToken(UUID identityToken)
	{
		this.identityToken = identityToken;
		return this;
	}

	public UserDTO<J> setRoles(Set<IUserRole<?>> roles)
	{
		this.roles = roles;
		return this;
	}

	public UserDTO<J> setEnterprise(IEnterprise<?> enterprise)
	{
		this.enterprise = enterprise;
		return this;
	}
}
