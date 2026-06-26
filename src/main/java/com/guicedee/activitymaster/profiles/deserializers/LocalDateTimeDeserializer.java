package com.guicedee.activitymaster.profiles.deserializers;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeDeserializer
		extends ValueDeserializer<LocalDateTime>
{
	public static String LocalDateTimeFormat ="yyyy-MM-dd'T'HH:mm:ss.SSSSSSSS";
	@Override
	public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException
	{
		String name = p.getValueAsString();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(LocalDateTimeFormat);
		LocalDateTime time = LocalDateTime.parse(name, formatter);
		return time;
	}
}
