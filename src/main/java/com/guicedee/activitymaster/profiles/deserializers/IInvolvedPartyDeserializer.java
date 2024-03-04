package com.guicedee.activitymaster.profiles.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.guicedee.activitymaster.fsdm.client.services.IInvolvedPartyService;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty;

import java.io.IOException;
import java.util.UUID;


public class IInvolvedPartyDeserializer
		extends JsonDeserializer<IInvolvedParty<?,?>>
{
	@Override
	public IInvolvedParty<?,?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException
	{
		String name = p.getValueAsString();
		try
		{
			IInvolvedPartyService<?> enterpriseService = com.guicedee.client.IGuiceContext.get(IInvolvedPartyService.class);
			return enterpriseService.findByID(UUID.fromString(name));
		}catch (Throwable t)
		{
			return null;
		}
	}
}
