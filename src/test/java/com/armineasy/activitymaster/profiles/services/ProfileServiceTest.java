package com.armineasy.activitymaster.profiles.services;

import com.armineasy.activitymaster.activity.configs.DefaultTestConfig;
import com.armineasy.activitymaster.activitymaster.ActivityMasterConfiguration;
import com.armineasy.activitymaster.activitymaster.db.entities.enterprise.Enterprise;
import com.armineasy.activitymaster.activitymaster.db.entities.involvedparty.InvolvedParty;
import com.armineasy.activitymaster.activitymaster.implementations.SystemsService;
import com.armineasy.activitymaster.activitymaster.services.classifications.enterprise.IEnterpriseName;
import com.armineasy.activitymaster.activitymaster.services.system.IEnterpriseService;
import com.armineasy.activitymaster.profiles.ProfileSystem;
import com.armineasy.activitymaster.profiles.dto.GuestDTO;
import com.jwebmp.guicedinjection.GuiceContext;
import com.jwebmp.guicedinjection.interfaces.JobService;
import com.jwebmp.guicedservlets.GuicedServletKeys;
import lombok.extern.java.Log;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

		Enterprise enterprise = get(IEnterpriseService.class)
				                        .getEnterprise(get(ActivityMasterConfiguration.class).getEnterpriseName());


		GuestDTO<?> newGuest = new GuestDTO<>().setWebClientUUID(UUID.randomUUID());
		//newGuest.setReadableUserAgent()
		newGuest = ps.loginVisitor(newGuest, TestEnterprise, ProfileSystem.getSystemTokens()
		                                                   .get(enterprise));
		JobService.getInstance()
		          .waitForJob("DefaultSecurityPersister");

		HttpServletRequest request = GuiceContext.get(GuicedServletKeys.getHttpServletRequestKey());
		ProfileService profileService = GuiceContext.get(ProfileService.class);
		InvolvedParty ip = profileService.configureFromHTTPServletRequest(newGuest, request, enterprise);

		System.out.println(request);
	}

	@org.junit.jupiter.api.Test
	public void testCreate100NewGuests()
	{
		defaultWaitTime = 1;
		defaultWaitUnit = MINUTES;
		JobService.maxQueueCount = 20;
		ExecutorService service = null;
		Enterprise enterprise = get(IEnterpriseService.class).getEnterprise(TestEnterprise);
		for (int i = 0; i < 100; i++)
		{
			NewGuestThread thread = GuiceContext.get(NewGuestThread.class);

			/*thread.setEnterprise(TestEnterprise);
			thread.setIdentityToken(ProfileSystem.getSystemTokens()
			                                     .get(enterprise));*/
			service = JobService.getInstance()
			                    .addJob("TestCreate100NewGuests", thread);
		}
		service.isTerminated();
		JobService.getInstance()
		          .waitForJob("TestCreate100NewGuests", 5, MINUTES);
	}

	@org.junit.jupiter.api.Test
	void findByWebClientKey()
	{

	}
}