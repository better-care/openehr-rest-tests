package org.openehr.data;

import org.openehr.ResultWithContributionWrapper;
import org.openehr.rest.exception.OpenEhrRestException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import javax.annotation.Nullable;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toMap;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * @author Dusan Markovic
 */
public final class OpenEhrConstants {

    public static final String AUDIT_DETAILS_COMMITTER = "openEHR-AUDIT_DETAILS.committer";
    public static final String AUDIT_DETAILS_DESCRIPTION = "openEHR-AUDIT_DETAILS.description";
    public static final String AUDIT_DETAILS_CHANGE_TYPE = "openEHR-AUDIT_DETAILS.change_type";
    public static final String VERSION_LIFECYCLE_STATE = "openEHR-VERSION.lifecycle_state";
    public static final String OPEN_EHR_PATH = "/rest/openehr/v1";
    public static final String EHR_PATH = "/ehr/{ehr_id}";
    public static final String GET_EHR_STATUS_PATH = "/ehr/{ehr_id}/ehr_status/{version_uid}";
    public static final String GET_EHR_STATUS_VERSION_PATH = "/ehr/{ehr_id}/versioned_ehr_status/version/{version_uid}";
    public static final String GET_FOLDER_PATH = "/ehr/{ehr_id}/directory/{version_uid}";
    public static final String GET_COMPOSITION_PATH = "/ehr/{ehr_id}/composition/{version_uid}";
    public static final String POST_COMPOSITION_PATH = "/ehr/{ehr_id}/composition";
    public static final String GET_VERSIONED_COMPOSITION_PATH = "/ehr/{ehr_id}/versioned_composition/{versioned_object_uid}/version";
    public static final String GET_COMPOSITION_VERSION_PATH = GET_VERSIONED_COMPOSITION_PATH + "/{version_uid}";
    public static final String EHR_STATUS_DEFAULT_RESPONSE = "{\n  \"_type\": \"EHR_STATUS\",\n  \"subject\": {\n    \"_type\": \"PARTY_SELF\"\n  },\n  \"is_queryable\": false,\n  \"is_modifiable\": true\n}";
    public static final String EHR_STATUS_REQUEST = "{\n      \"_type\": \"EHR_STATUS\",\n    \"subject\": {\n      \"_type\": \"PARTY_SELF\"\n    },\n    \"is_queryable\": true,\n    \"is_modifiable\": true\n}";
    public static final String EHR_DEFAULT_RESPONSE = "{\n    \"system_id\": {...},\n    \"ehr_id\": {...},\n    \"ehr_status\": {...},\n    \"time_created\", \"...\"\n}";
    public static final String VERSIONED_OBJECT_DEFAULT_RESPONSE = "{\n   \"uid\": \"_a_valid_uid_\",                     /* UUID or OID, UUID preferred */\n   \"owner_id\": \"{ehr_id}\",\n   \"time_created\": \"DV_DATE_TIME\"             /* ISO8601 YYYY-MM-DDThh:mm:ss.SSS(Z|[+-]hh:mm) e.g. 2017-08-01T01:06:46.000+00:00 */\n}";
    public static final String COMPOSITION_DEFAULT = "{\n  \"name\": {\n    \"value\": \"Vital Signs\"\n  },\n  \"uid\": {\n    \"_type\": \"OBJECT_VERSION_ID\",\n    \"value\": \"8849182c-82ad-4088-a07f-48ead4180515::example.domain.com::1\"\n  },\n  \"archetype_details\": {\n    \"archetype_id\": {\n      \"value\": \"openEHR-EHR-COMPOSITION.encounter.v1\"\n    },\n    \"template_id\": {\n      \"value\": \"Example.v1::c7ec861c-c413-39ff-9965-a198ebf44747\"\n    },\n    \"rm_version\": \"1.0.2\"\n  },\n  \"archetype_node_id\": \"openEHR-EHR-COMPOSITION.encounter.v1\",\n  \"language\": {\n    \"terminology_id\": {\n      \"value\": \"ISO_639-1\"\n    },\n    \"code_string\": \"en\"\n  },\n  \"territory\": {\n    \"terminology_id\": {\n      \"value\": \"ISO_3166-1\"\n    },\n    \"code_string\": \"NL\"\n  },\n  \"category\": {\n    \"value\": \"event\",\n    \"defining_code\": {\n      \"terminology_id\": {\n        \"value\": \"openehr\"\n      },\n      \"code_string\": \"433\"\n    }\n  },\n  \"composer\": {\n    \"_type\": \"PARTY_IDENTIFIED\",    \"external_ref\": {\n      \"id\": {\n        \"_type\": \"GENERIC_ID\",        \"value\": \"16b74749-e6aa-4945-b760-b42bdc07098a\"\n      },\n      \"namespace\": \"example.domain.com\",\n      \"type\": \"PERSON\"\n    },\n    \"name\": \"A name\"\n  },\n  \"context\": {\n    \"start_time\": {\n      \"value\": \"2014-11-18T09:50:35.000+01:00\"\n    },\n    \"setting\": {\n      \"value\": \"other care\",\n      \"defining_code\": {\n        \"terminology_id\": {\n          \"value\": \"openehr\"\n        },\n        \"code_string\": \"238\"\n      }\n    }\n  },\n  \"content\": []\n}";
    public static final String CONTRIBUTION_DEFAULT_REQUEST = "{\n  \"versions\": [\n    {\n      \"data\": {\n        \"_type\": \"COMPOSITION\",\n        \"name\": {\n          \"_type\": \"DV_TEXT\",\n          \"value\": \"Report\"\n        },\n        \"archetype_details\": {\n          \"_type\": \"ARCHETYPED\",\n          \"archetype_id\": {\n            \"_type\": \"ARCHETYPE_ID\",\n            \"value\": \"openEHR-EHR-COMPOSITION.report.v2\"\n          },\n          \"template_id\": {\n            \"_type\": \"TEMPLATE_ID\",\n            \"value\": \"HerzfrequenzTemplate\"\n          },\n          \"rm_version\": \"1.0.1\"\n        },\n        \"archetype_node_id\": \"openEHR-EHR-COMPOSITION.report.v2\",\n        \"language\": {\n          \"_type\": \"CODE_PHRASE\",\n          \"terminology_id\": {\n            \"_type\": \"TERMINOLOGY_ID\",\n            \"value\": \"ISO_639-1\"\n          },\n          \"code_string\": \"en\"\n        },\n        \"territory\": {\n          \"_type\": \"CODE_PHRASE\",\n          \"terminology_id\": {\n            \"_type\": \"TERMINOLOGY_ID\",\n            \"value\": \"ISO_3166-1\"\n          },\n          \"code_string\": \"DE\"\n        },\n        \"category\": {\n          \"_type\": \"DV_CODED_TEXT\",\n          \"value\": \"event\",\n          \"defining_code\": {\n            \"_type\": \"CODE_PHRASE\",\n            \"terminology_id\": {\n              \"_type\": \"TERMINOLOGY_ID\",\n              \"value\": \"openehr\"\n            },\n            \"code_string\": \"433\"\n          }\n        },\n        \"composer\": {\n          \"_type\": \"PARTY_IDENTIFIED\",\n          \"name\": \"Jane Nurse\"\n        },\n        \"context\": {\n          \"_type\": \"EVENT_CONTEXT\",\n          \"start_time\": {\n            \"_type\": \"DV_DATE_TIME\",\n            \"value\": \"2016-05-19T13:50:48.240+02:00\"\n          },\n          \"setting\": {\n            \"_type\": \"DV_CODED_TEXT\",\n            \"value\": \"other care\",\n            \"defining_code\": {\n              \"_type\": \"CODE_PHRASE\",\n              \"terminology_id\": {\n                \"_type\": \"TERMINOLOGY_ID\",\n                \"value\": \"openehr\"\n              },\n              \"code_string\": \"238\"\n            }\n          },\n          \"other_context\": {\n            \"_type\": \"ITEM_TREE\",\n            \"name\": {\n              \"_type\": \"DV_TEXT\",\n              \"value\": \"IZahl\"\n            },\n            \"archetype_node_id\": \"at0001\"\n          }\n        },\n        \"content\": [\n          {\n            \"_type\": \"OBSERVATION\",\n            \"name\": {\n              \"_type\": \"DV_TEXT\",\n              \"value\": \"*Pulse/Heart beat(en)\"\n            },\n            \"archetype_details\": {\n              \"_type\": \"ARCHETYPED\",\n              \"archetype_id\": {\n                \"_type\": \"ARCHETYPE_ID\",\n                \"value\": \"openEHR-EHR-OBSERVATION.pulse.v1\"\n              },\n              \"rm_version\": \"1.0.1\"\n            },\n            \"archetype_node_id\": \"openEHR-EHR-OBSERVATION.pulse.v1\",\n            \"language\": {\n              \"_type\": \"CODE_PHRASE\",\n              \"terminology_id\": {\n                \"_type\": \"TERMINOLOGY_ID\",\n                \"value\": \"ISO_639-1\"\n              },\n              \"code_string\": \"en\"\n            },\n            \"encoding\": {\n              \"_type\": \"CODE_PHRASE\",\n              \"terminology_id\": {\n                \"_type\": \"TERMINOLOGY_ID\",\n                \"value\": \"IANA_character-sets\"\n              },\n              \"code_string\": \"UTF-8\"\n            },\n            \"subject\": {\n              \"_type\": \"PARTY_SELF\"\n            },\n            \"provider\": {\n              \"_type\": \"PARTY_IDENTIFIED\",\n              \"name\": \"Dr. James Surgeon\"\n            },\n            \"protocol\": {\n              \"_type\": \"ITEM_TREE\",\n              \"name\": {\n                \"_type\": \"DV_TEXT\",\n                \"value\": \"*Extension(en)\"\n              },\n              \"archetype_node_id\": \"at0010\"\n            },\n            \"data\": {\n              \"_type\": \"HISTORY\",\n              \"name\": {\n                \"_type\": \"DV_TEXT\",\n                \"value\": \"History\"\n              },\n              \"archetype_node_id\": \"at0002\",\n              \"origin\": {\n                \"_type\": \"DV_DATE_TIME\",\n                \"value\": \"2016-05-19T13:50:48.240+02:00\"\n              },\n              \"events\": [\n                {\n                  \"_type\": \"POINT_EVENT\",\n                  \"name\": {\n                    \"_type\": \"DV_TEXT\",\n                    \"value\": \"*Any event(en)\"\n                  },\n                  \"archetype_node_id\": \"at0003\",\n                  \"time\": {\n                    \"_type\": \"DV_DATE_TIME\",\n                    \"value\": \"2016-05-19T13:50:48.240+02:00\"\n                  },\n                  \"data\": {\n                    \"_type\": \"ITEM_TREE\",\n                    \"name\": {\n                      \"_type\": \"DV_TEXT\",\n                      \"value\": \"*Clinical interpretation(en)\"\n                    },\n                    \"archetype_node_id\": \"at0001\",\n                    \"items\": [\n                      {\n                        \"_type\": \"ELEMENT\",\n                        \"name\": {\n                          \"_type\": \"DV_CODED_TEXT\",\n                          \"value\": \"Pulse\",\n                          \"defining_code\": {\n                            \"_type\": \"CODE_PHRASE\",\n                            \"terminology_id\": {\n                              \"_type\": \"TERMINOLOGY_ID\",\n                              \"value\": \"local\"\n                            },\n                            \"code_string\": \"at1026\"\n                          }\n                        },\n                        \"archetype_node_id\": \"at0004\",\n                        \"value\": {\n                          \"_type\": \"DV_QUANTITY\",\n                          \"magnitude\": 60.0,\n                          \"units\": \"/min\"\n                        }\n                      }\n                    ]\n                  },\n                  \"state\": {\n                    \"_type\": \"ITEM_TREE\",\n                    \"name\": {\n                      \"_type\": \"DV_TEXT\",\n                      \"value\": \"*Exertion(en)\"\n                    },\n                    \"archetype_node_id\": \"at0012\"\n                  }\n                }\n              ]\n            }\n          }\n        ]\n      },\n      \"lifecycleState\": 0,\n      \"commitAudit\": {\n        \"changeType\": \"CREATION\",\n        \"description\": \"Composition CREATE contribution\",\n        \"committer\": {\n          \"_type\": \"PARTY_IDENTIFIED\",\n          \"name\": \"NotJanezBananez\"\n        }\n      }\n    },\n    {\n      \"data\": {\n        \"_type\": \"FOLDER\",\n        \"name\": {\n          \"_type\": \"DV_TEXT\",\n          \"value\": \"subfolderName\"\n        },\n        \"folders\": [\n          {\n            \"_type\": \"FOLDER\",\n            \"name\": {\n              \"_type\": \"DV_TEXT\",\n              \"value\": \"parentFolderName\"\n            },\n            \"items\": [\n              {\n                \"_type\": \"OBJECT_REF\",\n                \"id\": {\n                  \"_type\": \"OBJECT_VERSION_ID\",\n                  \"value\": \"5606b19d-4392-4799-ba1a-196f6fef6535\"\n                },\n                \"namespace\": \"namespace\",\n                \"type\": \"ANY\"\n              }\n            ]\n          }\n        ],\n        \"items\": [\n          {\n            \"_type\": \"OBJECT_REF\",\n            \"id\": {\n              \"_type\": \"OBJECT_VERSION_ID\",\n              \"value\": \"72eabdb1-6bc3-44aa-b2e4-7550cb827ce2\"\n            },\n            \"namespace\": \"namespace\",\n            \"type\": \"ANY\"\n          }\n        ]\n      },\n      \"lifecycleState\": 0,\n      \"commitAudit\": {\n        \"changeType\": \"CREATION\",\n        \"description\": \"Folder CREATE contribution\",\n        \"committer\": {\n          \"_type\": \"PARTY_IDENTIFIED\",\n          \"name\": \"NotNotJanezBananez\"\n        }\n      }\n    },\n    {\n      \"data\": {\n        \"_type\": \"EHR_STATUS\",\n        \"subject\": {\n          \"_type\": \"PARTY_SELF\"\n        },\n        \"is_queryable\": false,\n        \"is_modifiable\": true\n      },\n      \"lifecycleState\": 0,\n      \"commitAudit\": {\n        \"changeType\": \"MODIFICATION\",\n        \"description\": \"EhrStatus CREATE contribution\"\n      }\n    }\n  ],\n  \"audit\": {\n    \"commiter\": {\n      \"_type\": \"PARTY_IDENTIFIED\",\n      \"name\": \"JanezBananez\"\n    }\n  }\n}";
    public static final String QUERY_DEFAULT_REQUEST = "{\n  \"q\": \"select o/data[at0002]/events[at0003 and name/value='Any event']/data[at0001]/items[at0004]/value/magnitude as temperature, o/data[at0002]/events[at0003 and name/value='Any event']/data[at0001]/items[at0004]/value/units as unit from EHR[ehr_id/value='0cf8371c-64ab-4e8a-adf1-af35a7b4da01'] CONTAINS Observation o[openEHR-EHR-OBSERVATION.body_temperature-zn.v1] WHERE o/data[at0002]/events[at0003 and name/value='Any event']/data[at0001]/items[at0004]/value/magnitude > 37.1 ORDER BY temperature desc\"\n}";
    public static final String STORED_QUERY_DEFAULT_REQUEST = "{\n  \"q\": \"select o/data[at0002]/events[at0003 and name/value='Any event']/data[at0001]/items[at0004]/value/magnitude as temperature, o/data[at0002]/events[at0003 and name/value='Any event']/data[at0001]/items[at0004]/value/units as unit from EHR[ehr_id/value='b36befc1-d577-40d4-a4d7-8aef4ebe7d95'] CONTAINS Observation o[openEHR-EHR-OBSERVATION.body_temperature-zn.v1] WHERE o/data[at0002]/events[at0003 and name/value='Any event']/data[at0001]/items[at0004]/value/magnitude > 37.1 ORDER BY temperature desc\",\n  \"description\": \"test\"\n}";
    public static final String FOLDER_DEFAULT_REQUEST = "{\n  \"_type\": \"FOLDER\",\n  \"name\": {\n    \"_type\": \"DV_TEXT\",\n    \"value\": \"NewName\"\n  },\n  \"folders\": [\n    {\n      \"_type\": \"FOLDER\",\n      \"name\": {\n        \"_type\": \"DV_TEXT\",\n        \"value\": \"folder1/F1\"\n      }\n    }\n  ]\n}";
    private static final String HEADER_EHR_ID = "openEHR-EHR-id";
    private static final String GET_COMPOSITION_REVISION_HISTORY_PATH = "/ehr/{ehr_id}/versioned_composition/{versioned_object_uid}/revision_history";
    private static final String EHR_ID_CONFLICT_MESSAGE = "Ehr Id conflict! Check header \"openEHR-EHR-id\", ehr_id request parameter or ehr_id query parameter.";
    private static final String INVALID_VERSION_MESSAGE = "Only major version numbers are supported.";

