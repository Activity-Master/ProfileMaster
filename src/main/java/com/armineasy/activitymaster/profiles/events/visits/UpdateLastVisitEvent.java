package com.armineasy.activitymaster.profiles.events.visits;

import com.armineasy.activitymaster.activitymaster.db.entities.enterprise.Enterprise;
import com.armineasy.activitymaster.activitymaster.db.entities.events.Event;
import com.armineasy.activitymaster.activitymaster.db.entities.involvedparty.InvolvedParty;
import com.armineasy.activitymaster.activitymaster.db.entities.systems.Systems;
import com.armineasy.activitymaster.activitymaster.threads.TransactionalIdentifiedThread;
import com.armineasy.activitymaster.profiles.ProfileSystem;
import com.armineasy.activitymaster.profiles.dto.GuestDTO;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static com.armineasy.activitymaster.profiles.enumerations.ProfileClassifications.*;


@Data
@Accessors(chain = true)
public class UpdateLastVisitEvent extends TransactionalIdentifiedThread
{
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");
	@Getter
	private static final String JobServiceName = "UpdateLastVisitEvent";

	private Event event;
	private GuestDTO<?> guestDTO;
	private Enterprise enterprise;
	private InvolvedParty newIp;
	private UUID[] identityToken;

	@Override
	public void perform()
	{
		Systems profileSystem = ProfileSystem.getNewSystem()
		                                     .get(enterprise);
		//Add last login time
		String lastVisit = formatter.format(LocalDateTime.now());
		newIp.addOrUpdateClassification(LastVisitTime,
		                                lastVisit,
		                                profileSystem,
		                                identityToken);
	}
}
