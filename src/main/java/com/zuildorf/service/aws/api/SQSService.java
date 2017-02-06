package com.zuildorf.service.aws.api;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.Principal;
import com.amazonaws.auth.policy.Resource;
import com.amazonaws.auth.policy.Statement;
import com.amazonaws.auth.policy.actions.SQSActions;
import com.amazonaws.auth.policy.conditions.ArnCondition;
import com.amazonaws.auth.policy.conditions.ConditionFactory;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.*;
import com.zuildorf.error.AwsApiException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by Milos Leposavic.
 */
@Component
public class SQSService {
    private Logger log = Logger.getLogger(SQSService.class);
    private final AmazonSQSClient sqsClient;
    private final String defaultQueueName;
    private final String defaultQueueUrl;
    private final String defaultQueueArn;
    private SNSService snsService;

    @Autowired
    public SQSService(@Value("${aws.api.credentials.file.path}") String apiProperties,
                      @Value("${aws.region}") String region,
                      @Value("${aws.accessKey}") String accessKey,
                      @Value("${aws.secretKey}") String secretKey,
                      @Value("${sqs.default.queue.name}") String defaultQueueName,
                      SNSService sns) {
        snsService = sns;
        try {
            if (StringUtils.isNotBlank(apiProperties)) {
                sqsClient = new AmazonSQSClient(new PropertiesFileCredentialsProvider(apiProperties))
                        .withRegion(Region.getRegion(Regions.fromName(region)));
            } else if (StringUtils.isNotBlank(accessKey) && StringUtils.isNotBlank(secretKey)) {
                sqsClient = new AmazonSQSClient(new BasicAWSCredentials(accessKey, secretKey))
                        .withRegion(Region.getRegion(Regions.fromName(region)));
            } else {
                log.info("Unable to get aws credential properties, creating \"anonymous\" credentials. Request will not be signed.");
                sqsClient = new AmazonSQSClient(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))
                        .withRegion(Region.getRegion(Regions.fromName(region)));
            }

            this.defaultQueueName = defaultQueueName;
            defaultQueueUrl = sqsClient.createQueue(defaultQueueName).getQueueUrl();
            log.info("Default queue url: " + defaultQueueUrl);

            defaultQueueArn = sqsClient.getQueueAttributes(new GetQueueAttributesRequest(defaultQueueUrl)
                    .withAttributeNames(new ArrayList<>(Collections.singletonList("All"))))
                    .getAttributes().get(("QueueArn"));
            log.info("Default queue arn: " + defaultQueueArn);

            // subscribing default queue to topic
            subscribeDefaultQueueToDefaultTopic();
        } catch (AmazonServiceException e) {
            throw new AwsApiException(String.valueOf(e.getStatusCode()), e);
        }
    }

    public String createQueue(String queueName) throws AwsApiException {
        try {
            log.info("Creating SQS queue: " + queueName);
            CreateQueueResult createQueueResult = sqsClient.createQueue(new CreateQueueRequest(queueName));
            log.info("Created queue url: " + createQueueResult.getQueueUrl());
            return createQueueResult.getQueueUrl();
        } catch (AmazonServiceException e) {
            throw new AwsApiException(String.valueOf(e.getStatusCode()), e);
        }
    }

    public Map<String, String> getQueueInfo(String queueURL) throws AwsApiException {
        try {
            GetQueueAttributesRequest request = new GetQueueAttributesRequest()
                    .withQueueUrl(queueURL)
                    .withAttributeNames(new ArrayList<>(Collections.singletonList("All")));
            GetQueueAttributesResult result = sqsClient.getQueueAttributes(request);

            return result.getAttributes();
        } catch (AmazonServiceException e) {
            throw new AwsApiException(String.valueOf(e.getStatusCode()), e);
        }
    }

    public List<String> listQueues() throws AwsApiException {
        try {
            ListQueuesResult result = sqsClient.listQueues(new ListQueuesRequest());
            return result.getQueueUrls();
        } catch (AmazonServiceException e) {
            throw new AwsApiException(String.valueOf(e.getStatusCode()), e);
        }
    }

    public String deleteQueue(String queueURL) throws AwsApiException {
        try {
            log.info("Deleting Amazon SQS Queue. Queue URL: " + queueURL);

            String resp = sqsClient.deleteQueue(new DeleteQueueRequest(queueURL))
                    .getSdkResponseMetadata()
                    .toString();
            log.info("Queue deleted successfully: " + resp);
            return resp;
        } catch (AmazonServiceException e) {
            throw new AwsApiException(String.valueOf(e.getStatusCode()), e);
        }
    }

    private String subscribeDefaultQueueToDefaultTopic() throws AwsApiException {
        String queueArn;
        try {
            // before subscribing, policy must be set on sqs queue, to allow the topic to send messages
            Policy policy = new Policy("AllowTopicToSendMessagesToSQS")
                    .withStatements(
                            new Statement(Statement.Effect.Allow)
                                    .withPrincipals(Principal.All)
                                    .withActions(SQSActions.SendMessage)
                                    .withResources(new Resource(defaultQueueArn))
                                    .withConditions(new ArnCondition(ArnCondition.ArnComparisonType.ArnEquals, ConditionFactory.SOURCE_ARN_CONDITION_KEY, snsService.getDefaultTopicArn()))
                    );
            Map<String, String> attributes = new HashMap<>();
            attributes.put("Policy", policy.toJson());
            SetQueueAttributesResult setQueueAttributesResult = sqsClient.setQueueAttributes(defaultQueueUrl, attributes);
            log.info("Created policy allowing the sending of messages from the topic to the queue: " + setQueueAttributesResult.toString());

            // after setting the policy, subscribe the queue
            return snsService.subscribeToTopic(snsService.getDefaultTopicArn(), "sqs", defaultQueueArn);
        } catch (AmazonServiceException e) {
            throw new AwsApiException(String.valueOf(e.getStatusCode()), e);
        }
    }

    public String getDefaultQueueName() {
        return defaultQueueName;
    }

    public String getDefaultQueueUrl() {
        return defaultQueueUrl;
    }

    public String getDefaultQueueArn() {
        return defaultQueueArn;
    }
}
