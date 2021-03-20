package com.guicedee.activitymaster.profiles.webdto;


import com.guicedee.activitymaster.client.services.IEnterpriseService;
import com.guicedee.activitymaster.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.profiles.ProfileSystem;
import com.guicedee.activitymaster.profiles.dto.UserDTO;
import com.guicedee.activitymaster.profiles.services.interfaces.IRolesService;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.*;

import static com.guicedee.activitymaster.client.services.classifications.DefaultClassifications.*;
import static com.guicedee.activitymaster.client.services.classifications.types.NameTypes.*;
import static com.guicedee.guicedinjection.GuiceContext.*;

@Data
@Accessors(chain = true)
public class UserProfileBasicDTO<J extends UserProfileBasicDTO<J>>
		extends UserDTO<J>
{
	private String fullName;
	private String firstName;
	private String surname;
	private Set<String> roles;

	public UserProfileBasicDTO<?> from(IInvolvedParty<?,?> involvedParty, String enterpriseName)
	{
		IEnterpriseService<?> enterpriseService = get(IEnterpriseService.class);
		IEnterprise<?,?> enterprise = enterpriseService.getIEnterpriseFromName(enterpriseName);
		ISystems<?,?> system = get(ProfileSystem.class).getSystem(enterprise);
		UUID identityToken = get(ProfileSystem.class).getSystemToken(enterprise);

		if (involvedParty.hasInvolvedPartyNameTypes(NoClassification.toString(),CommonNameType.toString(),null, system, identityToken))
		{
			fullName = involvedParty.findInvolvedPartyNameType(NoClassification.toString(),CommonNameType.toString(),null, system,true,true, identityToken)
			                        .get()
			                        .getValue();
			if (fullName.contains(" "))

			{
				StringTokenizer st = new StringTokenizer(fullName, " ");
				firstName = st.nextToken();
				if (st.hasMoreTokens())
				{
					surname = st.nextToken();
				}
			}
		}
		else if (involvedParty.hasInvolvedPartyNameTypes(NoClassification.toString(),PreferredNameType.toString(),null, system, identityToken))
		{
			fullName = involvedParty.findInvolvedPartyNameType(NoClassification.toString(),PreferredNameType.toString(),null, system,true,true, identityToken)
			                        .get().getValue();
			if (fullName.contains(" "))

			{
				StringTokenizer st = new StringTokenizer(fullName, " ");
				firstName = st.nextToken();
				if (st.hasMoreTokens())
				{
					surname = st.nextToken();
				}
			}
		}
		else if (involvedParty.hasInvolvedPartyNameTypes(NoClassification.toString(),FullNameType.toString(),null, system, identityToken))
		{
			fullName = involvedParty.findInvolvedPartyNameType(NoClassification.toString(),FullNameType.toString(),null, system,true,true, identityToken)
			                        .get().getValue();
		}
		else if (involvedParty.hasInvolvedPartyNameTypes(NoClassification.toString(),FirstNameType.toString(),null, system, identityToken))
		{
			fullName = involvedParty.findInvolvedPartyNameType(NoClassification.toString(),FirstNameType.toString(),null, system,true,true, identityToken)
			                        .get().getValue();
			if (involvedParty.hasInvolvedPartyNameTypes(NoClassification.toString(),SurnameType.toString(),null, system, identityToken))
			{
				fullName += " " + involvedParty.findInvolvedPartyNameType(NoClassification.toString(),SurnameType.toString(),null, system,true,true, identityToken)
				                               .get().getValue();
			}
		}
		else
		{
			fullName = "Guest";
		}

		roles = get(IRolesService.class)
				        .getRoles(involvedParty, system, identityToken);
		return this;
	}
}
