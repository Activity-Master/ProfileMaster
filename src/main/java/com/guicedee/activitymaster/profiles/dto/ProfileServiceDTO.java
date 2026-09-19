package com.guicedee.activitymaster.profiles.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = ANY,
    getterVisibility = NONE,
    setterVisibility = NONE)
public class ProfileServiceDTO<J extends ProfileServiceDTO<J>>
    extends UserDTO<J>
{
  public static final String IDENTITY_SESSION_NAME = "identity";

  @JsonProperty
  private UUID webClientUUID;

  public UUID getWebClientUUID()
  {
    return webClientUUID;
  }

  public @org.jspecify.annotations.NonNull J setWebClientUUID(UUID webClientUUID)
  {
    this.webClientUUID = webClientUUID;
    //noinspection unchecked
    return (J) this;
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (!(o instanceof ProfileServiceDTO))
    {
      return false;
    }
    ProfileServiceDTO<?> that = (ProfileServiceDTO<?>) o;
    return Objects.equals(getWebClientUUID(), that.getWebClientUUID()) &&
           Objects.equals(getIdentityToken(), that.getIdentityToken());
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(getWebClientUUID(), getIdentityToken());
  }
}