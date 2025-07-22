package com.guicedee.activitymaster.profiles.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.services.jsonrepresentation.IJsonRepresentation;
import lombok.extern.log4j.Log4j2;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.guicedee.client.IGuiceContext.get;
import static com.guicedee.guicedinjection.interfaces.ObjectBinderKeys.DefaultObjectMapper;

@SuppressWarnings({"MissingClassJavaDoc", "unused"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = ANY,
        getterVisibility = NONE,
        setterVisibility = NONE)
@Log4j2
public class UserDTO<J extends UserDTO<J>>
        implements Serializable, IJsonRepresentation<J>
{
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Involved Party Identity Token
     */
    private UUID identityToken;
    
    @Inject
    private IEnterprise<?,?> enterprise;

    @Override
    public String toString() {
        try {
            return objectAsString(this);
        } catch (Throwable e) {
            log.error("Can't do the string", e);
        }
        return "Can't Convert";
    }

    /**
     * Returns the object presented as a JSON strong
     *
     * @param o An object to represent
     * @return the string
     */
    private String objectAsString(Object o) throws JsonProcessingException {
        return get(DefaultObjectMapper)
                .writeValueAsString(o);
    }

    public UUID getIdentityToken() {
        return this.identityToken;
    }

    public J setIdentityToken(java.util.UUID identityToken) {
        this.identityToken = identityToken;
        //noinspection unchecked
        return (J)this;
    }

    public IEnterprise<?,?> getEnterprise() {
        return this.enterprise;
    }

    public J setEnterprise(IEnterprise<?,?> enterprise) {
        this.enterprise = enterprise;
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
        if (!(o instanceof UserDTO))
        {
            return false;
        }
        UserDTO<?> userDTO = (UserDTO<?>) o;
        return Objects.equals(getIdentityToken(), userDTO.getIdentityToken());
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(getIdentityToken());
    }
}
