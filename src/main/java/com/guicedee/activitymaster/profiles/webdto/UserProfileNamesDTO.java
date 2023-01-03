package com.guicedee.activitymaster.profiles.webdto;


import com.fasterxml.jackson.annotation.*;
import com.google.common.base.Strings;
import com.guicedee.activitymaster.fsdm.client.services.annotations.InvolvedPartyEvent;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.events.IEvent;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.types.annotations.Party;
import com.guicedee.activitymaster.fsdm.client.types.classifications.EventAction;
import com.guicedee.activitymaster.profiles.ProfileSystem;
import com.guicedee.activitymaster.profiles.dto.UserDTO;
import com.guicedee.guicedinjection.GuiceContext;
import jakarta.cache.annotation.CacheKey;
import jakarta.cache.annotation.CacheResult;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static com.guicedee.activitymaster.fsdm.client.types.classifications.DefaultClassifications.*;
import static com.guicedee.activitymaster.fsdm.client.types.classifications.types.NameTypes.*;
import static com.guicedee.guicedinjection.GuiceContext.*;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
                getterVisibility = JsonAutoDetect.Visibility.NONE,
                setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonIdentityInfo(
		generator = ObjectIdGenerators.PropertyGenerator.class,
		property = "identityToken")
public class UserProfileNamesDTO<J extends UserProfileNamesDTO<J>>
		extends UserDTO<J>
{
	private String fullName;
	private String birthName;
	private String legalName;
	private String salutationName;
	private String qualificationName;
	private String commonName;
	private String preferredName;
	private String firstName;
	private String initials;
	private String surname;
	private String suffix;
	private String title;
	
	private Set<String> middleNames = new LinkedHashSet<>();
	
	@JsonIgnore
	private IInvolvedParty<?, ?> involvedParty;
	
	public String getFullName()
	{
		return fullName;
	}
	
	public UserProfileNamesDTO<J> setFullName(String fullName)
	{
		this.fullName = fullName;
		return this;
	}
	
	public String getBirthName()
	{
		return birthName;
	}
	
	public UserProfileNamesDTO<J> setBirthName(String birthName)
	{
		this.birthName = birthName;
		return this;
	}
	
	public String getLegalName()
	{
		return legalName;
	}
	
	public UserProfileNamesDTO<J> setLegalName(String legalName)
	{
		this.legalName = legalName;
		return this;
	}
	
	public String getSalutationName()
	{
		return salutationName;
	}
	
	public UserProfileNamesDTO<J> setSalutationName(String salutationName)
	{
		this.salutationName = salutationName;
		return this;
	}
	
	public String getQualificationName()
	{
		return qualificationName;
	}
	
	public UserProfileNamesDTO<J> setQualificationName(String qualificationName)
	{
		this.qualificationName = qualificationName;
		return this;
	}
	
	public String getCommonName()
	{
		return commonName;
	}
	
	public UserProfileNamesDTO<J> setCommonName(String commonName)
	{
		this.commonName = commonName;
		return this;
	}
	
	public String getPreferredName()
	{
		return preferredName;
	}
	
	public UserProfileNamesDTO<J> setPreferredName(String preferredName)
	{
		this.preferredName = preferredName;
		return this;
	}
	
	public String getFirstName()
	{
		return firstName;
	}
	
	public UserProfileNamesDTO<J> setFirstName(String firstName)
	{
		this.firstName = firstName;
		return this;
	}
	
	public String getInitials()
	{
		return initials;
	}
	
	public UserProfileNamesDTO<J> setInitials(String initials)
	{
		this.initials = initials;
		return this;
	}
	
	public String getSurname()
	{
		return surname;
	}
	
	public UserProfileNamesDTO<J> setSurname(String surname)
	{
		this.surname = surname;
		return this;
	}
	
	public String getSuffix()
	{
		return suffix;
	}
	
	public UserProfileNamesDTO<J> setSuffix(String suffix)
	{
		this.suffix = suffix;
		return this;
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public UserProfileNamesDTO<J> setTitle(String title)
	{
		this.title = title;
		return this;
	}
	
	public Set<String> getMiddleNames()
	{
		return middleNames;
	}
	
	public UserProfileNamesDTO<J> setMiddleNames(Set<String> middleNames)
	{
		this.middleNames = middleNames;
		return this;
	}
	
	public IInvolvedParty<?, ?> getInvolvedParty()
	{
		return involvedParty;
	}
	
	public UserProfileNamesDTO<J> setInvolvedParty(IInvolvedParty<?, ?> involvedParty)
	{
		this.involvedParty = involvedParty;
		return this;
	}
	
	@CacheResult(cacheName = "UserProfileNamesDTO")
	public J from(@CacheKey IInvolvedParty<?, ?> involvedParty)
	{
		this.involvedParty = involvedParty;
		setEnterprise(involvedParty.getEnterprise());
		ISystems<?, ?> system = get(ProfileSystem.class).getSystem(getEnterprise());
		UUID identityToken = get(ProfileSystem.class).getSystemToken(getEnterprise());
		
		if (involvedParty.hasInvolvedPartyNameTypes(NoClassification.toString(), CommonNameType.toString(), null, system, identityToken))
		{
			setCommonName(involvedParty.findInvolvedPartyNameType(NoClassification.toString(), CommonNameType.toString(), null, system, true, true, identityToken)
			                           .orElseThrow()
			                           .getValue());
		}
		if (involvedParty.hasInvolvedPartyNameTypes(NoClassification.toString(), BirthNameType.toString(), null, system, identityToken))
		{
			setBirthName(involvedParty.findInvolvedPartyNameType(NoClassification.toString(), BirthNameType.toString(), null, system, true, true, identityToken)
			                          .orElseThrow()
			                          .getValue());
		}
		if (involvedParty.hasInvolvedPartyNameTypes(NoClassification.toString(), LegalNameType.toString(), null, system, identityToken))
		{
			setLegalName(involvedParty.findInvolvedPartyNameType(NoClassification.toString(), LegalNameType.toString(), null, system, true, true, identityToken)
			                          .orElseThrow()
			                          .getValue());
		}
		if (involvedParty.hasInvolvedPartyNameTypes(NoClassification.toString(), SalutationType.toString(), null, system, identityToken))
		{
			setSalutationName(involvedParty.findInvolvedPartyNameType(NoClassification.toString(), SalutationType.toString(), null, system, true, true, identityToken)
			                               .orElseThrow()
			                               .getValue());
		}
		if (involvedParty.hasInvolvedPartyNameTypes(NoClassification.toString(), QualificationType.toString(), null, system, identityToken))
		{
			setQualificationName(involvedParty.findInvolvedPartyNameType(NoClassification.toString(), QualificationType.toString(), null, system, true, true, identityToken)
			                                  .orElseThrow()
			                                  .getValue());
		}
		if (involvedParty.hasInvolvedPartyNameTypes(NoClassification.toString(), SuffixType.toString(), null, system, identityToken))
		{
			setSuffix(involvedParty.findInvolvedPartyNameType(NoClassification.toString(), SuffixType.toString(), null, system, true, true, identityToken)
			                       .orElseThrow()
			                       .getValue());
		}
		if (involvedParty.hasInvolvedPartyNameTypes(NoClassification.toString(), MiddleNameType.toString(), null, system, identityToken))
		{
			getMiddleNames().clear();
			var involvedPartyNameTypesAll = involvedParty.findInvolvedPartyNameTypesAll(NoClassification.toString(), SuffixType.toString(), null, system, false, identityToken);
			for (var middleName : involvedPartyNameTypesAll)
			{
				getMiddleNames().add(middleName.getValue());
			}
		}
		if (involvedParty.hasInvolvedPartyNameTypes(NoClassification.toString(), PreferredNameType.toString(), null, system, identityToken))
		{
			setPreferredName(involvedParty.findInvolvedPartyNameType(NoClassification.toString(), PreferredNameType.toString(), null, system, true, true, identityToken)
			                              .orElseThrow()
			                              .getValue());
		}
		
		if (involvedParty.hasInvolvedPartyNameTypes(NoClassification.toString(), FullNameType.toString(), null, system, identityToken))
		{
			setFullName(involvedParty.findInvolvedPartyNameType(NoClassification.toString(), FullNameType.toString(), null, system, true, true, identityToken)
			                         .orElseThrow()
			                         .getValue());
		}
		if (involvedParty.hasInvolvedPartyNameTypes(NoClassification.toString(), FirstNameType.toString(), null, system, identityToken))
		{
			setFirstName(involvedParty.findInvolvedPartyNameType(NoClassification.toString(), FirstNameType.toString(), null, system, true, true, identityToken)
			                          .orElseThrow()
			                          .getValue());
		}
		if (involvedParty.hasInvolvedPartyNameTypes(NoClassification.toString(), SurnameType.toString(), null, system, identityToken))
		{
			setSurname(involvedParty.findInvolvedPartyNameType(NoClassification.toString(), SurnameType.toString(), null, system, true, true, identityToken)
			                        .orElseThrow()
			                        .getValue());
		}
		if (involvedParty.hasInvolvedPartyNameTypes(NoClassification.toString(), InitialsType.toString(), null, system, identityToken))
		{
			setInitials(involvedParty.findInvolvedPartyNameType(NoClassification.toString(), InitialsType.toString(), null, system, false, false, identityToken)
			                         .orElseThrow()
			                         .getValue());
		}
		//noinspection unchecked
		return (J) this;
	}
	
	@CacheResult(cacheName = "UserProfileNamesDTO", skipGet = true)
	@InvolvedPartyEvent(EventAction.Updated)
	public void update(@CacheKey IInvolvedParty<?, ?> thisInvolvedParty,
	                   @Party("Updated") J updatedParty,
	                   @Party("OnBehalfOf") IInvolvedParty<?, ?> involvedParty)
	{
		ISystems<?, ?> system = get(ProfileSystem.class).getSystem(getEnterprise());
		UUID identityToken = get(ProfileSystem.class).getSystemToken(getEnterprise());
		
		IEvent<?, ?> event = GuiceContext.get(IEvent.class);
		event.addInvolvedParty(thisInvolvedParty, "Updated", null, system, identityToken);
		
		if (!Strings.isNullOrEmpty(updatedParty.getTitle()))
		{
			getInvolvedParty().addOrUpdateInvolvedPartyNameType(NoClassification.toString(), InitialsType, this.title, updatedParty.getTitle(), system, identityToken);
		}
		if (!Strings.isNullOrEmpty(updatedParty.getFirstName()))
		{
			getInvolvedParty().addOrUpdateInvolvedPartyNameType(NoClassification.toString(), FirstNameType, this.firstName, updatedParty.getFirstName(), system, identityToken);
		}
		if (!Strings.isNullOrEmpty(updatedParty.getSurname()))
		{
			getInvolvedParty().addOrUpdateInvolvedPartyNameType(NoClassification.toString(), SurnameType, this.surname, updatedParty.getSurname(), system, identityToken);
		}
		if (!Strings.isNullOrEmpty(updatedParty.getInitials()))
		{
			getInvolvedParty().addOrUpdateInvolvedPartyNameType(NoClassification.toString(), InitialsType, this.initials, updatedParty.getInitials(), system, identityToken);
		}
		if (!Strings.isNullOrEmpty(updatedParty.getFullName()))
		{
			getInvolvedParty().addOrUpdateInvolvedPartyNameType(NoClassification.toString(), FullNameType, this.fullName, updatedParty.getFullName(), system, identityToken);
		}
		if (!Strings.isNullOrEmpty(updatedParty.getBirthName()))
		{
			getInvolvedParty().addOrUpdateInvolvedPartyNameType(NoClassification.toString(), BirthNameType, this.birthName, updatedParty.getBirthName(), system, identityToken);
		}
		if (!Strings.isNullOrEmpty(updatedParty.getLegalName()))
		{
			getInvolvedParty().addOrUpdateInvolvedPartyNameType(NoClassification.toString(), LegalNameType, this.legalName, updatedParty.getLegalName(), system, identityToken);
		}
		if (!Strings.isNullOrEmpty(updatedParty.getSalutationName()))
		{
			getInvolvedParty().addOrUpdateInvolvedPartyNameType(NoClassification.toString(), SalutationType, this.salutationName, updatedParty.getSalutationName(), system, identityToken);
		}
		if (!Strings.isNullOrEmpty(updatedParty.getQualificationName()))
		{
			getInvolvedParty().addOrUpdateInvolvedPartyNameType(NoClassification.toString(), QualificationType, this.qualificationName, updatedParty.getQualificationName(), system, identityToken);
		}
		if (!Strings.isNullOrEmpty(updatedParty.getSuffix()))
		{
			getInvolvedParty().addOrUpdateInvolvedPartyNameType(NoClassification.toString(), SuffixType, this.suffix, updatedParty.getSuffix(), system, identityToken);
		}
		if (!Strings.isNullOrEmpty(updatedParty.getPreferredName()))
		{
			getInvolvedParty().addOrUpdateInvolvedPartyNameType(NoClassification.toString(), PreferredNameType, this.preferredName, updatedParty.getPreferredName(), system, identityToken);
		}
		if (!updatedParty.getMiddleNames()
		                 .isEmpty())
		{
			for (var currentMiddleName : getInvolvedParty().findInvolvedPartyNameTypesAll(NoClassification.toString(), MiddleNameType.toString(), null, system, false, identityToken))
			{
				currentMiddleName.expire();
			}
			for (String middleName : updatedParty.getMiddleNames())
			{
				getInvolvedParty().addInvolvedPartyNameType(NoClassification.toString(), MiddleNameType.toString(), middleName, system, identityToken);
			}
		}
	}
	
	/**
	 * Mr firstname middle name surname
	 *
	 * @return
	 */
	@SuppressWarnings("DuplicatedCode")
	public String toFullString()
	{
		StringBuilder personNameBuilder = new StringBuilder();
		if (getTitle() != null)
		{
			personNameBuilder.append(StringUtils.capitalize(Strings.nullToEmpty(getTitle())));
			personNameBuilder.append(" ");
		}
		if (this.getFirstName() != null)
		{
			personNameBuilder.append(getFirstName());
			personNameBuilder.append(" ");
		}
		
		for (String middleName : getMiddleNames())
		{
			if (!Strings.isNullOrEmpty(middleName))
			{
				personNameBuilder.append(StringUtils.capitalize(middleName));
				personNameBuilder.append(" ");
			}
		}
		if (getSurname() != null)
		{
			personNameBuilder.append(StringUtils.capitalize(Strings.nullToEmpty(getSurname())));
		}
		return personNameBuilder.toString();
	}
	
	/**
	 * MR M B surname
	 *
	 * @return
	 */
	@SuppressWarnings("DuplicatedCode")
	public String toInitialsString()
	{
		StringBuilder personNameBuilder = new StringBuilder();
		if (getTitle() != null)
		{
			personNameBuilder.append(StringUtils.capitalize(Strings.nullToEmpty(getTitle())));
			personNameBuilder.append(" ");
		}
		
		if (this.getFirstName() != null)
		{
			personNameBuilder.append(getFirstName());
			personNameBuilder.append(" ");
		}
		return personNameBuilder +
		       toInitials() +
		       StringUtils.capitalize(Strings.nullToEmpty(getSurname()));
	}
	
	/**
	 * Generated initials instead of fetched initials
	 *
	 * @return
	 */
	public String toInitials()
	{
		StringBuilder personNameBuilder = new StringBuilder();
		for (String middleName : getMiddleNames())
		{
			if (!Strings.isNullOrEmpty(middleName))
			{
				personNameBuilder.append(middleName.toUpperCase()
				                                   .charAt(0));
				personNameBuilder.append(" ");
			}
		}
		return personNameBuilder.toString();
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof UserProfileNamesDTO))
		{
			return false;
		}
		UserProfileNamesDTO<?> that = (UserProfileNamesDTO<?>) o;
		return Objects.equals(getInvolvedParty(), that.getInvolvedParty());
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(getInvolvedParty());
	}
}
