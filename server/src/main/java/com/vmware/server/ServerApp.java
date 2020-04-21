package com.vmware.server;

import com.vmware.common.JWTValidator;
import com.vmware.common.MessageSigner;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;

import java.util.Map;
import java.util.function.Consumer;

import static lombok.AccessLevel.PRIVATE;

@SpringBootApplication
@Import({JWTValidator.class, MessageSigner.class})
@Slf4j
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ServerApp {

    JWTValidator<Map<String, Object>> jwtValidator;
    MessageSigner<Map<String, Object>> messageSigner;

    public ServerApp(JWTValidator<Map<String, Object>> jwtValidator, MessageSigner<Map<String, Object>> messageSigner) {
        this.jwtValidator = jwtValidator;
        this.messageSigner = messageSigner;
    }

    public static void main(String[] args) {
        SpringApplication.run(ServerApp.class, args);
    }

    @Bean
    public Consumer<Message<Map<String, Object>>> consumer() {
        log.info("consumer");
        return message -> {
            log.info("consumer: message={}", message);
            boolean validSign = messageSigner.verifyMessageSignature(message);
            boolean validJwt = jwtValidator.validate(message);

            if (validSign && validJwt) {
                log.info("consumer: JWT and signature is valid: message={}", message);
            } else {
                if (!validSign) {
                    log.error("consumer: Signature is invalid: message={}", message);
                }
                if (!validJwt) {
                    log.error("consumer: JWT is invalid: message={}", message);
                }
            }
        };
    }
}
