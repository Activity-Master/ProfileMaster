package com.guicedee.activitymaster.profiles.deserializers;

import com.guicedee.activitymaster.core.services.IActivityMasterSystem;
import com.guicedee.activitymaster.core.services.classifications.classification.Classifications;
import com.guicedee.activitymaster.core.services.classifications.enterprise.IEnterpriseName;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.guicedee.activitymaster.core.services.dto.IEnterprise;
import com.guicedee.activitymaster.core.services.dto.IInvolvedParty;
import com.guicedee.activitymaster.core.services.dto.ISystems;
import com.guicedee.activitymaster.core.services.system.IEnterpriseService;
import com.guicedee.activitymaster.core.services.system.IInvolvedPartyService;
import com.guicedee.activitymaster.core.services.system.ISystemsService;
import com.guicedee.activitymaster.core.services.types.NameTypes;
import com.guicedee.activitymaster.profiles.ProfileSystem;
import com.guicedee.activitymaster.profiles.enumerations.ProfileIdentificationTypes;
import com.guicedee.guicedinjection.GuiceContext;
import io.github.classgraph.ClassInfo;

import java.io.IOException;
import java.util.UUID;

import static com.guicedee.guicedinjection.GuiceContext.*;

public class IEnterpriseNameDeserializer extends JsonDeserializer<IEnterpriseName<?>>
{
	public static ISystems<?> system;
	public static UUID systemToken;
	
	@Override
	public IEnterpriseName<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException
	{
		String name = p.getValueAsString();
		try
		{
			IEnterpriseService enterpriseService = get(IEnterpriseService.class);
			return enterpriseService.getIEnterpriseFromID(UUID.fromString(name));
		}catch (Throwable t)
		{
			return null;
		}
	}
}
