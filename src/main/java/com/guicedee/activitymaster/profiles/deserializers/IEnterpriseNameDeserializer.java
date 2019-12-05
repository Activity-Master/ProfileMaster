package com.guicedee.activitymaster.profiles.deserializers;

import com.guicedee.activitymaster.core.services.classifications.enterprise.IEnterpriseName;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.guicedee.guicedinjection.GuiceContext;
import io.github.classgraph.ClassInfo;

import java.io.IOException;

public class IEnterpriseNameDeserializer extends JsonDeserializer<IEnterpriseName<?>>
{
	@Override
	public IEnterpriseName<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException
	{
		String name = p.getValueAsString();
		for (ClassInfo classInfo : GuiceContext.instance()
		                                       .getScanResult()
		                                       .getClassesImplementing(IEnterpriseName.class.getCanonicalName()))
		{
			Class clazz = classInfo.loadClass();
			for (Object enumConstant : clazz.getEnumConstants())
			{
				IEnterpriseName<?> role = (IEnterpriseName<?>) enumConstant;
				if(role.name().equals(name))
					return role;
			}
		}
		throw new JsonProcessingException("Unable to find IEnterpriseName with value " + name){};
	}
}
