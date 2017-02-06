package com.zuildorf.service.aws.api;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.*;
import com.zuildorf.error.AwsApiException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * Created by Milos Leposavic.
 */
@Component
public class SNSService {
    private Logger log = Logger.getLogger(SNSService.class);
    private final String defaultTopicName;
    private final String defaultTopicArn;
    private final AmazonSNSClient snsClient;

    @Autowired
    public SNSService(@Value("${aws.api.credentials.file.path}") String apiProperties,
                      @Value("${aws.region}") String region,
                      @Value("${aws.accessKey}") String accessKey,
                      @Value("${aws.secretKey}") String secretKey,
                      @Value("${sns.default.topic.name}") String defaultTopicName) {
        this.defaultTopicName = defaultTopicName;
        try {
            if (StringUtils.isNotBlank(apiProperties)) {
                snsClient = new AmazonSNSClient(new PropertiesFileCredentialsProvider(apiProperties))
                        .withRegion(Region.getRegion(Regions.fromName(region)));
            } else if (StringUtils.isNotBlank(accessKey) && StringUtils.isNotBlank(secretKey)) {
                snsClient = new AmazonSNSClient(new BasicAWSCredentials(accessKey, secretKey))
                        .withRegion(Region.getRegion(Regions.fromName(region)));
            } else {
                log.info("Unable to get aws credential properties, creating \"anonymous\" credentials. Request will not be signed.");
                snsClient = new AmazonSNSClient(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))
                        .withRegion(Region.getRegion(Regions.fromName(region)));
            }
            defaultTopicArn = snsClient.createTopic(new CreateTopicRequest(defaultTopicName))
                    .getTopicArn();
        } catch (AmazonServiceException e) {
            throw new AwsApiException(String.valueOf(e.getStatusCode()), e);
        }
    }

    public String createTopic(String topicName) throws AwsApiException {
        try {
            log.info("Creating a new AWS SNS topic called: " + topicName);
            CreateTopicRequest createTopicRequest = new CreateTopicRequest(topicName);

            String createTopicArn = snsClient.createTopic(createTopicRequest).getTopicArn();
            log.info("Created Topic ARN: " + createTopicArn);
            return createTopicArn;
        } catch (AmazonServiceException e) {
            throw new AwsApiException(String.valueOf(e.getStatusCode()), e);
        }
    }

    public String deleteTopic(String topicARN) throws AwsApiException {
        try {
            log.info("Deleting topic with ARN: " + topicARN);
            DeleteTopicRequest deleteTopicRequest = new DeleteTopicRequest().withTopicArn(topicARN);

            DeleteTopicResult deleteTopicResult = snsClient.deleteTopic(deleteTopicRequest);
            log.info("Topic deleted successfully.");
            return snsClient.getCachedResponseMetadata(deleteTopicRequest).toString();
        } catch (AmazonServiceException e) {
            throw new AwsApiException(String.valueOf(e.getStatusCode()), e);
        }
    }

    public List<Topic> listTopics() throws AwsApiException {
        try {
            log.info("Retrieving all SNS topics.");
            ListTopicsResult listTopicsResult = snsClient.listTopics(new ListTopicsRequest());
            return listTopicsResult.getTopics();
        } catch (AmazonServiceException e) {
            throw new AwsApiException(String.valueOf(e.getStatusCode()), e);
        }
    }

    String subscribeToTopic(String topicARN, String protocol, String subscriberARN) {
        SubscribeRequest subRequest = new SubscribeRequest(topicARN, protocol, subscriberARN);
        String subscriptionArn;
        try {
            subscriptionArn = snsClient.subscribe(subRequest).getSubscriptionArn();
            log.info("SubscribeResult info: " + subscriptionArn);
        } catch (AmazonServiceException e) {
            throw new AwsApiException(String.valueOf(e.getStatusCode()), e);
        }
        return subscriptionArn;
    }

    public String publishMessageToTopic(String topicARN, String subject, String message) throws AwsApiException {
        try {
            PublishRequest publishRequest = new PublishRequest()
                    .withTopicArn(topicARN)
                    .withSubject(subject)
                    .withMessage(message);

            PublishResult publishResult = snsClient.publish(publishRequest);
            log.info("Published message successfully. Message: " + publishRequest.getMessage());
            return publishResult.toString();
        } catch (AmazonServiceException e) {
            throw new AwsApiException(String.valueOf(e.getStatusCode()), e);
        }
    }

    public AmazonSNSClient getSnsClient() {
        return snsClient;
    }

    public String getDefaultTopicName() {
        return defaultTopicName;
    }

    public String getDefaultTopicArn() {
        return defaultTopicArn;
    }


}
