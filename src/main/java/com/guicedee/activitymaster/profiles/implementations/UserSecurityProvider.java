package com.guicedee.activitymaster.profiles.implementations;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.guicedee.activitymaster.core.services.dto.IEnterprise;
import com.guicedee.activitymaster.core.services.dto.IInvolvedParty;
import com.guicedee.activitymaster.core.services.dto.ISystems;
import com.guicedee.activitymaster.profiles.ProfileSystem;
import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;
import com.guicedee.activitymaster.profiles.dto.UserSecurityDTO;
import com.guicedee.activitymaster.profiles.services.interfaces.IProfileService;
import com.guicedee.activitymaster.sessions.services.ISession;
import com.guicedee.activitymaster.sessions.services.ISessionMasterService;
import com.guicedee.guicedinjection.GuiceContext;
import com.jwebmp.core.base.ajax.AjaxCall;
import com.jwebmp.core.base.ajax.JsonVariable;
import com.jwebmp.core.utilities.StaticStrings;

import java.util.Map;
import java.util.UUID;

public class UserSecurityProvider
		implements Provider<UserSecurityDTO>
{
	@Inject
	private ProfileServiceDTO profileServiceDTO;
	
	@Override
	public UserSecurityDTO get()
	{
		AjaxCall<?> call = GuiceContext.get(AjaxCall.class);
		if(call.getVariable(StaticStrings.LOCAL_STORAGE_VARIABLE_KEY) != null)
		{
			IProfileService profileService = GuiceContext.get(IProfileService.class);
			
			IInvolvedParty<?> involvedParty = profileServiceDTO.findInvolvedParty();
			if (involvedParty == null)
			{
				return new UserSecurityDTO();
			}
			IEnterprise<?> enterprise = involvedParty.getEnterprise();
			
			ISystems<?> system = GuiceContext.get(ProfileSystem.class)
			                                 .getSystem(enterprise);
			UUID systemToken = GuiceContext.get(ProfileSystem.class)
			                               .getSystemToken(enterprise);
			
			ISessionMasterService<?> sessionMasterService = GuiceContext.get(ISessionMasterService.class);
			ISession<?> session = sessionMasterService.getSession(involvedParty, system, systemToken);
			UserSecurityDTO us;
			if (session.hasValue("user-security"))
			{
				us = session.as("user-security", UserSecurityDTO.class);
			}
			else
			{
				us = new UserSecurityDTO();
				session.addValue("user-security", us);
			}
			return us;
		}
		else
		{
			return new UserSecurityDTO();
		}
	}
}
