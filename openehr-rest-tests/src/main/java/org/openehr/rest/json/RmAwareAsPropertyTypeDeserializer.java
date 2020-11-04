package org.openehr.rest.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.util.JsonParserSequence;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.impl.AsPropertyTypeDeserializer;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.nedap.archie.rm.RMObject;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static org.openehr.utils.Utils.getRmTypeName;

/**
 * @author Dusan Markovic
 */
public class RmAwareAsPropertyTypeDeserializer extends AsPropertyTypeDeserializer {
    private static final long serialVersionUID = 1L;

    public RmAwareAsPropertyTypeDeserializer(AsPropertyTypeDeserializer src, BeanProperty property) {
        super(src, property);
    }

    @SuppressWarnings("AssignmentToMethodParameter")
    @Override
    protected Object _deserializeTypedUsingDefaultImpl(JsonParser p, DeserializationContext ctxt, TokenBuffer tb) throws IOException {
        JsonDeserializer<Object> deser = ctxt.findRootValueDeserializer(ctxt.getTypeFactory().constructType(Map.class));
        if (tb != null) {
            tb.writeEndObject();
            p = tb.asParser(p);
            // must move to point to the first token:
            p.nextToken();
        }
        return deser.deserialize(p, ctxt);
    }

    @Override
    public Object deserializeTypedFromAny(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.getCurrentToken() == JsonToken.START_ARRAY) {
            return ctxt.findRootValueDeserializer(ctxt.getTypeFactory().constructType(Collection.class)).deserialize(p, ctxt);
        }
        return deserializeTypedFromObject(p, ctxt);
    }

    @Override
    public TypeDeserializer forProperty(BeanProperty prop) {
        TypeDeserializer typeDeserializer = super.forProperty(prop);
        if (typeDeserializer instanceof AsPropertyTypeDeserializer) {
            return new RmAwareAsPropertyTypeDeserializer((AsPropertyTypeDeserializer)typeDeserializer, null);
        }
        return typeDeserializer;
    }

    @Override
    @SuppressWarnings("resource")
    public Object deserializeTypedFromObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.canReadTypeId()) {
            Object typeId = p.getTypeId();
            if (typeId != null) {
                return _deserializeWithNativeTypeId(p, ctxt, typeId);
            }
        }

        JsonToken t = p.getCurrentToken();
        if (t == JsonToken.START_OBJECT) {
            t = p.nextToken();
        } else if (t != JsonToken.FIELD_NAME) {
            return _deserializeTypedUsingDefaultImpl(p, ctxt, null);
        }
        TokenBuffer tb = null;

        for (; t == JsonToken.FIELD_NAME; t = p.nextToken()) {
            String name = p.getCurrentName();
            p.nextToken();
            if (name.equals(_typePropertyName)) {
                return _deserializeTypedForId(p, ctxt, tb);
            }

            if (tb == null) {
                tb = new TokenBuffer(p, ctxt);
            }
            tb.writeFieldName(name);
            tb.copyCurrentStructure(p);
        }
        if (tb != null) {
            tb.writeEndObject();
            p = tb.asParser(p);
            // must move to point to the first token:
            p.nextToken();
        }
        Class<?> rawClass = baseType().getRawClass();
        if (RMObject.class.isAssignableFrom(rawClass)) {
            if (tb != null) {
                tb.writeEndObject();
                p = tb.asParser(p);
                // must move to point to the first token:
                p.nextToken();
            }
            return _deserializeTypedForId(p, ctxt, tb, getRmTypeName(rawClass));
        }
        return _deserializeTypedUsingDefaultImpl(p, ctxt, tb);
    }

    @SuppressWarnings("resource")
    private Object _deserializeTypedForId(JsonParser p, DeserializationContext ctxt, TokenBuffer tb, String typeId) throws IOException {
        JsonDeserializer<Object> deser = _findDeserializer(ctxt, typeId);
        if (_typeIdVisible) {
            if (tb == null) {
                tb = new TokenBuffer(p, ctxt);
            }
            tb.writeFieldName(p.getCurrentName());
            tb.writeString(typeId);
        }
        if (tb != null) {
            p.clearCurrentToken();
            p = JsonParserSequence.createFlattened(false, tb.asParser(p), p);
        }
        p.nextToken();
        return deser.deserialize(p, ctxt);
    }
}
