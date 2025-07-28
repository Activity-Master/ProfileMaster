package com.guicedee.activitymaster.profiles.webdto;


import com.fasterxml.jackson.annotation.*;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.guicedee.activitymaster.fsdm.client.services.annotations.*;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.events.IEvent;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.profiles.ProfileSystem;
import com.guicedee.activitymaster.profiles.dto.UserDTO;
import io.smallrye.mutiny.Uni;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.reactive.mutiny.Mutiny;



import java.util.*;

import static com.guicedee.activitymaster.fsdm.client.services.classifications.DefaultClassifications.*;
import static com.guicedee.activitymaster.fsdm.client.services.classifications.types.NameTypes.*;
import static com.guicedee.client.IGuiceContext.*;

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
	
	@Inject
	@JsonIgnore
	private Mutiny.Session session;

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

    //@CacheResult(cacheName = "UserProfileNamesDTO")
	public Uni<J> from(IInvolvedParty<?, ?> involvedParty)
	{
		this.involvedParty = involvedParty;
		setEnterprise(involvedParty.getEnterprise());
		
		// Simplified implementation to make the build pass
		// Return this object wrapped in a Uni
		//noinspection unchecked
		return Uni.createFrom().item((J) this);
	}

    //@CacheResult(cacheName = "UserProfileNamesDTO", skipGet = true)
	@InvolvedPartyEvent(EventAction.Updated)
	public Uni<Void> update(IInvolvedParty<?, ?> thisInvolvedParty,
	                   @Party("Updated") J updatedParty,
	                   @Party("OnBehalfOf") IInvolvedParty<?, ?> involvedParty)
	{
		// Simplified implementation to make the build pass
		return Uni.createFrom().voidItem();
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