    private OpenEhrConstants() {
    }

    private static boolean isFullRepresentation(HttpHeaders headers) {
        List<String> preferHeaderValues = headers.get("Prefer");
        String value = preferHeaderValues != null && !preferHeaderValues.isEmpty() ? preferHeaderValues.iterator().next() : null;
        if (value != null) {
            value = value.replace("return=", "");
            return Arrays.stream(value.split(",")).map(String::trim).anyMatch(s -> Objects.equals(s, "representation"));
        }
        return false;
    }

    public static boolean isResolveReferences(HttpHeaders headers) {
        List<String> preferHeaderValues = headers.get("Prefer");
        String value = preferHeaderValues != null && !preferHeaderValues.isEmpty() ? preferHeaderValues.iterator().next() : null;
        if (value != null) {
            value = value.replace("return=", "");
            return Arrays.stream(value.split(",")).map(String::trim).anyMatch(s -> Objects.equals(s, "resolve_refs"));
        }
        return false;
    }


    public static <T> ResponseEntity<T> createHeadersEnrichedResponse(
            @Nullable HttpStatus overridingStatus,
            @Nullable ResultWithContributionWrapper<?> commitResult,
            HttpHeaders headers,
            String path,
            Supplier<T> supplier,
            String... ids) {
        HttpHeaders h = createMandatoryHeaders(path, commitResult, ids);

        if (overridingStatus != null) {
            ResponseEntity.BodyBuilder builder = ResponseEntity.status(overridingStatus).headers(h);
            if (isFullRepresentation(headers)) {
                return builder.body(supplier.get());
            }
            return builder.build();
        }
        return isFullRepresentation(headers)
                ? ResponseEntity.ok().headers(h).body(supplier.get())
                : ResponseEntity.noContent().headers(h).build();
    }

