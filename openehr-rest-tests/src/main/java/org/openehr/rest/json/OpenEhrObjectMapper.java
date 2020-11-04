package org.openehr.rest.json;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

/**
 * @author Dusan Markovic
 */
public class OpenEhrObjectMapper extends ObjectMapper {
    private static final long serialVersionUID = 34655941859371045L;
    protected static final TypeResolverBuilder<?> TYPE_RESOLVER_BUILDER = new RmTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_FINAL)
            .init(JsonTypeInfo.Id.CLASS, null)
            .typeProperty("_type")
            .inclusion(JsonTypeInfo.As.PROPERTY);

    public OpenEhrObjectMapper() {
        setDefaultTyping(TYPE_RESOLVER_BUILDER);
        registerModule(new JodaModule());
        registerModule(new JavaTimeModule());
        registerModule(new AfterburnerModule());

        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        setPropertyNamingStrategy(new OpenEhrPropertyNamingStrategy());
    }

    protected OpenEhrObjectMapper(ObjectMapper src) {
        super(src);
    }

    /**
     * Copy method - creates a new objectmapper with same properties as existing one.
     *
     * @return object mapper
     */
    @Override
    public ObjectMapper copy() {
        ObjectMapper copy = new OpenEhrObjectMapper(this);
        copy.setDefaultTyping(TYPE_RESOLVER_BUILDER);
        copy.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        copy.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        copy.setPropertyNamingStrategy(getSerializationConfig().getPropertyNamingStrategy());
        return copy;
    }
}
