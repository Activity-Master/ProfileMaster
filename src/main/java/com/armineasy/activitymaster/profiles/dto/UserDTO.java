package com.armineasy.activitymaster.profiles.dto;

import com.armineasy.activitymaster.activitymaster.db.entities.address.Address;
import com.armineasy.activitymaster.activitymaster.db.entities.enterprise.Enterprise;
import com.armineasy.activitymaster.activitymaster.db.entities.involvedparty.InvolvedParty;
import com.armineasy.activitymaster.activitymaster.db.entities.involvedparty.InvolvedPartyIdentificationType;
import com.armineasy.activitymaster.activitymaster.db.entities.involvedparty.InvolvedPartyXInvolvedPartyIdentificationType;
import com.armineasy.activitymaster.activitymaster.db.entities.systems.Systems;
import com.armineasy.activitymaster.activitymaster.implementations.AddressService;
import com.armineasy.activitymaster.activitymaster.implementations.InvolvedPartyService;
import com.armineasy.activitymaster.activitymaster.implementations.ResourceItemService;
import com.armineasy.activitymaster.activitymaster.implementations.SystemsService;
import com.armineasy.activitymaster.activitymaster.services.types.IdentificationTypes;
import com.armineasy.activitymaster.profiles.ProfileSystem;
import com.jwebmp.guicedinjection.GuiceContext;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;
import net.sf.uadetector.ReadableUserAgent;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

import static com.armineasy.activitymaster.activitymaster.services.types.IdentificationTypes.*;
import static com.armineasy.activitymaster.profiles.enumerations.ProfileIdentificationTypes.*;

@Data
@Accessors(chain = true)
@Log
public class UserDTO<J extends UserDTO<J>>
{
	private UUID identityToken;

	@SuppressWarnings("unchecked")
	public J fromIP(InvolvedParty ip)
	{
		if (identityToken == null)
		{
			UUID systemID = ProfileSystem.getSystemTokens()
			                             .get(ip.getEnterpriseID());
			Systems profileSystem = ProfileSystem.getNewSystem()
			                                     .get(ip.getEnterpriseID());
			Optional<InvolvedPartyXInvolvedPartyIdentificationType> ipId = ip.findIdentificationType(IdentificationTypeUUID,profileSystem, systemID);
			if (ipId.isPresent())
			{
				setIdentityToken(UUID.fromString(ipId.get()
				                                     .getValue()));
			}
			else
			{
				if (ip.hasIdentificationType(IdentificationTypeWebClientUUID,profileSystem, systemID))
				{
					UUID securityIdentityToken = UUID.randomUUID();
					ip.addIdentificationType(IdentificationTypeUUID,profileSystem, securityIdentityToken.toString(), systemID);

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
