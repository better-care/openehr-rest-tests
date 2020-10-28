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
