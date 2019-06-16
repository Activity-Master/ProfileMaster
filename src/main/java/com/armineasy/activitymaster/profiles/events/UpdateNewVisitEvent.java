package com.armineasy.activitymaster.profiles.events;

import com.armineasy.activitymaster.activitymaster.db.entities.involvedparty.InvolvedParty;
import com.armineasy.activitymaster.activitymaster.services.dto.IEnterprise;
import com.armineasy.activitymaster.activitymaster.services.dto.IEvent;
import com.armineasy.activitymaster.activitymaster.services.dto.ISystems;
import com.armineasy.activitymaster.activitymaster.threads.TransactionalIdentifiedThread;
import com.armineasy.activitymaster.profiles.dto.ProfileServiceDTO;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.UUID;

import static com.armineasy.activitymaster.activitymaster.services.classifications.events.EventInvolvedPartiesClassifications.*;
import static com.armineasy.activitymaster.activitymaster.services.types.NameTypes.*;

@Data
@Accessors(chain = true)
public class UpdateNewVisitEvent extends TransactionalIdentifiedThread
{
	@Getter
	private static final String JobServiceName = "NewVisitorCustomIdentifiersAndItems";

	private InvolvedParty newIp;
	private IEvent<?> event;
	private ProfileServiceDTO<?> profileServiceDTO;
	private IEnterprise<?> enterprise;
	private ISystems<?> profileSystem;
	private UUID[] identityToken;

	@Override
	public void perform()
	{
		newIp.addOrReuse(PreferredNameType, "Guest",profileSystem, identityToken);
		newIp.addOrReuse(CreatedBy, Long.toString(newIp.getId()), profileSystem, identityToken);
		event.addOrReuse(PerformedBy,newIp.getSecurityIdentity().toString(),  profileSystem, identityToken);
	}
}
