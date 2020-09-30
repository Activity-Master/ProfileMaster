package com.guicedee.activitymaster.profiles.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.guicedee.activitymaster.core.services.classifications.enterprise.IEnterpriseName;
import com.guicedee.activitymaster.core.services.dto.IInvolvedParty;
import com.guicedee.activitymaster.core.services.dto.IInvolvedPartyIdentificationType;
import com.guicedee.activitymaster.core.services.dto.IRelationshipValue;
import com.guicedee.activitymaster.core.services.dto.ISystems;
import com.guicedee.activitymaster.profiles.ProfileSystem;
import com.guicedee.activitymaster.profiles.deserializers.IEnterpriseNameDeserializer;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.*;
import static com.guicedee.activitymaster.core.services.classifications.classification.Classifications.*;
import static com.guicedee.activitymaster.core.services.types.IdentificationTypes.*;
import static com.guicedee.guicedinjection.GuiceContext.*;
import static com.guicedee.guicedinjection.interfaces.ObjectBinderKeys.*;

@SuppressWarnings({"MissingClassJavaDoc", "unused"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = ANY,
                getterVisibility = NONE,
                setterVisibility = NONE)
public class UserDTO<J extends UserDTO<J>>
		implements Serializable
{
	private static final Logger log = Logger.getLogger(UserDTO.class.getName());
	private static final long serialVersionUID = 4346902954717740631L;
	private UUID identityToken;
	
	@JsonDeserialize(using = IEnterpriseNameDeserializer.class)
	private IEnterpriseName<?> enterprise;
	
	@SuppressWarnings("unchecked")
	public J fromIP(IInvolvedParty<?> ip)
	{
		if (identityToken == null)
		{
			UUID systemID = get(ProfileSystem.class).getSystemToken(enterprise);
			ISystems<?> profileSystem = get(ProfileSystem.class).getSystem(enterprise);
			Optional<IRelationshipValue<IInvolvedParty<?>, IInvolvedPartyIdentificationType<?>, ?>> ipId = ip.findIdentificationType(IdentificationTypeUUID, profileSystem.getEnterprise(), systemID);
			if (ipId.isPresent())
			{
				setIdentityToken(ipId.get()
				                     .getValueAsUUID());
			}
			else
			{
				if (!ip.hasIdentificationType(IdentificationTypeUUID,null, profileSystem, systemID))
				{
					UUID securityIdentityToken = UUID.randomUUID();
					ip.addOrUpdateIdentificationType(IdentificationTypeUUID, securityIdentityToken.toString(), profileSystem.getEnterprise(), systemID);
					
				}
				else
				{
					log.log(Level.WARNING, "Involved Party Does Not Exist with token?!?" + ip.getId());
				}
			}
		}
		return (J) this;
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(getIdentityToken());
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
	
	/**
	 * Returns the object presented as a JSON strong
	 *
	 * @param o An object to represent
	 * @return the string
	 */
	private String objectAsString(Object o) throws JsonProcessingException
	{
		return get(DefaultObjectMapper)
				.writeValueAsString(o);
	}
	
	public UUID getIdentityToken()
	{
		return this.identityToken;
	}
	
	public UserDTO<J> setIdentityToken(UUID identityToken)
	{
		this.identityToken = identityToken;
		return this;
	}
	
	public IEnterpriseName<?> getEnterprise()
	{
		return this.enterprise;
	}
	
	public J setEnterprise(IEnterpriseName<?> enterprise)
	{
		this.enterprise = enterprise;
		return (J) this;
	}
}
