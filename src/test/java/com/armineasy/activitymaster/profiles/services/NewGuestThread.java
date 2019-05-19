package com.armineasy.activitymaster.profiles.services;

import com.armineasy.activitymaster.activitymaster.db.entities.enterprise.Enterprise;
import com.armineasy.activitymaster.activitymaster.services.system.IEnterpriseService;
import com.armineasy.activitymaster.activitymaster.threads.TransactionalIdentifiedThread;
import com.armineasy.activitymaster.profiles.ProfileSystem;
import com.armineasy.activitymaster.profiles.dto.GuestDTO;
import lombok.extern.java.Log;

import java.util.UUID;

import static com.armineasy.activitymaster.activitymaster.DefaultEnterprise.*;
import static com.jwebmp.guicedinjection.GuiceContext.*;

@Log
public class NewGuestThread
		extends TransactionalIdentifiedThread
{

	public void perform()
	{
		ProfileService ps = get(ProfileService.class);
		GuestDTO newGuest = new GuestDTO<>().setWebClientUUID(UUID.randomUUID());
		//newGuest.setReadableUserAgent()
		Enterprise enterprise = get(IEnterpriseService.class).getEnterprise(TestEnterprise);
		GuestDTO dto = ps.loginVisitor(newGuest, TestEnterprise, ProfileSystem.getSystemTokens()
		                                                                      .get(enterprise));


		log.info("Created Guest : " + newGuest.getWebClientUUID());
	}
}
