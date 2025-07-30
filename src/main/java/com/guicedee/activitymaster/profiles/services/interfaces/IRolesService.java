package com.guicedee.activitymaster.profiles.services.interfaces;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party.IInvolvedParty;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.Set;
import java.util.UUID;


public interface IRolesService<J extends IRolesService<J>>
{
	String USER_ROLES_SESSION_NAME = "user-roles";
	
	Uni<Set<String>> getRoles(Mutiny.Session session, IInvolvedParty<?,?> ip, ISystems<?,?> systems, UUID... identityToken);
	
	Uni<Set<String>> addRole(Mutiny.Session session, IInvolvedParty<?,?> ip, String role, ProfileServiceDTO<?> dto, ISystems<?,?> systems, UUID... identityToken);
	
	Uni<Set<String>> findAllRoles();
}
