package com.guicedee.activitymaster.profiles.webdto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.guicedee.activitymaster.fsdm.client.services.classifications.types.NameTypes;
import com.guicedee.activitymaster.profiles.enumerations.ProfileAttributes;
import com.guicedee.modules.services.jsonrepresentation.IJsonRepresentation;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

/**
 * A comprehensive, REST-friendly representation of an individual's full profile.
 *
 * <p>This is the transport object exchanged by {@code ProfileRestService} and
 * {@code ProfileRestClients}. It carries the complete set of profile fields — names, demographics,
 * contact details, address, occupation/employment, education, identification, social presence,
 * health, emergency contact and free-text meta — and knows how to project itself onto, and rebuild
 * itself from, the underlying ActivityMaster storage primitives:</p>
 *
 * <ul>
 *     <li><b>Names</b> map to the FSDM {@link NameTypes} mechanism — see {@link #toNameValues()}.</li>
 *     <li><b>Everything else</b> maps to {@link ProfileAttributes} classifications — see
 *         {@link #toAttributeValues()} / {@link #applyAttributeValues(Map)}.</li>
 * </ul>
 *
 * <p>Any field not covered by an explicit getter/setter can still be carried through the
 * {@link #additionalAttributes} bag, which is merged into the attribute map on write and populated
 * with any unrecognised classification on read.</p>
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class ComprehensiveProfileDTO
		implements Serializable, IJsonRepresentation<ComprehensiveProfileDTO>
{
	@Serial
	private static final long serialVersionUID = 1L;

	/** The unique identifier of this profile (the backing involved-party id). */
	private UUID profileId;

	/** The owning enterprise name (used by the REST client for routing/convenience). */
	private String enterpriseName;

	// -------------------------------------------------------------------------------------------
	//  Names (stored via NameTypes)
	// -------------------------------------------------------------------------------------------
	private String title;
	private String firstName;
	private String middleName;
	private String surname;
	private String preferredName;
	private String fullName;
	private String legalName;
	private String birthName;
	private String commonName;
	private String initials;
	private String suffix;
	private String qualification;

	// -------------------------------------------------------------------------------------------
	//  Demographics
	// -------------------------------------------------------------------------------------------
	private String dateOfBirth;
	private String gender;
	private String pronouns;
	private String maritalStatus;
	private String nationality;
	private String countryOfBirth;
	private String placeOfBirth;
	private String ethnicity;
	private String religion;
	private String homeLanguage;
	private String spokenLanguages;
	private String bloodType;

	// -------------------------------------------------------------------------------------------
	//  Contact
	// -------------------------------------------------------------------------------------------
	private String primaryEmail;
	private String secondaryEmail;
	private String mobileNumber;
	private String homeNumber;
	private String workNumber;
	private String faxNumber;
	private String website;

	// -------------------------------------------------------------------------------------------
	//  Address
	// -------------------------------------------------------------------------------------------
	private String residentialAddress;
	private String postalAddress;
	private String city;
	private String province;
	private String postalCode;
	private String country;

	// -------------------------------------------------------------------------------------------
	//  Occupation / Employment
	// -------------------------------------------------------------------------------------------
	private String occupation;
	private String jobTitle;
	private String employer;
	private String department;
	private String employmentStatus;
	private String industry;
	private String yearsOfExperience;
	private String workEmail;
	private String employeeNumber;
	private String manager;
	private String officeLocation;

	// -------------------------------------------------------------------------------------------
	//  Education
	// -------------------------------------------------------------------------------------------
	private String highestQualification;
	private String fieldOfStudy;
	private String institution;
	private String graduationYear;

	// -------------------------------------------------------------------------------------------
	//  Identification
	// -------------------------------------------------------------------------------------------
	private String idNumber;
	private String passportNumber;
	private String taxNumber;
	private String driversLicenseNumber;

	// -------------------------------------------------------------------------------------------
	//  Social / Online presence
	// -------------------------------------------------------------------------------------------
	private String linkedIn;
	private String twitter;
	private String facebook;
	private String instagram;
	private String github;

	// -------------------------------------------------------------------------------------------
	//  Health
	// -------------------------------------------------------------------------------------------
	private String medicalAidName;
	private String medicalAidNumber;
	private String dietaryRequirements;
	private String disabilityStatus;

	// -------------------------------------------------------------------------------------------
	//  Emergency contact
	// -------------------------------------------------------------------------------------------
	private String emergencyContactName;
	private String emergencyContactNumber;
	private String emergencyContactRelationship;

	// -------------------------------------------------------------------------------------------
	//  Meta / Preferences
	// -------------------------------------------------------------------------------------------
	private String preferredContactMethod;
	private String timeZone;
	private String locale;
	private String biography;
	private String avatarUrl;
	private String notes;

	/** Extensibility bag for any attribute not modelled by an explicit field. */
	private Map<String, String> additionalAttributes = new LinkedHashMap<>();

	// -------------------------------------------------------------------------------------------
	//  Name projection
	// -------------------------------------------------------------------------------------------

	/**
	 * Projects the populated name fields onto their {@link NameTypes}. Only non-null values are
	 * included, so a partial update never blanks an existing name.
	 */
	public Map<NameTypes, String> toNameValues()
	{
		Map<NameTypes, String> names = new LinkedHashMap<>();
		put(names, NameTypes.SalutationType, title);
		put(names, NameTypes.FirstNameType, firstName);
		put(names, NameTypes.MiddleNameType, middleName);
		put(names, NameTypes.SurnameType, surname);
		put(names, NameTypes.PreferredNameType, preferredName);
		put(names, NameTypes.FullNameType, fullName);
		put(names, NameTypes.LegalNameType, legalName);
		put(names, NameTypes.BirthNameType, birthName);
		put(names, NameTypes.CommonNameType, commonName);
		put(names, NameTypes.InitialsType, initials);
		put(names, NameTypes.SuffixType, suffix);
		put(names, NameTypes.QualificationType, qualification);
		return names;
	}

	/** Applies a single name value, resolved from its {@link NameTypes}, onto the matching field. */
	public ComprehensiveProfileDTO applyName(NameTypes nameType, String value)
	{
		if (nameType == null)
		{
			return this;
		}
		switch (nameType)
		{
			case SalutationType -> this.title = value;
			case FirstNameType -> this.firstName = value;
			case MiddleNameType -> this.middleName = value;
			case SurnameType -> this.surname = value;
			case PreferredNameType -> this.preferredName = value;
			case FullNameType -> this.fullName = value;
			case LegalNameType -> this.legalName = value;
			case BirthNameType -> this.birthName = value;
			case CommonNameType -> this.commonName = value;
			case InitialsType -> this.initials = value;
			case SuffixType -> this.suffix = value;
			case QualificationType -> this.qualification = value;
		}
		return this;
	}

	// -------------------------------------------------------------------------------------------
	//  Attribute projection
	// -------------------------------------------------------------------------------------------

	/**
	 * Projects the populated attribute fields onto their {@link ProfileAttributes} classification
	 * names. Only non-null values are included. Any entries in {@link #additionalAttributes} are
	 * merged in last.
	 */
	public Map<String, String> toAttributeValues()
	{
		Map<String, String> values = new LinkedHashMap<>();
		// Demographics
		put(values, ProfileAttributes.DateOfBirth, dateOfBirth);
		put(values, ProfileAttributes.Gender, gender);
		put(values, ProfileAttributes.Pronouns, pronouns);
		put(values, ProfileAttributes.MaritalStatus, maritalStatus);
		put(values, ProfileAttributes.Nationality, nationality);
		put(values, ProfileAttributes.CountryOfBirth, countryOfBirth);
		put(values, ProfileAttributes.PlaceOfBirth, placeOfBirth);
		put(values, ProfileAttributes.Ethnicity, ethnicity);
		put(values, ProfileAttributes.Religion, religion);
		put(values, ProfileAttributes.HomeLanguage, homeLanguage);
		put(values, ProfileAttributes.SpokenLanguages, spokenLanguages);
		put(values, ProfileAttributes.BloodType, bloodType);
		// Contact
		put(values, ProfileAttributes.PrimaryEmail, primaryEmail);
		put(values, ProfileAttributes.SecondaryEmail, secondaryEmail);
		put(values, ProfileAttributes.MobileNumber, mobileNumber);
		put(values, ProfileAttributes.HomeNumber, homeNumber);
		put(values, ProfileAttributes.WorkNumber, workNumber);
		put(values, ProfileAttributes.FaxNumber, faxNumber);
		put(values, ProfileAttributes.Website, website);
		// Address
		put(values, ProfileAttributes.ResidentialAddress, residentialAddress);
		put(values, ProfileAttributes.PostalAddress, postalAddress);
		put(values, ProfileAttributes.City, city);
		put(values, ProfileAttributes.Province, province);
		put(values, ProfileAttributes.PostalCode, postalCode);
		put(values, ProfileAttributes.Country, country);
		// Occupation / Employment
		put(values, ProfileAttributes.Occupation, occupation);
		put(values, ProfileAttributes.JobTitle, jobTitle);
		put(values, ProfileAttributes.Employer, employer);
		put(values, ProfileAttributes.Department, department);
		put(values, ProfileAttributes.EmploymentStatus, employmentStatus);
		put(values, ProfileAttributes.Industry, industry);
		put(values, ProfileAttributes.YearsOfExperience, yearsOfExperience);
		put(values, ProfileAttributes.WorkEmail, workEmail);
		put(values, ProfileAttributes.EmployeeNumber, employeeNumber);
		put(values, ProfileAttributes.Manager, manager);
		put(values, ProfileAttributes.OfficeLocation, officeLocation);
		// Education
		put(values, ProfileAttributes.HighestQualification, highestQualification);
		put(values, ProfileAttributes.FieldOfStudy, fieldOfStudy);
		put(values, ProfileAttributes.Institution, institution);
		put(values, ProfileAttributes.GraduationYear, graduationYear);
		// Identification
		put(values, ProfileAttributes.IdNumber, idNumber);
		put(values, ProfileAttributes.PassportNumber, passportNumber);
		put(values, ProfileAttributes.TaxNumber, taxNumber);
		put(values, ProfileAttributes.DriversLicenseNumber, driversLicenseNumber);
		// Social
		put(values, ProfileAttributes.LinkedIn, linkedIn);
		put(values, ProfileAttributes.Twitter, twitter);
		put(values, ProfileAttributes.Facebook, facebook);
		put(values, ProfileAttributes.Instagram, instagram);
		put(values, ProfileAttributes.GitHub, github);
		// Health
		put(values, ProfileAttributes.MedicalAidName, medicalAidName);
		put(values, ProfileAttributes.MedicalAidNumber, medicalAidNumber);
		put(values, ProfileAttributes.DietaryRequirements, dietaryRequirements);
		put(values, ProfileAttributes.DisabilityStatus, disabilityStatus);
		// Emergency contact
		put(values, ProfileAttributes.EmergencyContactName, emergencyContactName);
		put(values, ProfileAttributes.EmergencyContactNumber, emergencyContactNumber);
		put(values, ProfileAttributes.EmergencyContactRelationship, emergencyContactRelationship);
		// Meta
		put(values, ProfileAttributes.PreferredContactMethod, preferredContactMethod);
		put(values, ProfileAttributes.TimeZone, timeZone);
		put(values, ProfileAttributes.Locale, locale);
		put(values, ProfileAttributes.Biography, biography);
		put(values, ProfileAttributes.AvatarUrl, avatarUrl);
		put(values, ProfileAttributes.Notes, notes);

		if (additionalAttributes != null)
		{
			additionalAttributes.forEach((k, v) -> {
				if (k != null && v != null)
				{
					values.putIfAbsent(k, v);
				}
			});
		}
		return values;
	}

	/**
	 * Rebuilds the attribute fields from a {@code classification-name -> value} map (as returned by
	 * the warehouse read). Classifications that are not modelled profile attributes are ignored.
	 */
	public ComprehensiveProfileDTO applyAttributeValues(Map<String, String> values)
	{
		if (values == null || values.isEmpty())
		{
			return this;
		}
		for (Map.Entry<String, String> entry : values.entrySet())
		{
			ProfileAttributes attribute = resolve(entry.getKey());
			if (attribute == null)
			{
				continue;
			}
			String value = entry.getValue();
			switch (attribute)
			{
				case DateOfBirth -> this.dateOfBirth = value;
				case Gender -> this.gender = value;
				case Pronouns -> this.pronouns = value;
				case MaritalStatus -> this.maritalStatus = value;
				case Nationality -> this.nationality = value;
				case CountryOfBirth -> this.countryOfBirth = value;
				case PlaceOfBirth -> this.placeOfBirth = value;
				case Ethnicity -> this.ethnicity = value;
				case Religion -> this.religion = value;
				case HomeLanguage -> this.homeLanguage = value;
				case SpokenLanguages -> this.spokenLanguages = value;
				case BloodType -> this.bloodType = value;
				case PrimaryEmail -> this.primaryEmail = value;
				case SecondaryEmail -> this.secondaryEmail = value;
				case MobileNumber -> this.mobileNumber = value;
				case HomeNumber -> this.homeNumber = value;
				case WorkNumber -> this.workNumber = value;
				case FaxNumber -> this.faxNumber = value;
				case Website -> this.website = value;
				case ResidentialAddress -> this.residentialAddress = value;
				case PostalAddress -> this.postalAddress = value;
				case City -> this.city = value;
				case Province -> this.province = value;
				case PostalCode -> this.postalCode = value;
				case Country -> this.country = value;
				case Occupation -> this.occupation = value;
				case JobTitle -> this.jobTitle = value;
				case Employer -> this.employer = value;
				case Department -> this.department = value;
				case EmploymentStatus -> this.employmentStatus = value;
				case Industry -> this.industry = value;
				case YearsOfExperience -> this.yearsOfExperience = value;
				case WorkEmail -> this.workEmail = value;
				case EmployeeNumber -> this.employeeNumber = value;
				case Manager -> this.manager = value;
				case OfficeLocation -> this.officeLocation = value;
				case HighestQualification -> this.highestQualification = value;
				case FieldOfStudy -> this.fieldOfStudy = value;
				case Institution -> this.institution = value;
				case GraduationYear -> this.graduationYear = value;
				case IdNumber -> this.idNumber = value;
				case PassportNumber -> this.passportNumber = value;
				case TaxNumber -> this.taxNumber = value;
				case DriversLicenseNumber -> this.driversLicenseNumber = value;
				case LinkedIn -> this.linkedIn = value;
				case Twitter -> this.twitter = value;
				case Facebook -> this.facebook = value;
				case Instagram -> this.instagram = value;
				case GitHub -> this.github = value;
				case MedicalAidName -> this.medicalAidName = value;
				case MedicalAidNumber -> this.medicalAidNumber = value;
				case DietaryRequirements -> this.dietaryRequirements = value;
				case DisabilityStatus -> this.disabilityStatus = value;
				case EmergencyContactName -> this.emergencyContactName = value;
				case EmergencyContactNumber -> this.emergencyContactNumber = value;
				case EmergencyContactRelationship -> this.emergencyContactRelationship = value;
				case PreferredContactMethod -> this.preferredContactMethod = value;
				case TimeZone -> this.timeZone = value;
				case Locale -> this.locale = value;
				case Biography -> this.biography = value;
				case AvatarUrl -> this.avatarUrl = value;
				case Notes -> this.notes = value;
			}
		}
		return this;
	}

	private static void put(Map<NameTypes, String> map, NameTypes key, String value)
	{
		if (value != null)
		{
			map.put(key, value);
		}
	}

	private static void put(Map<String, String> map, ProfileAttributes key, String value)
	{
		if (value != null)
		{
			map.put(key.name(), value);
		}
	}

	private static ProfileAttributes resolve(String name)
	{
		if (name == null)
		{
			return null;
		}
		try
		{
			return ProfileAttributes.valueOf(name);
		}
		catch (IllegalArgumentException notAnAttribute)
		{
			return null;
		}
	}
}


