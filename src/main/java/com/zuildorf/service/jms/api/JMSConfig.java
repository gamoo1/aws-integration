package com.zuildorf.service.jms.api;

import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.auth.*;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

/**
 * Created by Milos Leposavic.
 */
@Configuration
@EnableJms
public class JMSConfig {
    private Logger log = Logger.getLogger(JMSConfig.class);
    private SQSConnectionFactory sqsConnectionFactory;
    private SQSListenerService sqsListener;

    @Autowired
    public JMSConfig(@Value("${aws.api.credentials.file.path}") String apiProperties,
                     @Value("${aws.accessKey}") String accessKey,
                     @Value("${aws.secretKey}") String secretKey,
                     @Value("${sqs.default.queue.name}") String queueName,
                     @Autowired SQSListenerService sqsListener) {

        if (!StringUtils.isBlank(apiProperties)) {
            sqsConnectionFactory = SQSConnectionFactory.builder()
                    .withAWSCredentialsProvider(new PropertiesFileCredentialsProvider(apiProperties))
                    .withRegion(Region.getRegion(Regions.US_WEST_2))
                    .withNumberOfMessagesToPrefetch(10)
                    .build();
        } else if (!StringUtils.isBlank(accessKey) && !StringUtils.isBlank(secretKey)) {
            sqsConnectionFactory = SQSConnectionFactory.builder()
                    .withAWSCredentialsProvider((AWSCredentialsProvider) new BasicAWSCredentials(accessKey, secretKey))
                    .withRegion(Region.getRegion(Regions.US_WEST_2))
                    .withNumberOfMessagesToPrefetch(10)
                    .build();
        } else {
            log.info("Unable to get aws credential properties, creating \"anonymous\" credentials. Request will not be signed.");
            sqsConnectionFactory = SQSConnectionFactory.builder()
                    .withAWSCredentialsProvider(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))
                    .withRegion(Region.getRegion(Regions.US_WEST_2))
                    .withNumberOfMessagesToPrefetch(10)
                    .build();
        }
        this.sqsListener = sqsListener;
    }

    @Bean
    public DefaultMessageListenerContainer jmsListenerContainer(@Value("${sqs.default.queue.name}") String queueName) {
        DefaultMessageListenerContainer dmlc = new DefaultMessageListenerContainer();
        dmlc.setConnectionFactory(sqsConnectionFactory);
        dmlc.setDestinationName(queueName);
        dmlc.setMessageListener(sqsListener);
        return dmlc;
    }
}