    public static HttpHeaders createMandatoryHeaders(String locationPath, String... placeholders) {
        return createMandatoryHeaders(locationPath, null, placeholders);
    }

    public static HttpHeaders createMandatoryHeaders(String locationPath, @Nullable ResultWithContributionWrapper<?> commitResult, String... placeholders) {
        return createMandatoryHeaders(locationPath, commitResult, false, placeholders);
    }

    public static HttpHeaders createMandatoryHeaders(
            String locationPath,
            @Nullable ResultWithContributionWrapper<?> wrapper,
            boolean locationOnly,
            String... placeholders) {
        HttpHeaders h = new HttpHeaders();

        if (wrapper != null && wrapper.getContributionUidCommittedTimestampMap().size() == 1) {
            Map<String, OffsetDateTime> timestampMap = wrapper.getContributionUidCommittedTimestampMap();
            OffsetDateTime committedtimestamp = timestampMap.entrySet().iterator().next().getValue();
            if (committedtimestamp != null) {
                h.setLastModified(committedtimestamp.toInstant().toEpochMilli());
            }
        }
        UriComponents build = ServletUriComponentsBuilder
                .fromCurrentServletMapping()
                .path(OPEN_EHR_PATH)
                .path(locationPath)
                .build();
        if (placeholders != null) {
            if (!locationOnly && placeholders.length > 0) {
                String etag = placeholders[placeholders.length - 1];
                if (!etag.startsWith("\"") && !etag.startsWith("W/\"")) {
                    etag = '"' + etag + '"';
                }
                h.setETag(etag);
            }
            build = build.expand((Object[])placeholders);
        }
        URI location = build.toUri();
        h.setLocation(location);

        getOpenEhrUri(locationPath, placeholders).ifPresent(openEhrUri -> h.set(OpenEhrUri.URL_HEADER_NAME, openEhrUri));

        return h;
    }

