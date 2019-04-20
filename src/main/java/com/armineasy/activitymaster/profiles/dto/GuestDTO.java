package com.armineasy.activitymaster.profiles.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain=true)
public class GuestDTO<J extends GuestDTO<J>> extends UserDTO<J>
{
	private UUID webClientUUID;
}
