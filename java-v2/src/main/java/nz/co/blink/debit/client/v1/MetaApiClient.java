package nz.co.blink.debit.client.v1;

import nz.co.blink.debit.dto.v1.BankMetadata;
import nz.co.blink.debit.exception.BlinkServiceException;
import nz.co.blink.debit.helpers.HttpClientHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

/**
 * Client for metadata operations.
 */
public class MetaApiClient {

    private static final Logger log = LoggerFactory.getLogger(MetaApiClient.class);
    private static final String META_PATH = "/payments/v1/meta";

    private final HttpClientHelper httpHelper;

    public MetaApiClient(HttpClientHelper httpHelper) {
        this.httpHelper = httpHelper;
    }

    /**
     * Get bank metadata.
     *
     * @return the list of bank metadata
     * @throws BlinkServiceException if the request fails
     */
    public List<BankMetadata> getMeta() throws BlinkServiceException {
        return getMeta(UUID.randomUUID().toString());
    }

    /**
     * Get bank metadata with custom request ID.
     *
     * @param requestId the request ID for tracing
     * @return the list of bank metadata
     * @throws BlinkServiceException if the request fails
     */
    public List<BankMetadata> getMeta(String requestId) throws BlinkServiceException {
        log.debug("Getting metadata with request-id: {}", requestId);
        return httpHelper.getList(META_PATH, BankMetadata.class, requestId);
    }
}
