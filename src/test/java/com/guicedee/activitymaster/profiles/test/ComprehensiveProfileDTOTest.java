package com.guicedee.activitymaster.profiles.test;

import com.guicedee.activitymaster.fsdm.client.services.classifications.types.NameTypes;
import com.guicedee.activitymaster.profiles.enumerations.ProfileAttributes;
import com.guicedee.activitymaster.profiles.webdto.ComprehensiveProfileDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure (no-database) unit tests for {@link ComprehensiveProfileDTO}: verifies that the comprehensive
 * field set projects correctly onto the FSDM {@link NameTypes} / {@link ProfileAttributes} storage
 * primitives and rebuilds losslessly from the warehouse read maps.
 */
class ComprehensiveProfileDTOTest
{
	private static ComprehensiveProfileDTO sample()
	{
		ComprehensiveProfileDTO p = new ComprehensiveProfileDTO();
		p.setProfileId(UUID.randomUUID());
		p.setEnterpriseName("Acme");
		// names
		p.setTitle("Dr");
		p.setFirstName("Ada");
		p.setMiddleName("Augusta");
		p.setSurname("Lovelace");
		p.setPreferredName("Ada");
		p.setInitials("A.A.");
		// comprehensive attributes
		p.setOccupation("Mathematician");
		p.setJobTitle("Analyst");
		p.setEmployer("Analytical Engines Ltd");
		p.setPrimaryEmail("ada@example.com");
		p.setMobileNumber("+27 11 555 0100");
		p.setNationality("British");
		p.setDateOfBirth("1815-12-10");
		p.setCity("London");
		p.setCountry("United Kingdom");
		p.setLinkedIn("https://linkedin.com/in/ada");
		p.setHighestQualification("Mathematics");
		return p;
	}

	@Test
	@DisplayName("Name fields project onto the matching NameTypes")
	void nameValuesProjection()
	{
		Map<NameTypes, String> names = sample().toNameValues();
		assertEquals("Dr", names.get(NameTypes.SalutationType));
		assertEquals("Ada", names.get(NameTypes.FirstNameType));
		assertEquals("Augusta", names.get(NameTypes.MiddleNameType));
		assertEquals("Lovelace", names.get(NameTypes.SurnameType));
		assertEquals("Ada", names.get(NameTypes.PreferredNameType));
		assertEquals("A.A.", names.get(NameTypes.InitialsType));
		// unset names are not emitted (so a partial update never blanks a stored value)
		assertFalse(names.containsKey(NameTypes.LegalNameType));
	}

	@Test
	@DisplayName("Attribute fields project onto the matching ProfileAttributes classification names")
	void attributeValuesProjection()
	{
		Map<String, String> values = sample().toAttributeValues();
		assertEquals("Mathematician", values.get(ProfileAttributes.Occupation.name()));
		assertEquals("Analyst", values.get(ProfileAttributes.JobTitle.name()));
		assertEquals("Analytical Engines Ltd", values.get(ProfileAttributes.Employer.name()));
		assertEquals("ada@example.com", values.get(ProfileAttributes.PrimaryEmail.name()));
		assertEquals("+27 11 555 0100", values.get(ProfileAttributes.MobileNumber.name()));
		assertEquals("British", values.get(ProfileAttributes.Nationality.name()));
		assertEquals("1815-12-10", values.get(ProfileAttributes.DateOfBirth.name()));
		assertEquals("London", values.get(ProfileAttributes.City.name()));
		assertEquals("United Kingdom", values.get(ProfileAttributes.Country.name()));
		assertEquals("https://linkedin.com/in/ada", values.get(ProfileAttributes.LinkedIn.name()));
		// unset attributes are not emitted
		assertFalse(values.containsKey(ProfileAttributes.PassportNumber.name()));
	}

	@Test
	@DisplayName("A profile round-trips losslessly through its storage maps")
	void roundTrip()
	{
		ComprehensiveProfileDTO original = sample();

		ComprehensiveProfileDTO rebuilt = new ComprehensiveProfileDTO();
		rebuilt.setProfileId(original.getProfileId());
		rebuilt.setEnterpriseName(original.getEnterpriseName());
		rebuilt.applyAttributeValues(original.toAttributeValues());
		original.toNameValues().forEach(rebuilt::applyName);

		assertEquals(original.getFirstName(), rebuilt.getFirstName());
		assertEquals(original.getSurname(), rebuilt.getSurname());
		assertEquals(original.getMiddleName(), rebuilt.getMiddleName());
		assertEquals(original.getTitle(), rebuilt.getTitle());
		assertEquals(original.getInitials(), rebuilt.getInitials());
		assertEquals(original.getOccupation(), rebuilt.getOccupation());
		assertEquals(original.getEmployer(), rebuilt.getEmployer());
		assertEquals(original.getPrimaryEmail(), rebuilt.getPrimaryEmail());
		assertEquals(original.getMobileNumber(), rebuilt.getMobileNumber());
		assertEquals(original.getNationality(), rebuilt.getNationality());
		assertEquals(original.getDateOfBirth(), rebuilt.getDateOfBirth());
		assertEquals(original.getCity(), rebuilt.getCity());
		assertEquals(original.getCountry(), rebuilt.getCountry());
		assertEquals(original.getLinkedIn(), rebuilt.getLinkedIn());
		assertEquals(original.getHighestQualification(), rebuilt.getHighestQualification());
	}

	@Test
	@DisplayName("Unrecognised classifications are ignored on read")
	void ignoresUnknownClassifications()
	{
		ComprehensiveProfileDTO p = new ComprehensiveProfileDTO();
		p.applyAttributeValues(Map.of(
				"UserRoles", "Administrator",
				ProfileAttributes.Occupation.name(), "Engineer"));
		assertEquals("Engineer", p.getOccupation());
		// a non-profile classification does not leak into a modelled field
		assertNull(p.getNotes());
	}
}

