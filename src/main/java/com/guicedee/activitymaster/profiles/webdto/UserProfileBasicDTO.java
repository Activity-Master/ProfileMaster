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
	private List<IUserRole<?>> roles;

	public UserProfileBasicDTO<?> from(IInvolvedParty<?> involvedParty, IEnterpriseName<?> enterpriseName)
	{
		IEnterpriseService enterpriseService = get(IEnterpriseService.class);
		IEnterprise<?> enterprise = enterpriseService.getIEnterpriseFromName(enterpriseName);
		ISystems<?> originatingSystem = get(ProfileSystem.class).getSystem(enterprise);
		UUID identityToken = get(ProfileSystem.class).getSystemToken(enterprise);

		if (involvedParty.hasNameType(CommonNameType,null, originatingSystem, identityToken))
		{
			fullName = involvedParty.findNameType(CommonNameType,NoClassification.name(), enterprise, identityToken)
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
		else if (involvedParty.hasNameType(PreferredNameType,null, originatingSystem, identityToken))
		{
			fullName = involvedParty.findNameType(PreferredNameType,NoClassification.name(), enterprise, identityToken)
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
		else if (involvedParty.hasNameType(FullNameType,null, originatingSystem, identityToken))
		{
			fullName = involvedParty.findNameType(FullNameType, NoClassification.name(),enterprise, identityToken)
			                        .get()
			                        .getValue();
		}
		else if (involvedParty.hasNameType(FirstNameType,null, originatingSystem, identityToken))
		{
			fullName = involvedParty.findNameType(FirstNameType, NoClassification.name(),enterprise, identityToken)
			                        .get()
			                        .getValue();
			if (involvedParty.hasNameType(SurnameType,null, originatingSystem, identityToken))
			{
				fullName += " " + involvedParty.findNameType(SurnameType, NoClassification.name(),enterprise, identityToken)
				                               .get()
				                               .getValue();
			}
		}
		else
		{
			fullName = "Guest";
		}

		roles = get(IRolesService.class)
				        .getRoles(involvedParty, originatingSystem, identityToken);
		return this;
	}
}
