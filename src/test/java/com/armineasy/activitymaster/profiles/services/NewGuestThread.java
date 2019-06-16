package com.armineasy.activitymaster.profiles.services;

import com.armineasy.activitymaster.activitymaster.db.entities.enterprise.Enterprise;
import com.armineasy.activitymaster.activitymaster.services.dto.IEnterprise;
import com.armineasy.activitymaster.activitymaster.services.system.IEnterpriseService;
import com.armineasy.activitymaster.activitymaster.threads.TransactionalIdentifiedThread;
import com.armineasy.activitymaster.profiles.ProfileService;
import com.armineasy.activitymaster.profiles.ProfileSystem;
import com.armineasy.activitymaster.profiles.dto.ProfileServiceDTO;
import com.armineasy.activitymaster.profiles.dto.UserLoginDTO;
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
		UserLoginDTO<?> newGuest = new UserLoginDTO<>().setWebClientUUID(UUID.randomUUID());
		//newGuest.setReadableUserAgent()
		IEnterprise<?> enterprise = get(IEnterpriseService.class).getEnterprise(TestEnterprise);
		ProfileServiceDTO dto = ps.loginVisitor(newGuest, TestEnterprise, ProfileSystem.getSystemTokens()
		                                                                               .get(enterprise));


		log.info("Created Guest : " + newGuest.getWebClientUUID());
	}
}
