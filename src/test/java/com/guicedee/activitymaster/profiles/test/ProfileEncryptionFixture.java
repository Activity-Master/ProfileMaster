package com.guicedee.activitymaster.profiles.test;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/** Ephemeral AES configuration; production key configuration is never used by profile tests. */
final class ProfileEncryptionFixture implements AutoCloseable
{
	private final Map<String, String> original = new LinkedHashMap<>();
	private static final String TEST_KEY = randomKey();

	ProfileEncryptionFixture()
	{
		set("activitymaster.encryption.mode", "aes-gcm");
		set("activitymaster.encryption.key-id", "profiletest");
		set("activitymaster.encryption.key.profiletest", TEST_KEY);
		set("activitymaster.encryption.read-key-ids", "");
	}

	private static String randomKey()
	{
		byte[] key = new byte[32];
		new SecureRandom().nextBytes(key);
		return Base64.getEncoder().encodeToString(key);
	}

	private void set(String property, String value)
	{
		original.put(property, System.getProperty(property));
		System.setProperty(property, value);
	}

	@Override
	public void close()
	{
		original.forEach((property, value) -> {
			if (value == null) System.clearProperty(property);
			else System.setProperty(property, value);
		});
	}
}
