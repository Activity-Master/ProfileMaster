package com.armineasy.activitymaster.profiles.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class UserConfirmationKeyDTO<J extends UserConfirmationKeyDTO<J>>
		extends ProfileServiceDTO<J>
{
	private UUID confirmationKey;
}
