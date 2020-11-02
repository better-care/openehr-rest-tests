/*
 * Copyright (C) 2020 Better d.o.o.
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

package org.openehr.rest.serialize;

import care.better.platform.json.OpenEhrObjectMapper;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

/**
 * @author matijak
 * @since 08.01.2014
 */
public class BetterObjectMapper extends OpenEhrObjectMapper {

    private static final long serialVersionUID = 1L;

    public BetterObjectMapper() {
        registerModule(new JodaModule());
        registerModule(new JavaTimeModule());
        registerModule(new AfterburnerModule());

        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        setDefaultPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, JsonInclude.Include.USE_DEFAULTS));

        // OpenEhr specification: 400 Bad Request is returned when the request has invalid content (e.g. content could not be converted to a valid directory
        // FOLDER)
        configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, true);
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    }
}
