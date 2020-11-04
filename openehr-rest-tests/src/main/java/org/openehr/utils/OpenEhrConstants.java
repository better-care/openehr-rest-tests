/*
 * Copyright 2020-2021 Better Ltd (www.better.care)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.openehr.utils;

import org.openehr.data.OpenEhrStructureType;
import org.openehr.data.OpenEhrUri;
import org.openehr.data.OpenEhrUriFormat;
import org.openehr.data.ResultWithContributionWrapper;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import javax.annotation.Nullable;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * @author Dusan Markovic
 */
public final class OpenEhrConstants {

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

    private OpenEhrConstants() {
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

}
