package com.guicedee.activitymaster.profiles.services;

import com.google.common.base.Stopwatch;
import com.guicedee.activitymaster.core.ActivityMasterConfiguration;
import com.guicedee.activitymaster.core.services.dto.IEnterprise;
import com.guicedee.activitymaster.core.services.dto.ISystems;
import com.guicedee.activitymaster.core.services.system.IEnterpriseService;
import com.guicedee.activitymaster.profiles.ProfileService;
import com.guicedee.activitymaster.profiles.ProfileSystem;
import com.guicedee.activitymaster.profiles.webdto.UserLoginDTO;
import com.guicedee.guicedinjection.interfaces.JobService;
import lombok.extern.java.Log;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import static com.guicedee.activitymaster.core.DefaultEnterprise.*;
import static com.guicedee.guicedinjection.GuiceContext.*;
import static java.util.concurrent.TimeUnit.*;

//@ExtendWith(DefaultTestConfig.class)
@Log
class ProfileServiceTest
{

	@org.junit.jupiter.api.Test
	public void testCreateNewGuest()
	{
		ProfileService ps = get(ProfileService.class);
		Stopwatch stopwatch = Stopwatch.createStarted();
		log.info("Started creating guest");
		IEnterprise<?> enterprise = get(IEnterpriseService.class)
				                            .getEnterprise(get(ActivityMasterConfiguration.class).getEnterpriseName());

		UserLoginDTO<?> newGuest = new UserLoginDTO<>().setWebClientUUID(UUID.randomUUID());
		//newGuest.setReadableUserAgent()
		ISystems<?> profileSystem = get(ProfileSystem.class)
				                            .getSystem(enterprise);
		UUID profileSystemUUID = get(ProfileSystem.class)
				                         .getSystemToken(enterprise);
		newGuest = (UserLoginDTO<?>) ps.loginVisitor(newGuest, TestEnterprise, profileSystemUUID);
		log.info("Created New Guest! Session Returned - " + stopwatch.stop()
		                                                             .elapsed(MILLISECONDS));
	}

	@org.junit.jupiter.api.Test
	public void testCreate100NewGuests()
	{
		defaultWaitTime = 1;
		defaultWaitUnit = MINUTES;
		ExecutorService service = null;
		IEnterprise<?> enterprise = get(IEnterpriseService.class).getEnterprise(TestEnterprise);
		for (int i = 0; i < 100; i++)
		{
			NewGuestThread thread = get(NewGuestThread.class);

		}
		JobService.getInstance()
		          .waitForJob("TestCreate100NewGuests", 5, MINUTES);
	}

	@org.junit.jupiter.api.Test
	public void testCreate1000NewGuests()
	{
		ExecutorService service = null;
		IEnterprise<?> enterprise = get(IEnterpriseService.class).getEnterprise(TestEnterprise);
		for (int i = 0; i < 1000; i++)
		{
			NewGuestThread thread = get(NewGuestThread.class);

			service = JobService.getInstance()
			                    .addJob("TestCreate100NewGuests", (Callable) thread);
		}
		JobService.getInstance()
		          .waitForJob("TestCreate100NewGuests", 5, MINUTES);
	}

	@org.junit.jupiter.api.Test
	public void testCreate10000NewGuests()
	{
		ExecutorService service = null;
		IEnterprise<?> enterprise = get(IEnterpriseService.class).getEnterprise(TestEnterprise);
		for (int i = 0; i < 10000; i++)
		{
			NewGuestThread thread = get(NewGuestThread.class);
			service = JobService.getInstance()
			                    .addJob("TestCreate100NewGuests", (Callable<?>) thread);
		}
		JobService.getInstance()
		          .waitForJob("TestCreate100NewGuests", 5, MINUTES);
	}

	@org.junit.jupiter.api.Test
	void findByWebClientKey()
	{

	}
}
