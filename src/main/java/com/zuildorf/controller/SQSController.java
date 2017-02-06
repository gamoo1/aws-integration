package com.zuildorf.controller;

import com.zuildorf.error.AwsApiException;
import com.zuildorf.service.aws.api.SQSService;
import io.swagger.annotations.ApiOperation;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Created by Milos Leposavic.
 */
@RestController
@RequestMapping("/aws-integration/sqs")
public class SQSController {
    private Logger log = Logger.getLogger(SQSController.class);
    private final SQSService sqsService;

    @Autowired
    public SQSController(SQSService sqsService) {
        this.sqsService = sqsService;
    }

    @GetMapping()
    @ApiOperation(value = "Hello Amazon SQS! List default sqs information...")
    public ResponseEntity helloWorld() {
        return ResponseEntity.ok(sqsService.getQueueInfo(sqsService.getDefaultQueueUrl()));
    }

    @PostMapping("/create")
    @ApiOperation(value = "Create an Amazon SQS queue with a given name")
    public ResponseEntity createSqsInstance(@RequestParam String queueName) {
        try {
            String createQueueResult = sqsService.createQueue(queueName);
            log.info("SQS queue created successfully: " + createQueueResult);
            return ResponseEntity.ok(createQueueResult);
        } catch (AwsApiException e) {
            String errorMessage = "Error creating queue: " + e.getCause().getMessage();
            log.error(errorMessage);
            return new ResponseEntity<>(errorMessage, HttpStatus.valueOf(Integer.parseInt(e.getMessage())));
        }
    }

    @GetMapping("/getInfo")
    @ApiOperation(value = "Get specific SQS queue info, including queue arn")
    public ResponseEntity getQueueInfo(@RequestParam String queueURL) {
        try {
            Map<String, String> queueInfo = sqsService.getQueueInfo(queueURL);
            log.info("Successfully retrieved queue info.");
            return ResponseEntity.ok(queueInfo);
        } catch (AwsApiException e) {
            String errorMessage = "Error retrieving queue info: " + e.getCause().getMessage();
            log.error(errorMessage);
            return new ResponseEntity<>(errorMessage, HttpStatus.valueOf(Integer.parseInt(e.getMessage())));
        }
    }

    @GetMapping("/list")
    @ApiOperation(value = "Return URL for all SQS instances")
    public ResponseEntity listAllSQS() {
        try {
            List<String> listQueuesResult = sqsService.listQueues();
            log.info("Successfully retrieved sqs instances.");
            return ResponseEntity.ok(listQueuesResult);
        } catch (AwsApiException e) {
            String errorMessage = "Error retrieving all queues: " + e.getCause().getMessage();
            log.error(errorMessage);
            return new ResponseEntity<>(errorMessage, HttpStatus.valueOf(Integer.parseInt(e.getMessage())));
        }
    }

    @DeleteMapping("/delete")
    @ApiOperation(value = "Delete an Amazon SQS queue using its URL")
    public ResponseEntity deleteSQS(@RequestParam String queueURL) {
        try {
            String deleteQueueResult = sqsService.deleteQueue(queueURL);
            log.info("Queue deleted successfully: " + deleteQueueResult);
            return ResponseEntity.ok(deleteQueueResult);
        } catch (AwsApiException e) {
            String errorMessage = "Error deleting queue: " + e.getCause().getMessage();
            log.error(errorMessage);
            return new ResponseEntity<>(errorMessage, HttpStatus.valueOf(Integer.parseInt(e.getMessage())));
        }
    }
}
