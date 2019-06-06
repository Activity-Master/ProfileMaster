package com.armineasy.activitymaster.profiles.events;

import com.armineasy.activitymaster.activitymaster.db.entities.events.Event;
import com.armineasy.activitymaster.activitymaster.db.entities.involvedparty.InvolvedParty;
import com.armineasy.activitymaster.activitymaster.services.dto.IEnterprise;
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
	private Event event;
	private ProfileServiceDTO<?> profileServiceDTO;
	private IEnterprise<?> enterprise;
	private ISystems profileSystem;
	private UUID[] identityToken;

	@Override
	public void perform()
	{
		newIp.addNameType(PreferredNameType, profileSystem, "Guest", identityToken);
		newIp.addOrReuse(CreatedBy, Long.toString(newIp.getId()), profileSystem, identityToken);
		event.add(newIp, PerformedBy, profileSystem, identityToken);
	}
}
