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

package org.openehr.rest.exception;

import org.openehr.data.OpenEhrErrorResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;


/**
 * @author Dusan Markovic
 */
public class OpenEhrRestException extends HttpClientErrorException {
    private static final long serialVersionUID = 1L;

    private OpenEhrErrorResponse errorResponse;

    public OpenEhrRestException(HttpStatus statusCode) {
        super(statusCode);
    }

    public OpenEhrRestException(HttpStatus status, HttpHeaders headers) {
        super(status, status.getReasonPhrase(), headers, null, null);
    }

    public OpenEhrRestException(HttpStatus statusCode, String statusText) {
        super(statusCode, statusText);
        errorResponse = new OpenEhrErrorResponse(statusText);
    }

    public OpenEhrRestException(HttpStatus statusCode, String message, HttpHeaders headers, List<String> validationErrors) {
        super(statusCode, message, headers, null, null);
        errorResponse = new OpenEhrErrorResponse(message);
        errorResponse.setValidationErrors(validationErrors);
    }

    public OpenEhrRestException(HttpStatus notFound, Exception e) {
        super(notFound, e.getMessage());
    }

    public OpenEhrErrorResponse getErrorResponse() {
        return errorResponse;
    }
}
