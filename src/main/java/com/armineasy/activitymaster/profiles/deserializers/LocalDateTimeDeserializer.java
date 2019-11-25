package com.armineasy.activitymaster.profiles.deserializers;

import com.armineasy.activitymaster.activitymaster.services.classifications.enterprise.IEnterpriseName;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.guicedee.guicedinjection.GuiceContext;
import io.github.classgraph.ClassInfo;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeDeserializer
		extends JsonDeserializer<LocalDateTime>
{
	public static String LocalDateTimeFormat ="yyyy-MM-dd'T'HH:mm:ss.SSSSSSSS";
	@Override
	public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException
	{
		String name = p.getValueAsString();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(LocalDateTimeFormat);
		LocalDateTime time = LocalDateTime.parse(name, formatter);
		return time;
	}
}
