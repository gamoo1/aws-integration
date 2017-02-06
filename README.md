### Amazon Web Services Integration POC ###
* Spring Boot application showing Amazon SNS and SQS integration with Spring JMS. 
 
* JMS is used as a listener for the SQS instance. Instead of directly invoking polling, I am using Amazons SQS Java 
Messaging library and integrating with Spring JMS. Out of the box configuration is set up such that the default sns 
topic has a valid subscription (Default Queue), which is configured with a listener. Once application has been started, 
you can publish messages to the default topic and they will be logged by the listener automatically. 

#### System Requirement ####
* Need to create local variable - 'AWS_CREDENTIALS' which will contain the path including filename of the aws properties 
file that contains the secret key and secret access key. Alternately you can directly provide secret key and secret 
access key in the properties file, however you need to delete the existing value for 'aws.api.credentials.file.path' key

#### Running the application ####
* Run 'gradle build' from the project directory to build project
* Run 'gradle bootrun' from the project directory to run project and navigate to localhost:6969/swagger-ui.html 
for documented endpoints
* If you dont have gradle installed locally, you can use the gradle wrapper tasks instead ('gradlew build' etc)