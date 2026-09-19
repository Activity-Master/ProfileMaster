package com.guicedee.activitymaster.profiles.implementations.graphql;

import com.guicedee.activitymaster.fsdm.client.services.SessionUtils;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.profiles.services.interfaces.IProfileService;
import com.guicedee.activitymaster.profiles.webdto.ComprehensiveProfileDTO;
import com.guicedee.client.IGuiceContext;
import com.guicedee.vertx.graphql.services.IGraphQLSchemaProvider;
import graphql.schema.DataFetcher;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Future;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.UUID;

/**
 * Contributes the strongly-typed {@code ComprehensiveProfile} GraphQL type (and its queries) to the shared
 * ActivityMaster GraphQL schema.
 *
 * <p>This provider is discovered automatically (via {@link java.util.ServiceLoader}) whenever the
 * profiles module is on the class/module path, so the core service registry exposes the profile
 * data structures without any change to the core itself. The {@code profile} / {@code comprehensiveProfile}
 * queries are resolved through {@link IProfileService#getProfile} inside the canonical
 * {@link SessionUtils#withActivityMaster} security/session context, returning a fully-hydrated
 * {@link ComprehensiveProfileDTO} read straight from the ActivityMaster warehouse.</p>
 *
 * <p>The {@code Query} root is shared with the core {@code FsdmGraphQLSchemaProvider}; this provider
 * therefore {@code extend}s it rather than redefining it.</p>
 */
