package com.guicedee.activitymaster.profiles.deserializers;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.SerializationContext;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeSerializer
		extends ValueSerializer<LocalDateTime>
{
	public static String LocalDateTimeFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSS";

	public LocalDateTimeSerializer()
	{
	}

	@Override
	public void serialize(LocalDateTime value, JsonGenerator generator, SerializationContext provider)
	{
		generator.writeString(value.format(DateTimeFormatter.ofPattern(LocalDateTimeFormat)));
	}
}
