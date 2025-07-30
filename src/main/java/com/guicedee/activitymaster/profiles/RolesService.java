package com.guicedee.activitymaster.profiles;

//import com.google.inject.persist.Transactional;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;
import com.guicedee.activitymaster.profiles.services.interfaces.IRolesService;
import com.guicedee.activitymaster.profiles.services.interfaces.IUserRole;
import io.github.classgraph.ClassInfo;
import io.smallrye.mutiny.Uni;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import static com.guicedee.activitymaster.profiles.enumerations.ProfileClassifications.*;
import static com.guicedee.client.IGuiceContext.*;

public class RolesService
		implements IRolesService<RolesService>
{
	private static final Logger log = LogManager.getLogger(RolesService.class);

	//@Transactional()
	@Override
	//@CacheResult(cacheName = "UserRolesGetRoles")
	public Uni<Set<String>> getRoles(Mutiny.Session session, IInvolvedParty<?, ?> ip, ISystems<?, ?> systems, UUID... identityToken)
	{
		if (ip == null)
		{
			Set<String> emptyRoles = new TreeSet<>();
			emptyRoles.add("Guest");
			return Uni.createFrom().item(emptyRoles);
		}
		
		// Use findClassifications from IManageClassifications instead of getClassificationsValuePivot
		return ip.findClassifications(session, UserRoles.toString(), systems, identityToken)
			.map(classifications -> {
				Set<String> assignedRoles = new TreeSet<>();
				for (var classification : classifications)
				{
					assignedRoles.add(classification.getClassificationID().getName());
				}
				if (assignedRoles.isEmpty())
				{
					assignedRoles.add("Guest");
				}
				return assignedRoles;
			})
			.onFailure().invoke(error -> log.error("Error getting roles: {}", error.getMessage(), error))
			.onFailure().recoverWithItem(() -> {
				Set<String> defaultRoles = new TreeSet<>();
				defaultRoles.add("Guest");
				return defaultRoles;
			});
	}
	
	@Override
	//@CacheResult(cacheName = "UserRolesGetRoles", skipGet = true)
	//@Transactional()
	public Uni<Set<String>> addRole(
			Mutiny.Session session, IInvolvedParty<?, ?> ip, String role, ProfileServiceDTO<?> dto, ISystems<?, ?> systems, UUID... identityToken)
	{
		// Avoid using ReactiveTransactionUtil
		return getRoles(session, ip, systems, identityToken)
			.chain(roles -> {
				if (!roles.contains(role))
				{
					// Use addClassification from IManageClassifications
					return ip.addClassification(session, UserRoles.toString(), role, systems, identityToken)
						.map(result -> {
							roles.add(role);
							return roles;
						});
				}
				return Uni.createFrom().item(roles);
			})
			.onFailure().invoke(error -> log.error("Error adding role: {}", error.getMessage(), error));
	}
	
	//@Transactional()
	//@CacheResult(cacheName = "RolesServiceFindAllRoles")
	@Override
	public Uni<Set<String>> findAllRoles()
	{
		return Uni.createFrom().item(() -> {
			Set<String> roles = new TreeSet<>();
			for (ClassInfo classInfo : instance().getScanResult()
											 .getClassesImplementing(IUserRole.class.getCanonicalName()))
			{
				for (Object enumConstant : classInfo.loadClass()
												.getEnumConstants())
				{
					IUserRole<?> role = (IUserRole<?>) enumConstant;
					roles.add(role.toString());
				}
			}
			return roles;
		}).onFailure().invoke(error -> log.error("Error finding all roles: {}", error.getMessage(), error));
	}
}