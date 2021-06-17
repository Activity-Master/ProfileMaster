package com.guicedee.activitymaster.profiles.implementations.providers;

import com.google.inject.Provider;
import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;
import com.guicedee.guicedinjection.GuiceContext;
import com.jwebmp.core.base.ajax.AjaxCall;

import java.util.Map;
import java.util.UUID;

import static com.jwebmp.core.utilities.StaticStrings.*;

public class ProfileServiceDTOProvider
		implements Provider<ProfileServiceDTO<?>>
{
	@Override
	public ProfileServiceDTO<?> get()
	{
		AjaxCall<?> ajaxCall = GuiceContext.get(AjaxCall.class);
		if(ajaxCall.getVariable("localstorage") != null)
		{
			Map<String, Object> stringStringMap = ajaxCall.getVariable("localstorage")
			                                              .asMap();
			if(stringStringMap.containsKey(LOCAL_STORAGE_PARAMETER_KEY))
			{
				UUID identityToken = UUID.fromString(stringStringMap.get(LOCAL_STORAGE_PARAMETER_KEY)
				                                                    .toString());
				
				ProfileServiceDTO<?> pro = new ProfileServiceDTO<>();
				GuiceContext.inject()
				            .injectMembers(pro);
				pro.setWebClientUUID(identityToken);
				return pro;
			}else
			{
				ProfileServiceDTO<?> pdto = new ProfileServiceDTO<>();
				GuiceContext.inject().injectMembers(pdto);
				return pdto;
			}
		}
		else {
			ProfileServiceDTO<?> pdto = new ProfileServiceDTO<>();
			GuiceContext.inject().injectMembers(pdto);
			return pdto;
		}
	}
}
