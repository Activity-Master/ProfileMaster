package com.guicedee.activitymaster.profiles.dto;

import com.fasterxml.jackson.annotation.*;
import com.guicedee.activitymaster.fsdm.client.types.*;
import com.guicedee.activitymaster.fsdm.client.types.structures.Party;
import com.guicedee.activitymaster.fsdm.communicator.endpoints.PartyCall;

import java.util.*;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.*;
import static com.guicedee.activitymaster.profiles.enumerations.ProfileClassifications.*;
import static com.guicedee.activitymaster.profiles.enumerations.ProfileIdentificationTypes.*;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = ANY,
                getterVisibility = NONE,
                setterVisibility = NONE)
public class ProfileServiceDTO<J extends ProfileServiceDTO<J>>
		extends UserDTO<J>
{
	public static final String IDENTITY_SESSION_NAME = "identity";
	
	@JsonProperty
	private UUID webClientUUID;
	
	private Set<String> roles = new HashSet<>();
	
	public UUID getWebClientUUID()
	{
		return webClientUUID;
	}
	
	public J setWebClientUUID(UUID webClientUUID)
	{
		this.webClientUUID = webClientUUID;
		return (J) this;
	}

	public void findInvolvedParty()
	{
			if (webClientUUID != null && getIdentityToken() == null)
			{
				IdentificationTypes identificationTypes = new IdentificationTypes()
						.setIdentificationType(new IdentificationType()
								.setName(IdentificationTypeWebClientUUID))
						.setValue(getWebClientUUID());
				AuthenticationConfiguration authenticationConfiguration = new AuthenticationConfiguration();
				Party party = new PartyCall(authenticationConfiguration).find(identificationTypes, Set.of(), UserRoles.toString());
				for (Classifications classification : party.getClassifications())
				{
					switch (classification.getClassification()
					                      .getName())
					{
						case "UserRoles":{
							roles.add(classification.getValue());
						}
					}
				}
				setIdentityToken(UUID.fromString(party.getId()));
			}
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
