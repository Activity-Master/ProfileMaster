package com.guicedee.activitymaster.profiles;

import com.google.inject.Inject;
import com.guicedee.activitymaster.fsdm.client.services.IActivityMasterService;
import com.guicedee.activitymaster.fsdm.client.services.IPasswordsService;
import com.guicedee.activitymaster.fsdm.client.services.ReactiveTransactionUtil;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;
import com.guicedee.activitymaster.profiles.services.interfaces.IProfileService;
import io.smallrye.mutiny.Uni;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.guicedee.activitymaster.fsdm.client.services.IActivityMasterService.getISystem;
import static com.guicedee.activitymaster.fsdm.client.services.IActivityMasterService.getISystemToken;
import static com.guicedee.activitymaster.fsdm.client.services.classifications.DefaultClassifications.NoClassification;
import static com.guicedee.activitymaster.profiles.enumerations.ProfileIdentificationTypes.IdentificationTypeWebClientUUID;

@SuppressWarnings("Duplicates")
public class ProfileService
		implements IProfileService<ProfileService>
{
	private static final Logger log = LogManager.getLogger(ProfileService.class);
	
	@Inject
	private IPasswordsService<?> passwordsService;
	
	@Inject
	private IEnterprise<?, ?> enterprise;
	
	// Helper methods for reactive operations
    private Uni<ISystems<?, ?>> getISystemReactive(String systemName) {
        return getISystem(systemName, enterprise)
                .onItem().ifNull().failWith(() -> new NoSuchElementException("System not found: " + systemName));
    }

    private Uni<UUID> getISystemTokenReactive(String systemName) {
        return getISystemToken(systemName, enterprise)
                .onItem().ifNull().failWith(() -> new NoSuchElementException("System token not found: " + systemName));
    }

	//@Transactional()
	@Override
	public Uni<List<ProfileServiceDTO<?>>> listUsers(String... roles)
	{
		return allUsers()
			.map(users -> {
				List<ProfileServiceDTO<?>> filtered = new ArrayList<>();
				for (ProfileServiceDTO<?> user : users)
				{
					for (String role : roles)
					{
						if (user.findRoles().contains(role))
						{
							filtered.add(user);
						}
					}
				}
				return filtered;
			})
			.onFailure().invoke(error -> log.error("Error listing users: {}", error.getMessage(), error));
	}

	//@CacheResult(cacheName = "UserProfiles")
	@Override
	//@Transactional()
	public Uni<List<ProfileServiceDTO<?>>> allUsers()
	{
		// Get system and token using reactive helper methods
		return getISystemReactive(ProfileSystemName)
			.chain(system -> {
				return getISystemTokenReactive(ProfileSystemName)
					.chain(token -> {
						// Now use the system and token to get all users
						return passwordsService.getAllUsers(system, token)
							.chain(allIds -> {
								List<Uni<ProfileServiceDTO<?>>> profileDtoUnis = new ArrayList<>();
								
								for (IInvolvedParty<?, ?> allId : allIds) {
									ProfileServiceDTO<?> profileServiceDTO = new ProfileServiceDTO<>();
									profileServiceDTO.setInvolvedParty(allId);
									
									// Get system and token for each involved party
									Uni<ProfileServiceDTO<?>> dtoUni = getISystemReactive(ProfileSystemName)
										.chain(innerSystem -> {
											return getISystemTokenReactive(ProfileSystemName)
												.chain(innerToken -> {
													return allId.findInvolvedPartyIdentificationType(
														NoClassification, 
														IdentificationTypeWebClientUUID, 
														null,
														innerSystem, 
														true, 
														true, 
														innerToken
													)
													.map(idType -> {
														if (idType != null) {
															profileServiceDTO.setWebClientUUID(idType.getValueAsUUID());
														}
														return profileServiceDTO;
													});
												});
										});
									
									profileDtoUnis.add(dtoUni);
								}
								
								// Handle empty list case
								if (profileDtoUnis.isEmpty()) {
									return Uni.createFrom().item(Collections.<ProfileServiceDTO<?>>emptyList());
								}
								
								// Process all DTOs in parallel
								return Uni.join().all(profileDtoUnis).andCollectFailures();
							});
					});
			})
			.onFailure().invoke(error -> log.error("Error getting all users: {}", error.getMessage(), error));
	}

	//@CacheRemove(cacheName = "UserProfiles")
	@Override
	public Uni<Void> clearCache()
	{
		// Since the original method was empty, we just return a completed Uni
		return Uni.createFrom().voidItem();
	}
	
}
