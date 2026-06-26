package com.guicedee.activitymaster.profiles.deserializers;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import com.guicedee.activitymaster.fsdm.client.services.IInvolvedPartyService;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.client.IGuiceContext;
import org.hibernate.reactive.mutiny.Mutiny;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;


public class IInvolvedPartyDeserializer
        extends ValueDeserializer<IInvolvedParty<?, ?>>
{
    @Override
    public IInvolvedParty<?, ?> deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException
    {
        String name = p.getValueAsString();
        if(name == null)
        {
            return null;
        }
        var factory = IGuiceContext.get(Mutiny.SessionFactory.class);
        return factory.openSession()
                       .chain(session -> {
                           IInvolvedPartyService<?> enterpriseService = com.guicedee.client.IGuiceContext.get(IInvolvedPartyService.class);
                           return enterpriseService.findByID(session, UUID.fromString(name))
                                   .eventually(session::close);
                       })
                       .await()
                       .atMost(Duration.ofSeconds(50));
    }
}
