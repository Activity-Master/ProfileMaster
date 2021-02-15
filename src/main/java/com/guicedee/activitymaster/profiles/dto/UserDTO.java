package com.guicedee.activitymaster.profiles.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.guicedee.activitymaster.core.services.classifications.enterprise.IEnterpriseName;
import com.guicedee.activitymaster.profiles.deserializers.IEnterpriseNameDeserializer;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.guicedee.guicedinjection.GuiceContext.get;
import static com.guicedee.guicedinjection.interfaces.ObjectBinderKeys.DefaultObjectMapper;

@SuppressWarnings({"MissingClassJavaDoc", "unused"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = ANY,
        getterVisibility = NONE,
        setterVisibility = NONE)
@EqualsAndHashCode(of = {"enterprise","identityToken"},callSuper = false)
public class UserDTO<J extends UserDTO<J>>
        implements Serializable {
    private static final Logger log = Logger.getLogger(UserDTO.class.getName());
    
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Involved Party Identity Token
     */
    private UUID identityToken;

    @JsonDeserialize(using = IEnterpriseNameDeserializer.class)
    private IEnterpriseName<?> enterprise;

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

    public UserDTO<J> setIdentityToken(UUID identityToken) {
        this.identityToken = identityToken;
        return this;
    }

    public IEnterpriseName<?> getEnterprise() {
        return this.enterprise;
    }

    public J setEnterprise(IEnterpriseName<?> enterprise) {
        this.enterprise = enterprise;
		//noinspection unchecked
		return (J) this;
    }
}
