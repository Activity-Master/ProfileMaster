package com.guicedee.activitymaster.profiles.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.guicedee.activitymaster.core.services.classifications.enterprise.IEnterpriseName;
import com.guicedee.activitymaster.core.services.system.IEnterpriseService;

import java.io.IOException;
import java.util.UUID;

import static com.guicedee.guicedinjection.GuiceContext.*;

public class IEnterpriseNameDeserializer extends JsonDeserializer<IEnterpriseName<?>>
{
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
