package com.guicedee.activitymaster.profiles.webdto;

import com.guicedee.activitymaster.core.services.classifications.enterprise.IEnterpriseName;
import com.guicedee.activitymaster.core.services.dto.IEnterprise;
import com.guicedee.activitymaster.core.services.dto.IInvolvedParty;
import com.guicedee.activitymaster.core.services.dto.ISystems;
import com.guicedee.activitymaster.core.services.system.IEnterpriseService;
import com.guicedee.activitymaster.profiles.ProfileSystem;
import com.guicedee.activitymaster.profiles.dto.UserDTO;
import com.guicedee.activitymaster.profiles.services.interfaces.IRolesService;
import com.guicedee.activitymaster.profiles.services.interfaces.IUserRole;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;

import static com.guicedee.activitymaster.core.services.classifications.classification.Classifications.*;
import static com.guicedee.activitymaster.core.services.types.NameTypes.*;
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

	public UserProfileBasicDTO<?> from(IInvolvedParty<?> involvedParty, IEnterpriseName<?> enterpriseName)
	{
		IEnterpriseService enterpriseService = get(IEnterpriseService.class);
		IEnterprise<?> enterprise = enterpriseService.getIEnterpriseFromName(enterpriseName);
		ISystems<?> system = get(ProfileSystem.class).getSystem(enterprise);
		UUID identityToken = get(ProfileSystem.class).getSystemToken(enterprise);
		
		

		if (involvedParty.hasNameType(CommonNameType,null, system, identityToken))
		{
			fullName = involvedParty.findNameType(CommonNameType,NoClassification.name(), system, identityToken)
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
		else if (involvedParty.hasNameType(PreferredNameType,null, system, identityToken))
		{
			fullName = involvedParty.findNameType(PreferredNameType,NoClassification.name(), system, identityToken)
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
		else if (involvedParty.hasNameType(FullNameType,null, system, identityToken))
		{
			fullName = involvedParty.findNameType(FullNameType, NoClassification.name(),system, identityToken)
			                        .get()
			                        .getValue();
		}
		else if (involvedParty.hasNameType(FirstNameType,null, system, identityToken))
		{
			fullName = involvedParty.findNameType(FirstNameType, NoClassification.name(),system, identityToken)
			                        .get()
			                        .getValue();
			if (involvedParty.hasNameType(SurnameType,null, system, identityToken))
			{
				fullName += " " + involvedParty.findNameType(SurnameType, NoClassification.name(),system, identityToken)
				                               .get()
				                               .getValue();
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
