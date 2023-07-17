package nz.co.blink.debit.service;

import nz.co.blink.debit.exception.BlinkServiceException;

public interface ValidationService {
    /**
     * Validates the constructed request before sending to the API to reduce calls.
     *
     * @param type    the request type
     * @param payload the request to be validated
     * @throws BlinkServiceException thrown if the request is invalid
     */
    void validateRequest(String type, Object payload) throws BlinkServiceException;
}
