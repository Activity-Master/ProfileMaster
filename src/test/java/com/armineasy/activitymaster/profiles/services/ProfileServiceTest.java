package com.armineasy.activitymaster.profiles.services;

import com.armineasy.activitymaster.activity.configs.DefaultTestConfig;
import com.armineasy.activitymaster.activitymaster.ActivityMasterConfiguration;
import com.armineasy.activitymaster.activitymaster.db.entities.enterprise.Enterprise;
import com.armineasy.activitymaster.activitymaster.db.entities.involvedparty.InvolvedParty;
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

import static com.jwebmp.guicedinjection.GuiceContext.*;

@ExtendWith(DefaultTestConfig.class)
@Log
class ProfileServiceTest
{

	@org.junit.jupiter.api.Test
	public void testCreateNewGuest()
	{
		ProfileService ps = get(ProfileService.class);

		Enterprise enterprise = get(IEnterpriseService.class)
				                        .findEnterprise(get(ActivityMasterConfiguration.class).getEnterpriseName())
				                        .orElseThrow();


		GuestDTO<?> newGuest = new GuestDTO<>().setWebClientUUID(UUID.randomUUID());
		//newGuest.setReadableUserAgent()
		newGuest = ps.loginVisitor(newGuest, enterprise, ProfileSystem.getSystemTokens()
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
		ProfileService ps = get(ProfileService.class);

		Enterprise enterprise = get(IEnterpriseService.class)
				                        .findEnterprise(get(ActivityMasterConfiguration.class).getEnterpriseName())
				                        .orElseThrow();


		GuestDTO<?> newGuest = new GuestDTO<>().setWebClientUUID(UUID.randomUUID());
		for (int i = 0; i < 100; i++)
		{
			log.info("Creating Guest User [" + i + "]");
			newGuest = new GuestDTO<>().setWebClientUUID(UUID.randomUUID());
			//newGuest.setReadableUserAgent()
			GuestDTO dto = ps.loginVisitor(newGuest, enterprise, ProfileSystem.getSystemTokens()
			                                                   .get(enterprise));

		}
	}

	@org.junit.jupiter.api.Test
	void findByWebClientKey()
	{

	}

}