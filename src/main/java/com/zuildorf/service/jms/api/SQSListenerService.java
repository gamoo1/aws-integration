package com.zuildorf.service.jms.api;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

/**
 * Created by Milos Leposavic.
 */
@Component
public class SQSListenerService implements MessageListener {
    private Logger log = Logger.getLogger(SQSListenerService.class);

    @Override
    public void onMessage(Message message) {
        try {
            TextMessage textMessage = (TextMessage) message;
            String messageString = textMessage.getText();
            log.info("Received message " + messageString);
        } catch (JMSException e) {
            String errorMessage = "Error processing message: " + e.getMessage();
            log.error(errorMessage, e);
        } catch (Exception e) {
            String errorMessage = "Exception occurred: " + e.getMessage();
            log.error(errorMessage);
        }
    }
}