    private static Map<String, String> getAndMapHeaderValues(HttpHeaders headers, String key) {
        List<String> values = headers.get(key);
        return values != null ? values.stream().map(s -> s.split("=")).collect(toMap(parts -> parts[0], parts -> parts[1])) : Collections.emptyMap();
    }

    public static String getRequestHeaderEhrId(HttpHeaders headers) {
        List<String> values = headers.get(HEADER_EHR_ID);
        if (values == null || values.isEmpty()) {
            return null;
        }

        if (values.size() == 1) {
            return values.get(0).replaceAll("\"", "");
        } else {
            throw new OpenEhrRestException(BAD_REQUEST, EHR_ID_CONFLICT_MESSAGE);
        }
    }

    private static Optional<String> getOpenEhrUri(String locationPath, String... placeholders) {
        if (EHR_PATH.equals(locationPath)) {
            return Optional.of(OpenEhrUriFormat.format(placeholders[0], null, null));
        } else if (GET_EHR_STATUS_PATH.equals(locationPath)) {
            return Optional.of(OpenEhrUriFormat.format(placeholders[0], OpenEhrStructureType.STATUS, null));
        } else if (GET_FOLDER_PATH.equals(locationPath)) {
            return Optional.of(OpenEhrUriFormat.format(placeholders[0], OpenEhrStructureType.DIRECTORY, null));
        } else if (GET_COMPOSITION_PATH.equals(locationPath)) {
            return Optional.of(OpenEhrUriFormat.format(placeholders[0], OpenEhrStructureType.COMPOSITION, placeholders[1]));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Parse SemVer ({@code major.minor.patch}) where only major version is required and minor and patch versions are optional. Major version starts with 0.
     * If supplied, minor nad patch versions must be equal to zero. Possible inputs are 0, 1, 1.0, 1.0.0.
     * Minor and patch versions aren't supported because current versioning is based on JPA versioning.
     *
     * @param semanticVersion String value of SemVer
     * @return major version number
     * @throws OpenEhrRestException if {@code semanticVersion} cannot be parsed. This could happen if major version is not a number or if optional minor or
     *                              patch versions aren't equal to 0.
     */
    public static Integer getVersionNumber(String semanticVersion) {
        if (semanticVersion == null) {
            return null;
        }

        String[] majorMinorAndPatch = semanticVersion.split("\\.");
        String majorVersion = majorMinorAndPatch[0];

        // validate minor version
        String minorVersion = majorMinorAndPatch.length > 1 ? majorMinorAndPatch[1] : null;
        if (minorVersion != null && parseVersionNumber(minorVersion) != 0) {
            throw new OpenEhrRestException(BAD_REQUEST, INVALID_VERSION_MESSAGE);
        }

        // validate patch version
        String patchVersion = majorMinorAndPatch.length > 2 ? majorMinorAndPatch[2] : null;
        if (patchVersion != null && parseVersionNumber(patchVersion) != 0) {
            throw new OpenEhrRestException(BAD_REQUEST, INVALID_VERSION_MESSAGE);
        }

        return parseVersionNumber(majorVersion);
    }

    private static Integer parseVersionNumber(String majorVersion) {
        try {
            return Integer.parseInt(majorVersion);
        } catch (NumberFormatException e) {
            throw new OpenEhrRestException(BAD_REQUEST, INVALID_VERSION_MESSAGE);
        }
    }
}
