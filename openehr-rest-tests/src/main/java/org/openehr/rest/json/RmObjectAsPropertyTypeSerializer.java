package org.openehr.rest.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.AsPropertyTypeSerializer;
import com.nedap.archie.rm.RMObject;

import java.io.IOException;

/**
 * @author Dusan Markovic
 */
public class RmObjectAsPropertyTypeSerializer extends AsPropertyTypeSerializer {


    public RmObjectAsPropertyTypeSerializer(
            TypeIdResolver idRes, BeanProperty property, String propName) {
        super(idRes, property, propName);
    }

    @SuppressWarnings("ObjectEquality")
    @Override
    public AsPropertyTypeSerializer forProperty(BeanProperty prop) {
        return (_property == prop) ? this : new RmObjectAsPropertyTypeSerializer(_idResolver, prop, _typePropertyName);
    }

    @Override
    public WritableTypeId writeTypePrefix(JsonGenerator g, WritableTypeId typeIdDef) throws IOException {

        if (isRmObject(typeIdDef.forValue)) {
            return super.writeTypePrefix(g, typeIdDef);
        } else {
            _generateTypeId(typeIdDef);
            return writeNonRmTypePrefix(g, typeIdDef);
        }
    }

    @Override
    public WritableTypeId writeTypeSuffix(JsonGenerator g, WritableTypeId typeIdDef) throws IOException {

        if (isRmObject(typeIdDef.forValue)) {
            return super.writeTypeSuffix(g, typeIdDef);
        } else {
            return writeNonRmTypeSuffix(g, typeIdDef);
        }
    }

    private WritableTypeId writeNonRmTypePrefix(JsonGenerator g, WritableTypeId typeIdDef) throws IOException {
        Object id = typeIdDef.id;

        final JsonToken valueShape = typeIdDef.valueShape;
        if (g.canWriteTypeId()) {
            typeIdDef.wrapperWritten = false;
            g.writeTypeId(id);
        } else {
            String idStr = (id instanceof String) ? (String)id : String.valueOf(id);
            typeIdDef.wrapperWritten = true;

            WritableTypeId.Inclusion incl = typeIdDef.include;
            if ((valueShape != JsonToken.START_OBJECT)
                    && incl.requiresObjectContext()) {
                typeIdDef.include = incl = WritableTypeId.Inclusion.WRAPPER_ARRAY;
            }

            switch (incl) {
                case PARENT_PROPERTY:
                case PAYLOAD_PROPERTY:
                case METADATA_PROPERTY:
                    break;
                case WRAPPER_OBJECT:
                    g.writeStartObject();
                    break;
                case WRAPPER_ARRAY:
                    break;
                default: // should never occur but translate as "as-array"
                    g.writeStartArray();
                    g.writeString(idStr);
            }
        }
        // and finally possible start marker for value itself:
        if (valueShape == JsonToken.START_OBJECT) {
            g.writeStartObject(typeIdDef.forValue);
        } else if (valueShape == JsonToken.START_ARRAY) {
            // should we now set the current object?
            g.writeStartArray();
        }
        return typeIdDef;
    }

    private WritableTypeId writeNonRmTypeSuffix(JsonGenerator g, WritableTypeId typeIdDef) throws IOException {
        final JsonToken valueShape = typeIdDef.valueShape;
        // First: does value need closing?
        if (valueShape == JsonToken.START_OBJECT) {
            g.writeEndObject();
        } else if (valueShape == JsonToken.START_ARRAY) {
            g.writeEndArray();
        }

        if (typeIdDef.wrapperWritten) {
            switch (typeIdDef.include) {
                case PARENT_PROPERTY:
                    // unusually, need to output AFTER value. And no real wrapper...
                {
                    Object id = typeIdDef.id;
                    String idStr = (id instanceof String) ? (String)id : String.valueOf(id);
                    g.writeStringField(typeIdDef.asProperty, idStr);
                }
                break;
                case WRAPPER_ARRAY:
                case METADATA_PROPERTY:
                case PAYLOAD_PROPERTY:
                    // no actual wrapper; included within Object itself
                    break;
                case WRAPPER_OBJECT:
                default: // should never occur but...
                    g.writeEndObject();
                    break;
            }
        }
        return typeIdDef;
    }

    private boolean isRmObject(Object value) {
        return RMObject.class.isAssignableFrom(value.getClass());
    }
}
