package com.guicedee.activitymaster.profiles;

import com.google.inject.Inject;
import com.guicedee.activitymaster.fsdm.client.services.IInvolvedPartyService;
import com.guicedee.activitymaster.fsdm.client.services.ISystemsService;
import com.guicedee.activitymaster.fsdm.client.services.administration.ActivityMasterDefaultSystem;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.systems.IActivityMasterSystem;
import com.guicedee.activitymaster.profiles.services.interfaces.IRolesService;
import io.smallrye.mutiny.Uni;
import lombok.extern.log4j.Log4j2;
import org.hibernate.reactive.mutiny.Mutiny;

import static com.guicedee.activitymaster.fsdm.client.services.classifications.types.IdentificationTypes.*;
import static com.guicedee.activitymaster.profiles.services.enumerations.UserRoles.*;
import static com.guicedee.activitymaster.profiles.services.interfaces.IProfileService.*;

@Log4j2
public class ProfileSystem
		extends ActivityMasterDefaultSystem<ProfileSystem>
		implements IActivityMasterSystem<ProfileSystem>
{
	@Inject
	private ISystemsService<?> systemsService;

	@Override
	public Uni<ISystems<?,?>> registerSystem(Mutiny.Session session, IEnterprise<?,?> enterprise)
	{
		log.info("🚀 Registering Profile System for enterprise: '{}'", enterprise.getName());
		log.debug("📋 Creating Profile System with session: {}", session.hashCode());
		
		return systemsService
		        .create(session, enterprise, getSystemName(), getSystemDescription())
		        .chain(system -> {
		            log.debug("✅ Created Profile System: '{}' with session: {}", system.getName(), session.hashCode());
		            // Properly chain the registration to keep all operations on the same session/thread
		            return getSystem(session, enterprise)
		                .chain(sys -> systemsService.registerNewSystem(session, enterprise, sys))
		                .onItem()
		                .invoke(() -> {
		                    log.debug("✅ Registered system: {}", getSystemName());
		                    log.info("🎉 Successfully registered Profile System for enterprise: '{}'", enterprise.getName());
		                })
		                .onFailure()
		                .invoke(error -> log.error("❌ Error registering system: {}", error.getMessage(), error))
		                .chain(ignored -> Uni.createFrom().item((ISystems<?, ?>) system)); // return the created system after registration
		        })
		        .onFailure()
		        .invoke(error -> log.error("❌ Failed to create Profile System: '{}' with session {}: {}",
		            getSystemName(), session.hashCode(), error.getMessage(), error))
		        .map(sys -> (ISystems<?, ?>) sys);
	}
	
	@Override
	public Uni<Void> createDefaults(Mutiny.Session session, IEnterprise<?,?> enterprise)
	{
		logProgress("Profile System", "Starting Profile Checks");
		log.info("🚀 Creating profile defaults for enterprise: '{}'", enterprise.getName());
		log.debug("📋 Starting with session: {}", session.hashCode());
		
		// No actual operations needed, just return a void item
		log.debug("✅ No specific defaults needed for Profile System");
		return Uni.createFrom()
		           .voidItem()
		           .onItem()
		           .invoke(() -> log.info("🎉 Successfully completed Profile System defaults"))
		           .onFailure()
		           .invoke(error -> log.error("❌ Error in Profile System defaults: {}", error.getMessage(), error))
		           .replaceWithVoid();
	}
	
	@Override
	public int totalTasks()
	{
		return 5;
	}
	
	@Override
	public Integer sortOrder()
	{
		return Integer.MIN_VALUE + 15;
	}
	
	@Override
	public Uni<Void> postStartup(Mutiny.Session session, IEnterprise<?,?> enterprise)
	{
		log.info("🚀 Starting reactive postStartup for Profile System");
		log.debug("📋 Beginning postStartup operations for enterprise: '{}' with session: {}", 
		         enterprise.getName(), session.hashCode());
		
		// Get the system and token first
		return getSystem(session, enterprise)
		        .onItem()
		        .invoke(system -> log.debug("✅ Found system: '{}'", system.getName()))
		        .onItem()
		        .ifNull()
		        .failWith(() -> new RuntimeException("System not found: " + getSystemName()))
		        .onFailure()
		        .invoke(error -> log.error("❌ Failed to find system: {}", error.getMessage(), error))
		        .chain(system -> {
		            log.debug("🔍 Retrieving security token for system: '{}'", system.getName());
		            return getSystemToken(session, enterprise)
		                    .onItem()
		                    .invoke(token -> log.debug("🔑 Found security token for system: '{}'", system.getName()))
		                    .onItem()
		                    .ifNull()
		                    .failWith(() -> new RuntimeException("Security token not found for system: " + system.getName()))
		                    .onFailure()
		                    .invoke(error -> log.error("❌ Failed to retrieve security token: {}", error.getMessage(), error))
		                    .chain(identityToken -> {
		                        log.debug("🔍 Finding involved party with enterprise creator role");
		                        IInvolvedPartyService<?> involvedPartyService = com.guicedee.client.IGuiceContext.get(IInvolvedPartyService.class);
		                        
                          // Use reactive pattern for finding involved party
                          return involvedPartyService.get().builder(session)
                                  .findByIdentificationType(IdentificationTypeEnterpriseCreatorRole.toString(), null, system, identityToken)
                                  .get()
		                                .onItem()
		                                .invoke(ipObj -> {
		                                    if (ipObj != null) {
		                                        log.debug("✅ Found involved party with enterprise creator role");
		                                    } else {
		                                        log.debug("⚠️ No involved party found with enterprise creator role");
		                                    }
		                                })
		                                .onFailure()
		                                .invoke(error -> log.error("❌ Error finding involved party: {}", error.getMessage(), error))
		                                .chain(ipObj -> {
		                                    if (ipObj != null) {
		                                        // Cast to the correct type
		                                        IInvolvedParty<?, ?> ip = (IInvolvedParty<?, ?>) ipObj;
		                                        IRolesService<?> rolesService = com.guicedee.client.IGuiceContext.get(IRolesService.class);
		                                        
		                                        log.debug("🔍 Checking roles for involved party");
		                                        // Use reactive getRoles method
		                                        return rolesService.getRoles(session, ip, system, identityToken)
		                                            .onItem()
		                                            .invoke(roles -> log.debug("✅ Found {} roles for involved party", roles.size()))
		                                            .onFailure()
		                                            .invoke(error -> log.error("❌ Error getting roles: {}", error.getMessage(), error))
		                                            .chain(roles -> {
		                                                if (!roles.contains(Administrator.toString())) {
		                                                    log.debug("🔄 Adding Administrator role to involved party");
		                                                    // Use reactive addRole method
		                                                    return rolesService.addRole(session, ip, Administrator.toString(), null, system, identityToken)
		                                                        .onItem()
		                                                        .invoke(result -> log.debug("✅ Added Administrator role to involved party"))
		                                                        .onFailure()
		                                                        .invoke(error -> log.error("❌ Error adding Administrator role: {}", error.getMessage(), error));
		                                                }
		                                                log.debug("✅ Involved party already has Administrator role");
		                                                return Uni.createFrom().item(roles);
		                                            });
		                                    }
		                                    log.debug("⚠️ No involved party to assign roles to");
		                                    return Uni.createFrom().nullItem();
		                                });
		                    });
		        })
		        .onItem()
		        .invoke(() -> log.info("🎉 Profile System postStartup completed successfully"))
		        .onFailure()
		        .invoke(error -> log.error("❌ Error in Profile System postStartup: {}", error.getMessage(), error))
		        .replaceWithVoid();
	}
	
	@Override
	public String getSystemName()
	{
		return ProfileSystemName;
	}
	
	@Override
	public String getSystemDescription()
	{
		return "The system for managing User Profiles";
	}
}