public class ProfileGraphQLSchemaProvider implements IGraphQLSchemaProvider<ProfileGraphQLSchemaProvider>
{
	private static final String SDL =
			"\"A comprehensive individual profile stored within ActivityMaster.\"\n"
			+ "type ComprehensiveProfile {\n"
			+ "    \"The unique identifier of this profile (the backing involved-party ID).\"\n"
			+ "    profileId: String\n"
			+ "    \"The owning enterprise name.\"\n"
			+ "    enterpriseName: String\n"
			+ "\n"
			+ "    \"Title (e.g. Mr, Mrs, Dr, Prof).\"\n"
			+ "    title: String\n"
			+ "    \"First / given name.\"\n"
			+ "    firstName: String\n"
			+ "    \"Middle name(s).\"\n"
			+ "    middleName: String\n"
			+ "    \"Surname / last name / family name.\"\n"
			+ "    surname: String\n"
			+ "    \"Preferred or nickname.\"\n"
			+ "    preferredName: String\n"
			+ "    \"Full display name.\"\n"
			+ "    fullName: String\n"
			+ "    \"Full legal name.\"\n"
			+ "    legalName: String\n"
			+ "    \"Birth name (e.g. maiden name).\"\n"
			+ "    birthName: String\n"
			+ "    \"Commonly known as name.\"\n"
			+ "    commonName: String\n"
			+ "    \"Initials.\"\n"
			+ "    initials: String\n"
			+ "    \"Suffix (e.g. Jr, III, PhD).\"\n"
			+ "    suffix: String\n"
			+ "    \"Post-nominal qualification letters (e.g. BCom, CA(SA)).\"\n"
			+ "    qualification: String\n"
			+ "\n"
			+ "    \"Date of birth (ISO-8601 string: YYYY-MM-DD).\"\n"
			+ "    dateOfBirth: String\n"
			+ "    \"Gender or gender identity.\"\n"
			+ "    gender: String\n"
			+ "    \"Preferred pronouns (e.g. they/them, she/her, he/him).\"\n"
			+ "    pronouns: String\n"
			+ "    \"Marital status (e.g. Single, Married, Divorced).\"\n"
			+ "    maritalStatus: String\n"
			+ "    \"Nationality / citizenship.\"\n"
			+ "    nationality: String\n"
			+ "    \"Country of birth.\"\n"
			+ "    countryOfBirth: String\n"
			+ "    \"City/town of birth.\"\n"
			+ "    placeOfBirth: String\n"
			+ "    \"Ethnic background.\"\n"
			+ "    ethnicity: String\n"
			+ "    \"Religion or religious affiliation.\"\n"
			+ "    religion: String\n"
			+ "    \"Primary / home language.\"\n"
			+ "    homeLanguage: String\n"
			+ "    \"Spoken languages (comma-separated or formatted).\"\n"
			+ "    spokenLanguages: String\n"
			+ "    \"Blood type (e.g. O+, A-).\"\n"
			+ "    bloodType: String\n"
			+ "\n"
			+ "    \"Primary contact email address.\"\n"
			+ "    primaryEmail: String\n"
			+ "    \"Secondary / recovery email address.\"\n"
			+ "    secondaryEmail: String\n"
			+ "    \"Mobile / cell phone number.\"\n"
			+ "    mobileNumber: String\n"
			+ "    \"Home landline phone number.\"\n"
			+ "    homeNumber: String\n"
			+ "    \"Work / office phone number.\"\n"
			+ "    workNumber: String\n"
			+ "    \"Fax number.\"\n"
			+ "    faxNumber: String\n"
			+ "    \"Personal or professional website URL.\"\n"
			+ "    website: String\n"
			+ "\n"
			+ "    \"Residential / street address.\"\n"
			+ "    residentialAddress: String\n"
			+ "    \"Postal / mailing address (P.O. Box, etc.).\"\n"
			+ "    postalAddress: String\n"
			+ "    \"City / town.\"\n"
			+ "    city: String\n"
			+ "    \"State / province / region.\"\n"
			+ "    province: String\n"
			+ "    \"Postal / ZIP code.\"\n"
			+ "    postalCode: String\n"
			+ "    \"Country.\"\n"
			+ "    country: String\n"
			+ "\n"
			+ "    \"Occupation / profession.\"\n"
			+ "    occupation: String\n"
			+ "    \"Job title / role.\"\n"
			+ "    jobTitle: String\n"
			+ "    \"Current employer or company name.\"\n"
			+ "    employer: String\n"
			+ "    \"Department or organizational unit.\"\n"
			+ "    department: String\n"
			+ "    \"Employment status (e.g. Full-Time, Part-Time, Contractor, Unemployed, Retired).\"\n"
			+ "    employmentStatus: String\n"
			+ "    \"Industry sector.\"\n"
			+ "    industry: String\n"
			+ "    \"Years of professional experience.\"\n"
			+ "    yearsOfExperience: String\n"
			+ "    \"Work email address.\"\n"
			+ "    workEmail: String\n"
			+ "    \"Employee number or staff ID.\"\n"
			+ "    employeeNumber: String\n"
			+ "    \"Reporting manager name or role.\"\n"
			+ "    manager: String\n"
			+ "    \"Office location or branch.\"\n"
			+ "    officeLocation: String\n"
			+ "\n"
			+ "    \"Highest level of education / qualification achieved.\"\n"
			+ "    highestQualification: String\n"
			+ "    \"Field / major of study.\"\n"
			+ "    fieldOfStudy: String\n"
			+ "    \"Educational institution attended.\"\n"
			+ "    institution: String\n"
			+ "    \"Year of graduation.\"\n"
			+ "    graduationYear: String\n"
			+ "\n"
			+ "    \"National identity document / ID number.\"\n"
			+ "    idNumber: String\n"
			+ "    \"Passport number.\"\n"
			+ "    passportNumber: String\n"
			+ "    \"Tax / revenue identification number.\"\n"
			+ "    taxNumber: String\n"
			+ "    \"Driver's license number.\"\n"
			+ "    driversLicenseNumber: String\n"
			+ "\n"
			+ "    \"LinkedIn profile URL.\"\n"
			+ "    linkedIn: String\n"
			+ "    \"Twitter / X profile handle or URL.\"\n"
			+ "    twitter: String\n"
			+ "    \"Facebook profile URL.\"\n"
			+ "    facebook: String\n"
			+ "    \"Instagram handle or profile URL.\"\n"
			+ "    instagram: String\n"
			+ "    \"GitHub profile URL.\"\n"
			+ "    github: String\n"
			+ "\n"
			+ "    \"Medical aid / health insurance provider name.\"\n"
			+ "    medicalAidName: String\n"
			+ "    \"Medical aid / health insurance policy/membership number.\"\n"
			+ "    medicalAidNumber: String\n"
			+ "    \"Dietary requirements or restrictions.\"\n"
			+ "    dietaryRequirements: String\n"
			+ "    \"Disability status or accommodation needs.\"\n"
			+ "    disabilityStatus: String\n"
			+ "\n"
			+ "    \"Emergency contact full name.\"\n"
			+ "    emergencyContactName: String\n"
			+ "    \"Emergency contact phone number.\"\n"
			+ "    emergencyContactNumber: String\n"
			+ "    \"Relationship of emergency contact (e.g. Spouse, Parent, Sibling, Friend).\"\n"
			+ "    emergencyContactRelationship: String\n"
			+ "\n"
			+ "    \"Preferred method of contact (e.g. Email, Mobile, SMS).\"\n"
			+ "    preferredContactMethod: String\n"
			+ "    \"Time zone preference.\"\n"
			+ "    timeZone: String\n"
			+ "    \"Locale / language preference (e.g. en-US, en-ZA).\"\n"
			+ "    locale: String\n"
			+ "    \"Short biography or personal summary.\"\n"
			+ "    biography: String\n"
			+ "    \"Avatar / profile picture image URL.\"\n"
			+ "    avatarUrl: String\n"
			+ "    \"Internal administrator notes.\"\n"
			+ "    notes: String\n"
			+ "}\n"
			+ "\n"
			+ "extend type Query {\n"
			+ "    \"Resolves a comprehensive individual profile by its ID within an enterprise/system scope.\"\n"
			+ "    profile(enterprise: String!, system: String!, profileId: String!): ComprehensiveProfile\n"
			+ "    \"Resolves a comprehensive individual profile by its ID within an enterprise/system scope.\"\n"
			+ "    comprehensiveProfile(enterprise: String!, system: String!, profileId: String!): ComprehensiveProfile\n"
			+ "}\n";

