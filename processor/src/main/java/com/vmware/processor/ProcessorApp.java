package com.vmware.processor;

import com.vmware.common.JWTValidator;
import com.vmware.common.MessageSigner;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Map;
import java.util.function.Function;

import static lombok.AccessLevel.PRIVATE;

@SpringBootApplication
@Import({JWTValidator.class, MessageSigner.class})
@Slf4j
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ProcessorApp {

    JWTValidator<Map<String, Object>> validator;
    MessageSigner<Map<String, Object>> messageSigner;

    public ProcessorApp(JWTValidator<Map<String, Object>> validator, MessageSigner<Map<String, Object>> messageSigner) {
        this.validator = validator;
        this.messageSigner = messageSigner;
    }

    public static void main(String[] args) {
        SpringApplication.run(ProcessorApp.class, args);
    }

    @Bean
    public Function<Message<Map<String, Object>>, Message<Map<String, Object>>> processor() {
        log.info("processor");
        return input -> {
            try {
                log.info("processor: input={}", input);

                boolean validSign = messageSigner.verifyMessageSignature(input, "signatureErrorSupplier-out-0");
                boolean validJwt = validator.validate(input, "jwtErrorSupplier-out-0");

                if (validSign && validJwt) {
                    log.info("processor: JWT and signature is valid: input={}", input);
                    return MessageBuilder.fromMessage(input).build();
                } else {
                    if (!validJwt) {
                        log.error("processor: JWT is invalid: input={}", input);
                    }

                    if (!validSign) {
                        log.error("processor: Signature is invalid: input={}", input);
                    }

                    return null;
                }
            } catch (Exception e) {
                log.error("processor: e={}", e.toString(), e);
                throw new IllegalArgumentException(e.toString(), e);
            }
        };
    }
}
