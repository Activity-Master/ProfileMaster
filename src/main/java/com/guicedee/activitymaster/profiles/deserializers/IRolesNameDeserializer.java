package com.guicedee.activitymaster.profiles.deserializers;

import com.guicedee.activitymaster.profiles.services.interfaces.IUserRole;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.guicedee.guicedinjection.GuiceContext;
import io.github.classgraph.ClassInfo;

import java.io.IOException;

public class IRolesNameDeserializer
		extends JsonDeserializer<IUserRole<?>>
{
	@Override
	public IUserRole<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException
	{
		String name = p.getValueAsString();
		for (ClassInfo classInfo : GuiceContext.instance()
		                                       .getScanResult()
		                                       .getClassesImplementing(IUserRole.class.getCanonicalName()))
		{
			Class clazz = classInfo.loadClass();
			for (Object enumConstant : clazz.getEnumConstants())
			{
				IUserRole<?> role = (IUserRole<?>) enumConstant;
				if(role.name().equals(name))
					return role;
			}
		}
		throw new JsonProcessingException("Unable to find IUserRole with value " + name){};
	}
}
