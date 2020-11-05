package org.openehr.rest.json;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.ClassNameIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.nedap.archie.rm.RMObject;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.openehr.utils.Utils.getRmClass;
import static org.openehr.utils.Utils.getRmTypeName;

/**
 * @author Dusan Markovic
 */
public class RmIdResolver implements TypeIdResolver {
    private final TypeFactory typeFactory;
    @SuppressWarnings("AnonymousInnerClassMayBeStatic")
    private final LoadingCache<Class<?>, String> rmClassNames = CacheBuilder.newBuilder()
            .maximumSize(1000L)
            .build(new CacheLoader<>() {
                @Override
                public String load(@Nonnull Class<?> clazz) {
                    return getRmTypeName(clazz);
                }
            });

    private JavaType baseType;

    public RmIdResolver() {
        this(TypeFactory.defaultInstance());
    }

    public RmIdResolver(TypeFactory typeFactory) {
        this.typeFactory = typeFactory;
    }


    @Override
    public void init(JavaType baseType) {
        this.baseType = baseType;
    }

    @Override
    public String idFromValue(Object obj) {
        return idFromValueAndType(obj, obj.getClass());
    }

    @Override
    public String idFromValueAndType(Object value, Class<?> suggestedType) {
        if (RMObject.class.isAssignableFrom(suggestedType) || RMObject.class.isAssignableFrom(value.getClass())) {
            try {
                return rmClassNames.get(suggestedType);
            } catch (ExecutionException e) {
                return getRmTypeName(suggestedType);
            }
        }

        // Should really never happen
        ClassNameIdResolver classNameIdResolver = new ClassNameIdResolver(
                typeFactory.constructType(value.getClass()), typeFactory, LaissezFaireSubTypeValidator.instance);
        return classNameIdResolver.idFromValueAndType(value, suggestedType);
    }

    @Override
    public String idFromBaseType() {
        return idFromValueAndType(null, baseType.getRawClass());
    }

    @Override
    public JavaType typeFromId(DatabindContext context, String id) throws IOException {
        try {
            Class<? extends RMObject> rmClass = getRmClass(id);
            return typeFactory.constructType(rmClass);
        } catch (ClassNotFoundException ignored) {
            // Now try with the regular class type serializer
            ClassNameIdResolver classNameIdResolver = new ClassNameIdResolver(
                    null, context != null ? context.getTypeFactory() : typeFactory, LaissezFaireSubTypeValidator.instance);
            return classNameIdResolver.typeFromId(context, id);
        }
    }

    @Override
    public String getDescForKnownTypeIds() {
        return null;
    }

    @Override
    public Id getMechanism() {
        return Id.CUSTOM;
    }
}
