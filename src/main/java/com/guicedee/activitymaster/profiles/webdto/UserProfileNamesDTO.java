package com.guicedee.activitymaster.profiles.webdto;


import com.fasterxml.jackson.annotation.*;
import com.google.common.base.Strings;
import com.guicedee.activitymaster.fsdm.client.services.annotations.*;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.events.IEvent;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.profiles.ProfileSystem;
import com.guicedee.activitymaster.profiles.dto.UserDTO;
import io.smallrye.mutiny.Uni;
import org.apache.commons.lang3.StringUtils;



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
		ISystems<?, ?> system = get(ProfileSystem.class).getSystem(session, getEnterprise());
		UUID identityToken = get(ProfileSystem.class).getSystemToken(session, getEnterprise());

		// Create a list to hold all the name type operations
		List<Uni<?>> nameTypeOperations = new ArrayList<>();

		// Common Name
		nameTypeOperations.add(
			involvedParty.hasInvolvedPartyNameTypes(session, NoClassification.toString(), CommonNameType.toString(), null, system, identityToken)
				.chain(hasCommonName -> {
					if (hasCommonName) {
						return involvedParty.findInvolvedPartyNameType(session, NoClassification.toString(), CommonNameType.toString(), null, system, true, true, identityToken)
							.onItem().invoke(nameType -> setCommonName(nameType.getValue()));
					}
					return Uni.createFrom().nullItem();
				})
		);

		// Birth Name
		nameTypeOperations.add(
			involvedParty.hasInvolvedPartyNameTypes(session, NoClassification.toString(), BirthNameType.toString(), null, system, identityToken)
				.chain(hasBirthName -> {
					if (hasBirthName) {
						return involvedParty.findInvolvedPartyNameType(session, NoClassification.toString(), BirthNameType.toString(), null, system, true, true, identityToken)
							.onItem().invoke(nameType -> setBirthName(nameType.getValue()));
					}
					return Uni.createFrom().nullItem();
				})
		);

		// Legal Name
		nameTypeOperations.add(
			involvedParty.hasInvolvedPartyNameTypes(session, NoClassification.toString(), LegalNameType.toString(), null, system, identityToken)
				.chain(hasLegalName -> {
					if (hasLegalName) {
						return involvedParty.findInvolvedPartyNameType(session, NoClassification.toString(), LegalNameType.toString(), null, system, true, true, identityToken)
							.onItem().invoke(nameType -> setLegalName(nameType.getValue()));
					}
					return Uni.createFrom().nullItem();
				})
		);

		// Salutation Name
		nameTypeOperations.add(
			involvedParty.hasInvolvedPartyNameTypes(session, NoClassification.toString(), SalutationType.toString(), null, system, identityToken)
				.chain(hasSalutationName -> {
					if (hasSalutationName) {
						return involvedParty.findInvolvedPartyNameType(session, NoClassification.toString(), SalutationType.toString(), null, system, true, true, identityToken)
							.onItem().invoke(nameType -> setSalutationName(nameType.getValue()));
					}
					return Uni.createFrom().nullItem();
				})
		);

		// Qualification Name
		nameTypeOperations.add(
			involvedParty.hasInvolvedPartyNameTypes(session, NoClassification.toString(), QualificationType.toString(), null, system, identityToken)
				.chain(hasQualificationName -> {
					if (hasQualificationName) {
						return involvedParty.findInvolvedPartyNameType(session, NoClassification.toString(), QualificationType.toString(), null, system, true, true, identityToken)
							.onItem().invoke(nameType -> setQualificationName(nameType.getValue()));
					}
					return Uni.createFrom().nullItem();
				})
		);

		// Suffix
		nameTypeOperations.add(
			involvedParty.hasInvolvedPartyNameTypes(session, NoClassification.toString(), SuffixType.toString(), null, system, identityToken)
				.chain(hasSuffix -> {
					if (hasSuffix) {
						return involvedParty.findInvolvedPartyNameType(session, NoClassification.toString(), SuffixType.toString(), null, system, true, true, identityToken)
							.onItem().invoke(nameType -> setSuffix(nameType.getValue()));
					}
					return Uni.createFrom().nullItem();
				})
		);

		// Middle Names
		nameTypeOperations.add(
			involvedParty.hasInvolvedPartyNameTypes(session, NoClassification.toString(), MiddleNameType.toString(), null, system, identityToken)
				.chain(hasMiddleNames -> {
					if (hasMiddleNames) {
						getMiddleNames().clear();
						return involvedParty.findInvolvedPartyNameTypesAll(session, NoClassification.toString(), MiddleNameType.toString(), null, system, false, identityToken)
							.onItem().invoke(middleNames -> {
								for (var middleName : middleNames) {
									getMiddleNames().add(middleName.getValue());
								}
							});
					}
					return Uni.createFrom().nullItem();
				})
		);

		// Preferred Name
		nameTypeOperations.add(
			involvedParty.hasInvolvedPartyNameTypes(session, NoClassification.toString(), PreferredNameType.toString(), null, system, identityToken)
				.chain(hasPreferredName -> {
					if (hasPreferredName) {
						return involvedParty.findInvolvedPartyNameType(session, NoClassification.toString(), PreferredNameType.toString(), null, system, true, true, identityToken)
							.onItem().invoke(nameType -> setPreferredName(nameType.getValue()));
					}
					return Uni.createFrom().nullItem();
				})
		);

		// Full Name
		nameTypeOperations.add(
			involvedParty.hasInvolvedPartyNameTypes(session, NoClassification.toString(), FullNameType.toString(), null, system, identityToken)
				.chain(hasFullName -> {
					if (hasFullName) {
						return involvedParty.findInvolvedPartyNameType(session, NoClassification.toString(), FullNameType.toString(), null, system, true, true, identityToken)
							.onItem().invoke(nameType -> setFullName(nameType.getValue()));
					}
					return Uni.createFrom().nullItem();
				})
		);

		// First Name
		nameTypeOperations.add(
			involvedParty.hasInvolvedPartyNameTypes(session, NoClassification.toString(), FirstNameType.toString(), null, system, identityToken)
				.chain(hasFirstName -> {
					if (hasFirstName) {
						return involvedParty.findInvolvedPartyNameType(session, NoClassification.toString(), FirstNameType.toString(), null, system, true, true, identityToken)
							.onItem().invoke(nameType -> setFirstName(nameType.getValue()));
					}
					return Uni.createFrom().nullItem();
				})
		);

		// Surname
		nameTypeOperations.add(
			involvedParty.hasInvolvedPartyNameTypes(session, NoClassification.toString(), SurnameType.toString(), null, system, identityToken)
				.chain(hasSurname -> {
					if (hasSurname) {
						return involvedParty.findInvolvedPartyNameType(session, NoClassification.toString(), SurnameType.toString(), null, system, true, true, identityToken)
							.onItem().invoke(nameType -> setSurname(nameType.getValue()));
					}
					return Uni.createFrom().nullItem();
				})
		);

		// Initials
		nameTypeOperations.add(
			involvedParty.hasInvolvedPartyNameTypes(session, NoClassification.toString(), InitialsType.toString(), null, system, identityToken)
				.chain(hasInitials -> {
					if (hasInitials) {
						return involvedParty.findInvolvedPartyNameType(session, NoClassification.toString(), InitialsType.toString(), null, system, false, false, identityToken)
							.onItem().invoke(nameType -> setInitials(nameType.getValue()));
					}
					return Uni.createFrom().nullItem();
				})
		);

		// Run all name type operations in parallel
		return Uni.combine().all().unis(nameTypeOperations)
			.discardItems()
			.onFailure().invoke(error -> {
				// Log the error
				System.err.println("Error loading name types: " + error.getMessage());
			})
			.map(ignored -> {
				//noinspection unchecked
				return (J) this;
			});
	}

 //@CacheResult(cacheName = "UserProfileNamesDTO", skipGet = true)
	@InvolvedPartyEvent(EventAction.Updated)
	public Uni<Void> update(IInvolvedParty<?, ?> thisInvolvedParty,
	                   @Party("Updated") J updatedParty,
	                   @Party("OnBehalfOf") IInvolvedParty<?, ?> involvedParty)
	{
		ISystems<?, ?> system = get(ProfileSystem.class).getSystem(session, getEnterprise());
		UUID identityToken = get(ProfileSystem.class).getSystemToken(session, getEnterprise());

		IEvent<?, ?> event = com.guicedee.client.IGuiceContext.get(IEvent.class);
		
		// Create a list to hold all the update operations
		List<Uni<?>> updateOperations = new ArrayList<>();
		
		// Add the event operation
		updateOperations.add(
			event.addInvolvedParty(session, thisInvolvedParty, "Updated", null, system, identityToken)
		);

		// Title
		if (!Strings.isNullOrEmpty(updatedParty.getTitle())) {
			updateOperations.add(
				getInvolvedParty().addOrUpdateInvolvedPartyNameType(
						session, NoClassification.toString(), InitialsType, this.title, updatedParty.getTitle(), system, identityToken)
			);
		}
		
		// First Name
		if (!Strings.isNullOrEmpty(updatedParty.getFirstName())) {
			updateOperations.add(
				getInvolvedParty().addOrUpdateInvolvedPartyNameType(
						session, NoClassification.toString(), FirstNameType, this.firstName, updatedParty.getFirstName(), system, identityToken)
			);
		}
		
		// Surname
		if (!Strings.isNullOrEmpty(updatedParty.getSurname())) {
			updateOperations.add(
				getInvolvedParty().addOrUpdateInvolvedPartyNameType(
						session, NoClassification.toString(), SurnameType, this.surname, updatedParty.getSurname(), system, identityToken)
			);
		}
		
		// Initials
		if (!Strings.isNullOrEmpty(updatedParty.getInitials())) {
			updateOperations.add(
				getInvolvedParty().addOrUpdateInvolvedPartyNameType(
						session, NoClassification.toString(), InitialsType, this.initials, updatedParty.getInitials(), system, identityToken)
			);
		}
		
		// Full Name
		if (!Strings.isNullOrEmpty(updatedParty.getFullName())) {
			updateOperations.add(
				getInvolvedParty().addOrUpdateInvolvedPartyNameType(
						session, NoClassification.toString(), FullNameType, this.fullName, updatedParty.getFullName(), system, identityToken)
			);
		}
		
		// Birth Name
		if (!Strings.isNullOrEmpty(updatedParty.getBirthName())) {
			updateOperations.add(
				getInvolvedParty().addOrUpdateInvolvedPartyNameType(
						session, NoClassification.toString(), BirthNameType, this.birthName, updatedParty.getBirthName(), system, identityToken)
			);
		}
		
		// Legal Name
		if (!Strings.isNullOrEmpty(updatedParty.getLegalName())) {
			updateOperations.add(
				getInvolvedParty().addOrUpdateInvolvedPartyNameType(
						session, NoClassification.toString(), LegalNameType, this.legalName, updatedParty.getLegalName(), system, identityToken)
			);
		}
		
		// Salutation Name
		if (!Strings.isNullOrEmpty(updatedParty.getSalutationName())) {
			updateOperations.add(
				getInvolvedParty().addOrUpdateInvolvedPartyNameType(
						session, NoClassification.toString(), SalutationType, this.salutationName, updatedParty.getSalutationName(), system, identityToken)
			);
		}
		
		// Qualification Name
		if (!Strings.isNullOrEmpty(updatedParty.getQualificationName())) {
			updateOperations.add(
				getInvolvedParty().addOrUpdateInvolvedPartyNameType(
						session, NoClassification.toString(), QualificationType, this.qualificationName, updatedParty.getQualificationName(), system, identityToken)
			);
		}
		
		// Suffix
		if (!Strings.isNullOrEmpty(updatedParty.getSuffix())) {
			updateOperations.add(
				getInvolvedParty().addOrUpdateInvolvedPartyNameType(
						session, NoClassification.toString(), SuffixType, this.suffix, updatedParty.getSuffix(), system, identityToken)
			);
		}
		
		// Preferred Name
		if (!Strings.isNullOrEmpty(updatedParty.getPreferredName())) {
			updateOperations.add(
				getInvolvedParty().addOrUpdateInvolvedPartyNameType(
						session, NoClassification.toString(), PreferredNameType, this.preferredName, updatedParty.getPreferredName(), system, identityToken)
			);
		}
		
		// Middle Names
		if (!updatedParty.getMiddleNames().isEmpty()) {
			// Create a list to hold all middle name operations
			List<Uni<?>> middleNameOperations = new ArrayList<>();
			
			// Add all new middle names
			for (String middleName : updatedParty.getMiddleNames()) {
				middleNameOperations.add(
					getInvolvedParty().addOrUpdateInvolvedPartyNameType(
							session, NoClassification.toString(), MiddleNameType.toString(), null, middleName, system, identityToken)
				);
			}
			
			// Run all middle name operations in parallel
			Uni<Void> middleNamesOperation = Uni.combine().all().unis(middleNameOperations)
				.discardItems();
			
			updateOperations.add(middleNamesOperation);
		}
		
		// Run all update operations in parallel
		return Uni.combine().all().unis(updateOperations)
			.discardItems()
			.onFailure().invoke(error -> {
				// Log the error
				System.err.println("Error updating name types: " + error.getMessage());
			});
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
