package org.openehr.rest.json;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.AsPropertyTypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.nedap.archie.rm.RMObject;

import java.util.Collection;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.CLASS;

/**
 * @author Dusan Markovic
 */
public class RmTypeResolverBuilder extends ObjectMapper.DefaultTypeResolverBuilder {
    private static final long serialVersionUID = 1L;

    public RmTypeResolverBuilder(ObjectMapper.DefaultTyping t) {
        super(t, LaissezFaireSubTypeValidator.instance);
    }

    @Override
    public TypeDeserializer buildTypeDeserializer(DeserializationConfig config, JavaType baseType, Collection<NamedType> subtypes) {
        TypeDeserializer typeDeserializer = super.buildTypeDeserializer(config, baseType, subtypes);
        if (typeDeserializer instanceof AsPropertyTypeDeserializer) {
            return new RmAwareAsPropertyTypeDeserializer((AsPropertyTypeDeserializer)typeDeserializer, null);
        }
        return typeDeserializer;
    }

    @Override
    public TypeSerializer buildTypeSerializer(SerializationConfig config, JavaType baseType, Collection<NamedType> subtypes) {

        if (_idType == JsonTypeInfo.Id.NONE) {
            return null;
        }

        if (!useForType(baseType)) {
            return null;
        }

        if (_includeAs == JsonTypeInfo.As.PROPERTY) {
            TypeIdResolver idRes = idResolver(config, baseType, LaissezFaireSubTypeValidator.instance, subtypes, true, false);
            return new RmObjectAsPropertyTypeSerializer(idRes, null, _typeProperty);
        }

        return super.buildTypeSerializer(config, baseType, subtypes);
    }

    @Override
    public boolean useForType(JavaType t) {
        // Allow RmObject and descendants or just if type is Object (NOT A DESCENDANT TYPE!!!) since the type resolver will often figure out Object
        // for the generic type of a collection or map
        if (!RMObject.class.isAssignableFrom(t.getRawClass()) && !t.getRawClass().equals(Object.class)) {
            return false;
        }

        return super.useForType(t);
    }

    @Override
    protected TypeIdResolver idResolver(
            MapperConfig<?> config,
            JavaType baseType,
            PolymorphicTypeValidator subtypeValidator,
            Collection<NamedType> subtypes,
            boolean forSer,
            boolean forDeser) {

        if (_idType == CLASS) {
            if (RMObject.class.isAssignableFrom(baseType.getRawClass()) || baseType.getRawClass().equals(Object.class)) {
                return new RmIdResolver(config.getTypeFactory());
            }
        }
        return super.idResolver(config, baseType, subtypeValidator, subtypes, forSer, forDeser);
    }
}
