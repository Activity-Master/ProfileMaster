package com.armineasy.activitymaster.profiles.dto;

import com.armineasy.activitymaster.activitymaster.db.entities.involvedparty.InvolvedParty;
import com.armineasy.activitymaster.profiles.ProfileSystem;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.UUID;

import static com.armineasy.activitymaster.profiles.enumerations.ProfileIdentificationTypes.*;

@Getter
@Setter
@Accessors(chain = true)
public class ProfileServiceDTO<J extends ProfileServiceDTO<J>>
		extends UserDTO<J>
{
	private UUID webClientUUID;

	@SuppressWarnings("unchecked")
	@Override
	public J fromIP(InvolvedParty ip)
	{
		super.fromIP(ip);
		if (ip.hasIdentificationType(IdentificationTypeWebClientUUID,
		                             ProfileSystem.getNewSystem()
		                                          .get(ip.getEnterpriseID()),
		                             ProfileSystem.getSystemTokens()
		                                          .get(ip.getEnterpriseID())
		                            ))
		{
			webClientUUID = UUID.fromString(ip.findIdentificationType(IdentificationTypeWebClientUUID,
			                                                          ProfileSystem.getNewSystem()
			                                                                       .get(ip.getEnterpriseID()),
			                                                          ProfileSystem.getSystemTokens()
			                                                                       .get(ip.getEnterpriseID()))
			                                  .orElseThrow()
			                                  .getValue());
		}
		return (J) this;
	}
}
