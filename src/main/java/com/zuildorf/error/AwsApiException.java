package com.zuildorf.error;

import com.amazonaws.AmazonServiceException;
import org.apache.log4j.Logger;

/**
 * Created by Milos Leposavic.
 */
public class AwsApiException extends RuntimeException {
    private static Logger log = Logger.getLogger(AwsApiException.class);

    public AwsApiException(String message, Throwable cause) {
        super(message, cause);
        if (cause instanceof AmazonServiceException) {
            log.error("Amazon Service Exception occurred. Details below...");
            log.error("Error Message:    " + cause.getMessage());
            log.error("HTTP Status Code: " + ((AmazonServiceException) cause).getStatusCode());
            log.error("AWS Error Code:   " + ((AmazonServiceException) cause).getErrorCode());
            log.error("Error Type:       " + ((AmazonServiceException) cause).getErrorType());
            log.error("Request ID:       " + ((AmazonServiceException) cause).getRequestId());
        }
    }
}
