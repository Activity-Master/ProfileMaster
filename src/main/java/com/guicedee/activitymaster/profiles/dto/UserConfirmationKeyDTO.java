package com.guicedee.activitymaster.profiles.dto;

import java.util.UUID;


public class UserConfirmationKeyDTO<J extends UserConfirmationKeyDTO<J>>
		extends ProfileServiceDTO<J>
{
	private UUID confirmationKey;

	public UserConfirmationKeyDTO()
	{
	}

	public UUID getConfirmationKey()
	{
		return this.confirmationKey;
	}

	public UserConfirmationKeyDTO<J> setConfirmationKey(UUID confirmationKey)
	{
		this.confirmationKey = confirmationKey;
		return this;
	}

	public boolean equals(final Object o)
	{
		if (o == this)
		{
			return true;
		}
		if (!(o instanceof UserConfirmationKeyDTO))
		{
			return false;
		}
		final UserConfirmationKeyDTO<?> other = (UserConfirmationKeyDTO<?>) o;
		if (!other.canEqual((Object) this))
		{
			return false;
		}
		final Object this$confirmationKey = this.getConfirmationKey();
		final Object other$confirmationKey = other.getConfirmationKey();
		if (this$confirmationKey == null ? other$confirmationKey != null : !this$confirmationKey.equals(other$confirmationKey))
		{
			return false;
		}
		return true;
	}

	protected boolean canEqual(final Object other)
	{
		return other instanceof UserConfirmationKeyDTO;
	}

	public int hashCode()
	{
		final int PRIME = 59;
		int result = 1;
		final Object $confirmationKey = this.getConfirmationKey();
		result = result * PRIME + ($confirmationKey == null ? 43 : $confirmationKey.hashCode());
		return result;
	}

	public String toString()
	{
		return "UserConfirmationKeyDTO(confirmationKey=" + this.getConfirmationKey() + ")";
	}
}
