package com.guicedee.activitymaster.profiles.implementations.providers;

import com.google.inject.Provider;
import com.guicedee.activitymaster.core.services.dto.IInvolvedParty;
import com.guicedee.activitymaster.core.services.system.IInvolvedPartyService;
import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;
import com.guicedee.guicedinjection.GuiceContext;
import com.guicedee.guicedservlets.services.scopes.CallScope;
import com.jwebmp.core.base.ajax.AjaxCall;
import com.jwebmp.core.utilities.StaticStrings;

import java.util.Map;
import java.util.UUID;

import static com.guicedee.activitymaster.profiles.enumerations.ProfileIdentificationTypes.*;

@CallScope
public class ProfileServiceDTOProvider
		implements Provider<ProfileServiceDTO>
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

			/*
			if (call.isWebSocketCall())
			{
				if (GuicedWebSocket.hasProperty(call.getWebsocketSession(), StaticStrings.LOCAL_STORAGE_PARAMETER_KEY))
				{
					localStorageKey = GuicedWebSocket.getPropertyMap(call.getWebsocketSession())
					                                 .get(StaticStrings.LOCAL_STORAGE_PARAMETER_KEY);
				}
			}
			*/

			ProfileServiceDTO<?> pro = new ProfileServiceDTO<>();
			pro.setWebClientUUID(identityToken);
			
			IInvolvedPartyService<?> partyService = GuiceContext.get(IInvolvedPartyService.class);
			IInvolvedParty<?> byIdentificationType = partyService.findByIdentificationType(IdentificationTypeWebClientUUID.toString(), pro.getWebClientUUID()
			                                                                                                                              .toString());
			if (byIdentificationType == null)
			{
				//must create an involved party for this client UUID?
			}
			
			return pro;
		}
		else {
			return new ProfileServiceDTO<>();
		}
	}
}
