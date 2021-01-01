package com.guicedee.activitymaster.profiles.services;

import com.guicedee.activitymaster.core.services.dto.IEnterprise;
import com.guicedee.activitymaster.core.services.dto.ISystems;
import com.guicedee.activitymaster.core.services.system.IEnterpriseService;
import com.guicedee.activitymaster.core.threads.TransactionalIdentifiedThread;
import com.guicedee.activitymaster.profiles.ProfileService;
import com.guicedee.activitymaster.profiles.ProfileSystem;
import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;
import com.guicedee.activitymaster.profiles.webdto.UserLoginDTO;
import lombok.extern.java.Log;

import java.util.UUID;

import static com.guicedee.activitymaster.core.DefaultEnterprise.*;
import static com.guicedee.guicedinjection.GuiceContext.*;

@Log
public class NewGuestThread
		extends TransactionalIdentifiedThread
{

	@Override
	public void perform()
	{
		ProfileService ps = get(ProfileService.class);
		UserLoginDTO<?> newGuest = new UserLoginDTO<>().setWebClientUUID(UUID.randomUUID());
		//newGuest.setReadableUserAgent()
		IEnterprise<?> enterprise = get(IEnterpriseService.class).getEnterprise(TestEnterprise);
		ISystems<?> profileSystem = get(ProfileSystem.class)
				                            .getSystem(enterprise);
		UUID profileSystemUUID = get(ProfileSystem.class)
				                         .getSystemToken(enterprise);
		ProfileServiceDTO dto = ps.loginVisitor(newGuest, profileSystem, profileSystemUUID);
		log.info("Created Guest : " + newGuest.getWebClientUUID());
	}
}
