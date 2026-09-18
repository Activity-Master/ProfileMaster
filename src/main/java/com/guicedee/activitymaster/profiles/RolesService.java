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

	
	
	@Override
	public Uni<Set<String>> getRoles(Mutiny.StatelessSession session, IInvolvedParty<?, ?> ip, ISystems<?, ?> systems, UUID... identityToken)
	{
		if (ip == null) { Set<String> e = new TreeSet<>(); e.add("Guest"); return Uni.createFrom().item(e); }
		return ip.findClassifications(session, UserRoles.toString(), systems, identityToken)
			.map(classifications -> {
				Set<String> assignedRoles = new TreeSet<>();
				for (var classification : classifications) { assignedRoles.add(classification.getValue()); }
				if (assignedRoles.isEmpty()) { assignedRoles.add("Guest"); }
				return assignedRoles;
			})
			.onFailure().recoverWithItem(() -> { Set<String> d = new TreeSet<>(); d.add("Guest"); return d; });
	}

	@Override
	public Uni<Set<String>> addRole(Mutiny.StatelessSession session, IInvolvedParty<?, ?> ip, String role, ProfileServiceDTO<?> dto, ISystems<?, ?> systems, UUID... identityToken)
	{
		return getRoles(session, ip, systems, identityToken)
			.chain(roles -> roles.contains(role) ? Uni.createFrom().item(roles)
				: ip.addClassification(session, UserRoles.toString(), role, systems, identityToken).map(r -> { roles.add(role); return roles; }));
	}

	@Override
	//@CacheResult(cacheName = "RolesServiceFindAllRoles", skipGet = true)
	
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
