package com.guicedee.activitymaster.profiles.services;

import com.guicedee.activitymaster.core.services.dto.IEnterprise;
import com.guicedee.activitymaster.core.services.system.IEnterpriseService;
import com.guicedee.activitymaster.core.threads.TransactionalIdentifiedThread;
import com.guicedee.activitymaster.profiles.ProfileService;
import com.guicedee.activitymaster.profiles.ProfileSystem;
import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;
import com.guicedee.activitymaster.profiles.dto.UserLoginDTO;
import lombok.extern.java.Log;

import java.util.UUID;

import static com.guicedee.activitymaster.core.DefaultEnterprise.*;
import static com.guicedee.guicedinjection.GuiceContext.*;

@Log
public class NewGuestThread
		extends TransactionalIdentifiedThread
{

	public void perform()
	{
		ProfileService ps = get(ProfileService.class);
		UserLoginDTO<?> newGuest = new UserLoginDTO<>().setWebClientUUID(UUID.randomUUID());
		//newGuest.setReadableUserAgent()
		IEnterprise<?> enterprise = get(IEnterpriseService.class).getEnterprise(TestEnterprise);
		ProfileServiceDTO dto = ps.loginVisitor(newGuest, TestEnterprise, ProfileSystem.getSystemTokens()
		                                                                               .get(enterprise));


		log.info("Created Guest : " + newGuest.getWebClientUUID());
	}
}
