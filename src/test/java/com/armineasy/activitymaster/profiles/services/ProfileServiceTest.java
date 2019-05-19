package com.armineasy.activitymaster.profiles.services;

import com.armineasy.activitymaster.activity.configs.DefaultTestConfig;
import com.armineasy.activitymaster.activitymaster.ActivityMasterConfiguration;
import com.armineasy.activitymaster.activitymaster.db.entities.enterprise.Enterprise;
import com.armineasy.activitymaster.activitymaster.services.system.IEnterpriseService;
import com.armineasy.activitymaster.profiles.ProfileSystem;
import com.armineasy.activitymaster.profiles.dto.GuestDTO;
import com.google.common.base.Stopwatch;
import com.jwebmp.guicedinjection.GuiceContext;
import com.jwebmp.guicedinjection.interfaces.JobService;
import lombok.extern.java.Log;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import static com.armineasy.activitymaster.activitymaster.DefaultEnterprise.*;
import static com.jwebmp.guicedinjection.GuiceContext.*;
import static java.util.concurrent.TimeUnit.*;

@ExtendWith(DefaultTestConfig.class)
@Log
class ProfileServiceTest
{

	@org.junit.jupiter.api.Test
	public void testCreateNewGuest()
	{
		ProfileService ps = get(ProfileService.class);
		Stopwatch stopwatch = Stopwatch.createStarted();
		log.info("Started creating guest");
		Enterprise enterprise = get(IEnterpriseService.class)
				                        .getEnterprise(get(ActivityMasterConfiguration.class).getEnterpriseName());


		GuestDTO<?> newGuest = new GuestDTO<>().setWebClientUUID(UUID.randomUUID());
		//newGuest.setReadableUserAgent()
		newGuest = ps.loginVisitor(newGuest, TestEnterprise, ProfileSystem.getSystemTokens()
		                                                                  .get(enterprise));
		log.info("Created New Guest! Session Returned - " + stopwatch.stop()
		                                                             .elapsed(MILLISECONDS));
	}

	@org.junit.jupiter.api.Test
	public void testCreate100NewGuests()
	{
		defaultWaitTime = 1;
		defaultWaitUnit = MINUTES;
		ExecutorService service = null;
		Enterprise enterprise = get(IEnterpriseService.class).getEnterprise(TestEnterprise);
		for (int i = 0; i < 100; i++)
		{
			NewGuestThread thread = GuiceContext.get(NewGuestThread.class);

		}
		JobService.getInstance()
		          .waitForJob("TestCreate100NewGuests", 5, MINUTES);
	}

	@org.junit.jupiter.api.Test
	public void testCreate1000NewGuests()
	{
		ExecutorService service = null;
		Enterprise enterprise = get(IEnterpriseService.class).getEnterprise(TestEnterprise);
		for (int i = 0; i < 1000; i++)
		{
			NewGuestThread thread = GuiceContext.get(NewGuestThread.class);

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
		Enterprise enterprise = get(IEnterpriseService.class).getEnterprise(TestEnterprise);
		for (int i = 0; i < 10000; i++)
		{
			NewGuestThread thread = GuiceContext.get(NewGuestThread.class);
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