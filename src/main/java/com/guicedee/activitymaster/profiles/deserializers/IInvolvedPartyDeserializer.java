package com.guicedee.activitymaster.profiles.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.guicedee.activitymaster.core.services.classifications.enterprise.IEnterpriseName;
import com.guicedee.activitymaster.core.services.dto.IInvolvedParty;
import com.guicedee.activitymaster.core.services.dto.ISystems;
import com.guicedee.activitymaster.core.services.system.IEnterpriseService;
import com.guicedee.activitymaster.core.services.system.IInvolvedPartyService;

import java.io.IOException;
import java.util.UUID;

import static com.guicedee.guicedinjection.GuiceContext.*;

public class IInvolvedPartyDeserializer
		extends JsonDeserializer<IInvolvedParty<?>>
{
	@Override
	public IInvolvedParty<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException
	{
		String name = p.getValueAsString();
		try
		{
			IInvolvedPartyService<?> enterpriseService = get(IInvolvedPartyService.class);
			return enterpriseService.findByID(UUID.fromString(name));
		}catch (Throwable t)
		{
			return null;
		}
	}
}
