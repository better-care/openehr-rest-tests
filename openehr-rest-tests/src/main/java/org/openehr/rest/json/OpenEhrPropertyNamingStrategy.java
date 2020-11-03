package org.openehr.rest.json;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.nedap.archie.rm.RMObject;
import com.nedap.archie.rm.changecontrol.VersionedObject;
import com.nedap.archie.rm.ehr.Ehr;
import com.nedap.archie.rm.ehr.EhrStatus;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.lang.annotation.Annotation;

import static org.openehr.utils.Utils.getAttributeForField;

/**
 * @author Dusan Markovic
 */
public class OpenEhrPropertyNamingStrategy extends PropertyNamingStrategy {
    private static final long serialVersionUID = 1L;

    @Override
    public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName) {
        if (field != null && field.getDeclaringClass() != null && isRmClass(field.getDeclaringClass())) {
            return getAttributeForField(defaultName);
        }
        return super.nameForField(config, field, defaultName);
    }

    @Override
    public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
        if (method != null && method.getDeclaringClass() != null && isRmClass(method.getDeclaringClass())) {
            String xmlElementName = getXmlElementName(method);
            if (xmlElementName == null) {
                String xmlAttributeName = getXmlAttributeName(method);
                return xmlAttributeName == null ? defaultName : xmlAttributeName;
            }
            return xmlElementName;
        }
        return super.nameForGetterMethod(config, method, defaultName);
    }

    @Override
    public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
        if (method != null && method.getDeclaringClass() != null && isRmClass(method.getDeclaringClass())) {
            String xmlElementName = getXmlElementName(method);
            if (xmlElementName == null) {
                String xmlAttributeName = getXmlAttributeName(method);
                return xmlAttributeName == null ? defaultName : xmlAttributeName;
            }
            return xmlElementName;
        }
        return super.nameForSetterMethod(config, method, defaultName);
    }

    private String getXmlElementName(AnnotatedMethod method) {
        Annotation annotation = method.getAnnotation(XmlElement.class);
        try {
            return annotation == null || annotation.annotationType().getDeclaredMethod("name").getDefaultValue().equals(((XmlElement)annotation).name()) ?
                    null :
                    ((XmlElement)annotation).name();
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private String getXmlAttributeName(AnnotatedMethod method) {
        Annotation annotation = method.getAnnotation(XmlAttribute.class);
        try {
            return annotation == null || annotation.annotationType().getDeclaredMethod("name").getDefaultValue().equals(((XmlAttribute)annotation).name()) ?
                    null :
                    ((XmlAttribute)annotation).name();
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private boolean isRmClass(Class<?> declaringClass) {
        return RMObject.class.isAssignableFrom(declaringClass)
                || Ehr.class.isAssignableFrom(declaringClass)
                || EhrStatus.class.isAssignableFrom(declaringClass)
                || VersionedObject.class.isAssignableFrom(declaringClass);
    }
}
