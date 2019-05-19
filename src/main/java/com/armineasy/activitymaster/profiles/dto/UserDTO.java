package com.armineasy.activitymaster.profiles.dto;

import com.armineasy.activitymaster.activitymaster.db.entities.involvedparty.InvolvedParty;
import com.armineasy.activitymaster.activitymaster.db.entities.involvedparty.InvolvedPartyXInvolvedPartyIdentificationType;
import com.armineasy.activitymaster.activitymaster.db.entities.systems.Systems;
import com.armineasy.activitymaster.profiles.ProfileSystem;
import com.armineasy.activitymaster.profiles.services.interfaces.IUserRole;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import static com.armineasy.activitymaster.activitymaster.services.types.IdentificationTypes.*;

@Data
@Accessors(chain = true)
@Log
public class UserDTO<J extends UserDTO<J>>
{
	private UUID identityToken;
	private Set<IUserRole<?>> roles;

	@SuppressWarnings("unchecked")
	public J fromIP(InvolvedParty ip)
	{
		if (identityToken == null)
		{
			UUID systemID = ProfileSystem.getSystemTokens()
			                             .get(ip.getEnterpriseID());
			Systems profileSystem = ProfileSystem.getNewSystem()
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


}
