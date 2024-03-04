package com.guicedee.activitymaster.profiles.dto;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.services.jsonrepresentation.IJsonRepresentation;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.*;
import static com.guicedee.client.IGuiceContext.*;
import static com.guicedee.guicedinjection.interfaces.ObjectBinderKeys.*;

@SuppressWarnings({"MissingClassJavaDoc", "unused"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = ANY,
        getterVisibility = NONE,
        setterVisibility = NONE)
public class UserDTO<J extends UserDTO<J>>
        implements Serializable, IJsonRepresentation<J>
{
    private static final Logger log = Logger.getLogger(UserDTO.class.getName());
    
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
        } catch (JsonProcessingException e) {
            log.log(Level.SEVERE, "Can't do the string", e);
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
