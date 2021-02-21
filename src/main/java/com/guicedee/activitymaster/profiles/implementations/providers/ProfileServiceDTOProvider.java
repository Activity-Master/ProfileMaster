package com.guicedee.activitymaster.profiles.implementations.providers;

import com.google.inject.Provider;
import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;
import com.guicedee.guicedinjection.GuiceContext;
import com.jwebmp.core.base.ajax.AjaxCall;
import com.jwebmp.core.utilities.StaticStrings;

import java.util.Map;
import java.util.UUID;

public class ProfileServiceDTOProvider
		implements Provider<ProfileServiceDTO<?>>
{
	@Override
	public ProfileServiceDTO<?> get()
	{
		AjaxCall<?> ajaxCall = GuiceContext.get(AjaxCall.class);
		if(ajaxCall.getVariable(StaticStrings.LOCAL_STORAGE_VARIABLE_KEY) != null)
		{
			Map<String, String> stringStringMap = ajaxCall.getVariable(StaticStrings.LOCAL_STORAGE_VARIABLE_KEY)
			                                              .asMap();
			UUID identityToken = UUID.fromString(stringStringMap.get(StaticStrings.LOCAL_STORAGE_PARAMETER_KEY));

			ProfileServiceDTO<?> pro = new ProfileServiceDTO<>();
			GuiceContext.inject().injectMembers(pro);
			pro.setWebClientUUID(identityToken);
			return pro;
		}
		else {
			ProfileServiceDTO<?> pdto = new ProfileServiceDTO<>();
			GuiceContext.inject().injectMembers(pdto);
			return pdto;
		}
	}
}
