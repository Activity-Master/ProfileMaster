package com.armineasy.activitymaster.profiles.events.visits;

import com.armineasy.activitymaster.activitymaster.db.entities.involvedparty.InvolvedParty;
import com.armineasy.activitymaster.activitymaster.services.dto.IEnterprise;
import com.armineasy.activitymaster.activitymaster.services.dto.IEvent;
import com.armineasy.activitymaster.activitymaster.services.dto.IInvolvedParty;
import com.armineasy.activitymaster.activitymaster.services.dto.ISystems;
import com.armineasy.activitymaster.activitymaster.threads.TransactionalIdentifiedThread;
import com.armineasy.activitymaster.profiles.ProfileSystem;
import com.armineasy.activitymaster.profiles.dto.ProfileServiceDTO;
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

	private IEvent<?> event;
	private ProfileServiceDTO<?> profileServiceDTO;
	private IEnterprise<?> enterprise;
	private IInvolvedParty<?> newIp;
	private UUID[] identityToken;

	@Override
	public void perform()
	{
		ISystems profileSystem = ProfileSystem.getNewSystem()
		                                      .get(enterprise);
		//Add last login time
		String lastVisit = formatter.format(LocalDateTime.now());
		newIp.addOrUpdate(LastVisitTime,
		                  lastVisit,
		                  profileSystem,
		                  identityToken);
	}
}
