package com.guicedee.activitymaster.profiles.implementations;

import com.google.inject.Provider;
import com.guicedee.activitymaster.core.services.dto.IEnterprise;
import com.guicedee.activitymaster.core.services.dto.IInvolvedParty;
import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;
import com.guicedee.activitymaster.profiles.services.interfaces.IProfileService;
import com.guicedee.guicedinjection.GuiceContext;
import com.jwebmp.core.base.ajax.AjaxCall;
import com.jwebmp.core.utilities.StaticStrings;

import java.util.Map;
import java.util.UUID;

public class EnterpriseProvider implements Provider<IEnterprise>
{
	@Override
	public IEnterprise<?> get()
	{
		AjaxCall<?> call = GuiceContext.get(AjaxCall.class);
		
		Map<String, String> stringStringMap = call.getVariable(StaticStrings.LOCAL_STORAGE_VARIABLE_KEY)
		                                          .asMap();
		UUID identityToken = UUID.fromString(stringStringMap.get(StaticStrings.LOCAL_STORAGE_PARAMETER_KEY));
		ProfileServiceDTO<?> pro = new ProfileServiceDTO<>();
		
		pro.setWebClientUUID(identityToken);
		IProfileService profileService = GuiceContext.get(IProfileService.class);
		IInvolvedParty<?> involvedParty = profileService.findInvolvedParty(pro.getWebClientUUID());
		if (involvedParty == null)
		{
			return null;
		}
		IEnterprise<?> enterprise = involvedParty.getEnterprise();
		return enterprise;
	}
}
