package com.zuildorf.controller;

import com.amazonaws.services.sns.model.Topic;
import com.zuildorf.error.AwsApiException;
import com.zuildorf.service.aws.api.SNSService;
import io.swagger.annotations.ApiOperation;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by Milos Leposavic.
 */
@RestController
@RequestMapping("/aws-integration/sns")
public class SNSController {
    private Logger log = Logger.getLogger(SNSController.class);
    private final SNSService snsService;

    @Autowired
    public SNSController(SNSService snsService) {
        this.snsService = snsService;
    }

    @GetMapping()
    @ApiOperation(value = "Hello Amazon SNS! List default topic information...")
    public ResponseEntity helloWorld() {
        String message = String.format("Default topic name: %s\nDefault topic arn: %s", snsService.getDefaultTopicName(), snsService.getDefaultTopicArn());
        return ResponseEntity.ok(message);
    }

    @GetMapping("/list")
    @ApiOperation(value = "Return all SNS topics")
    public ResponseEntity listTopics() {
        try {
            List<Topic> listTopicsResult = snsService.listTopics();
            log.info("Topics retrieved successfully.");
            return ResponseEntity.ok(listTopicsResult);
        } catch (AwsApiException e) {
            String errorMessage = "Error getting topics: " + e.getCause().getMessage();
            log.error(errorMessage);
            return new ResponseEntity<>(errorMessage, HttpStatus.valueOf(Integer.parseInt(e.getMessage())));
        }
    }

    @PostMapping("/create")
    @ApiOperation(value = "Create an Amazon SNS Topic with a given name")
    public ResponseEntity createTopic(@RequestParam String topicName) {
        try {
            String createTopicResult = snsService.createTopic(topicName);
            log.info("Created topic successfully: " + createTopicResult);
            return ResponseEntity.ok(createTopicResult);
        } catch (AwsApiException e) {
            String errorMessage = "Error creating topic: " + e.getCause().getMessage();
            log.error(errorMessage);
            return new ResponseEntity<>(errorMessage, HttpStatus.valueOf(Integer.parseInt(e.getMessage())));
        }
    }

    @DeleteMapping("/delete")
    @ApiOperation(value = "Delete an Amazon SNS Topic using its ARN")
    public ResponseEntity deleteTopic(@RequestParam String topicARN) {
        try {
            String deleteTopicResult = snsService.deleteTopic(topicARN);
            log.info("Deleted topic successfully: " + deleteTopicResult);
            return ResponseEntity.ok(deleteTopicResult);
        } catch (AwsApiException e) {
            String errorMessage = "Error deleting topic: " + e.getCause().getMessage();
            log.error(errorMessage);
            return new ResponseEntity<>(errorMessage, HttpStatus.valueOf(Integer.parseInt(e.getMessage())));
        }
    }
}