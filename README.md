# Spring Cloud Stream Security Example

The purpose of this POC is to demonstrate how Spring Security Oauth2 can be used with Spring Cloud Stream to secure messages so that rogue apps
 cannot inject malicious messages into a system that uses SCSt.  This example uses a JWT token to validate the message, and a digital signature to
  ensure that the payload has not been tampered with.
  
This POC is designed to run locally on a laptop or desktop.  It uses Kafka as the messaging layer, although it does not depend on Kafka.  RabbitMQ
 should also work.  Kafka can be installed via `brew install kafka` or downloading the Kafka binary dist from [Apache](https://kafka.apache.org/quickstart).
 
A local install of an open source Oauth server, Keycloak, is used to generate and validate the JWT token.  Instructions for configuring Keycloak
 are found [here](misc/keycloak-setup.md).

Payload signatures are created and
 validated using the standard JDK cryptography APIs.  A standard RSA keypair is included in this repo for this purpose.
 
4 apps are included: client, processor, server, and error-monitor.  

The client app is a REST endpoint that uses Spring Security Oauth2 to validate the JWT token and set the Security Context.  The client app then
 puts the JWT token in a message header.  The payload is used to generate a signature which is also put in a message header.  The client app sends
  the message to the processor app, which validates the JWT and the signature.  If successful, the message is passed to the server app.  If the JWT
   or signature are invalid the message is sent to the error-monitor app, which logs the message.

The call to the client app REST endpoint is provided via CURL in the `send.sh` script.  It uses the JWT token set in the Keycloak instructions.

Note that InfluxDB is being used to monitor metrics.  Either install and configure InfluxDB, or ignore the errors in the logs.