package com.guicedee.activitymaster.profiles.services.interfaces;

import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.profiles.dto.ProfileServiceDTO;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.List;

public interface IProfileService<J extends IProfileService<J>>
{
	String ProfileSystemName = "Profiles Master";
	
	Uni<List<ProfileServiceDTO<?>>> listUsers(Mutiny.Session session, IEnterprise<?, ?> enterprise, String... roles);
	
	Uni<List<ProfileServiceDTO<?>>> allUsers(Mutiny.Session session, IEnterprise<?, ?> enterprise);
	
	Uni<Void> clearCache();
}