	@Override
	public TypeDefinitionRegistry getTypeDefinitions()
	{
		return new SchemaParser().parse(SDL);
	}

	@Override
	public RuntimeWiring.Builder configureWiring(RuntimeWiring.Builder builder)
	{
		return builder
				.type("Query", q -> q
						.dataFetcher("profile", profileFetcher())
						.dataFetcher("comprehensiveProfile", profileFetcher()))
				.type("ComprehensiveProfile", t -> t
						.dataFetcher("profileId", env -> {
							ComprehensiveProfileDTO p = env.getSource();
							return p == null || p.getProfileId() == null ? null : p.getProfileId().toString();
						}));
	}

	/**
	 * Builds the data fetcher for the {@code profile} / {@code comprehensiveProfile} query.
	 * Execution runs inside the canonical Activity Master security/session context and the resulting
	 * Mutiny {@link Uni} is bridged to a Vert.x {@link Future} so the auto-installed
	 * {@code VertxFutureAdapter} resolves it.
	 */
	private DataFetcher<Future<ComprehensiveProfileDTO>> profileFetcher()
	{
		return env -> {
			String enterprise = env.getArgument("enterprise");
			String system = env.getArgument("system");
			String profileIdStr = env.getArgument("profileId");
			UUID profileId = UUID.fromString(profileIdStr);

			Uni<ComprehensiveProfileDTO> uni = SessionUtils.withActivityMaster(enterprise, system, tuple -> {
				Mutiny.StatelessSession session = tuple.getItem1();
				IEnterprise<?, ?> ent = tuple.getItem2();
				IProfileService<?> service = IGuiceContext.get(IProfileService.class);
				return service.getProfile(session, ent, profileId);
			});

			return Future.fromCompletionStage(uni.subscribeAsCompletionStage());
		};
	}
}
