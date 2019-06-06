package com.armineasy.activitymaster.profiles.dto;

import com.armineasy.activitymaster.activitymaster.db.entities.involvedparty.InvolvedParty;
import com.armineasy.activitymaster.activitymaster.db.entities.involvedparty.InvolvedPartyXInvolvedPartyIdentificationType;
import com.armineasy.activitymaster.activitymaster.services.dto.IEnterprise;
import com.armineasy.activitymaster.activitymaster.services.dto.ISystems;
import com.armineasy.activitymaster.profiles.ProfileSystem;
import com.armineasy.activitymaster.profiles.services.interfaces.IRolesService;
import com.armineasy.activitymaster.profiles.services.interfaces.IUserRole;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwebmp.guicedinjection.GuiceContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import static com.armineasy.activitymaster.activitymaster.services.types.IdentificationTypes.*;

@Getter
@Setter
@Accessors(chain = true)
@Log
@EqualsAndHashCode(of = "identityToken")
@SuppressWarnings({"MissingClassJavaDoc", "unused"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDTO<J extends UserDTO<J>> implements Serializable
{
	private UUID identityToken;
	private Set<IUserRole<?>> roles;
	private IEnterprise<?> enterprise;

	@SuppressWarnings("unchecked")
	public J fromIP(InvolvedParty ip)
	{
		if (identityToken == null)
		{
			UUID systemID = ProfileSystem.getSystemTokens()
			                             .get(ip.getEnterpriseID());
			ISystems profileSystem = ProfileSystem.getNewSystem()
			                                      .get(ip.getEnterpriseID());
			Optional<InvolvedPartyXInvolvedPartyIdentificationType> ipId = ip.findIdentificationType(IdentificationTypeUUID, profileSystem, systemID);
			if (ipId.isPresent())
			{
				setIdentityToken(UUID.fromString(ipId.get()
				                                     .getValue()));
			}
			else
			{
				if (ip.hasIdentificationType(IdentificationTypeUUID, profileSystem, systemID))
				{
					UUID securityIdentityToken = UUID.randomUUID();
					ip.addIdentificationType(IdentificationTypeUUID, profileSystem, securityIdentityToken.toString(), systemID);

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
	public String objectAsString(Object o) throws JsonProcessingException
	{
		return GuiceContext.get(ObjectMapper.class)
		                   .writeValueAsString(o);
	}

	public Set<IUserRole<?>> findRoles()
	{
		if (roles == null)
		{
			IRolesService rolesService = GuiceContext.get(IRolesService.class);
			List<IUserRole<?>> rolesss = rolesService.getRoles(this, ProfileSystem.getNewSystem()
			                                                                      .get(enterprise), ProfileSystem.getSystemTokens()
			                                                                                                     .get(enterprise));
		}
		return roles;
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
}
