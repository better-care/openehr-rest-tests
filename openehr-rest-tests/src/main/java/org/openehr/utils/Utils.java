package org.openehr.utils;

import com.google.common.base.CaseFormat;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.nedap.archie.rm.RMObject;
import com.nedap.archie.rm.composition.Composition;
import com.nedap.archie.rm.datatypes.CodePhrase;
import com.nedap.archie.rm.datavalues.DvCodedText;
import com.nedap.archie.rm.datavalues.DvText;
import com.nedap.archie.rm.ehr.Ehr;
import com.nedap.archie.rm.generic.PartyIdentified;
import com.nedap.archie.rm.support.identification.TerminologyId;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Dusan Markovic
 */
public class Utils {
    private static final ConcurrentMap<String, Class<RMObject>> CLASS_MAP = new ConcurrentHashMap<>();
    private static final Set<String> RM_PACKAGE_NAMES = ImmutableSet.of(
            Composition.class.getPackage().getName(),
            Ehr.class.getPackage().getName());

    public static Class<? extends RMObject> getRmClass(@Nonnull String rmTypeName) throws ClassNotFoundException {
        int genericsTypeIndex = rmTypeName.indexOf('<');
        String name = genericsTypeIndex == -1 ? rmTypeName : rmTypeName.substring(0, genericsTypeIndex);
        if (!CLASS_MAP.containsKey(name)) {
            Class<RMObject> clazz = findRmClass(name).orElseThrow(() -> new ClassNotFoundException(rmTypeName));
            CLASS_MAP.put(name, clazz);
        }
        return CLASS_MAP.get(name);
    }

    @SuppressWarnings("unchecked")
    private static Optional<Class<RMObject>> findRmClass(String className) {
        for (String packageName : RM_PACKAGE_NAMES) {
            try {
                return Optional.of((Class<RMObject>)Class.forName(packageName + '.' + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, className)));
            } catch (ClassNotFoundException ignored) {
            }
        }
        return Optional.empty();
    }

    public static DvText getText(String value) {
        DvText text = new DvText();
        text.setValue(value);
        return text;
    }

    public static PartyIdentified getPartyIdentified(String partyName) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(partyName), "party name is empty");
        PartyIdentified partyIdentified = new PartyIdentified();
        partyIdentified.setName(partyName);
        return partyIdentified;
    }

    public static DvCodedText getCodedText(String terminology, String code, String value) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(terminology), "terminology is empty");
        Preconditions.checkArgument(StringUtils.isNotEmpty(code), "code is empty");
        DvCodedText coded = new DvCodedText();
        coded.setDefiningCode(getCodePhrase(terminology, code));
        coded.setValue(value);
        return coded;
    }

    public static CodePhrase getCodePhrase(String terminology, String code) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(terminology), "code phrase terminology is empty");
        Preconditions.checkArgument(StringUtils.isNotEmpty(code), "code is empty");
        TerminologyId terminologyId = new TerminologyId();
        terminologyId.setValue(terminology);
        CodePhrase codePhrase = new CodePhrase();
        codePhrase.setTerminologyId(terminologyId);
        codePhrase.setCodeString(code);
        return codePhrase;
    }

    public static String getRmTypeName(@Nonnull Class<?> clazz) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, clazz.getSimpleName());
    }

    public static String getAttributeForField(@Nonnull String fieldName) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, fieldName);
    }

}
