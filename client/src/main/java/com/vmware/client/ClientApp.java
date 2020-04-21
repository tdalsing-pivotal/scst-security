package com.vmware.client;

import com.vmware.common.JWTValidator;
import com.vmware.common.MessageSigner;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.function.Supplier;

import static lombok.AccessLevel.PRIVATE;
import static org.springframework.http.HttpStatus.ACCEPTED;

@SpringBootApplication
@RestController
@Import({JWTValidator.class, MessageSigner.class})
@Slf4j
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ClientApp {

    EmitterProcessor<Message<Map<String, Object>>> emitterProcessor = EmitterProcessor.create();
    JWTValidator<Map<String, Object>> jwtValidator;
    MessageSigner<Map<String, Object>> messageSigner;

    public ClientApp(JWTValidator<Map<String, Object>> jwtValidator,
                     MessageSigner<Map<String, Object>> messageSigner) {
        this.jwtValidator = jwtValidator;
        this.messageSigner = messageSigner;
    }

    public static void main(String[] args) {
        SpringApplication.run(ClientApp.class, args);
    }

    @PostMapping("/data")
    @ResponseStatus(ACCEPTED)
    public void post(@RequestBody Map<String, Object> data) throws Exception {
        log.info("post: data={}", data);

        boolean invalidJwt = (boolean) data.getOrDefault("invalidJwt", Boolean.FALSE);
        boolean invalidSign = (boolean) data.getOrDefault("invalidSign", Boolean.FALSE);

        MessageBuilder<Map<String, Object>> messageBuilder = MessageBuilder.withPayload(data);
        jwtValidator.populateJwtHeader(messageBuilder);
        messageSigner.signMessage(messageBuilder, data);

        if (invalidSign) {
            log.warn("post: tampering with data after signing");
            data.put("extraData", 123);
        }

        if (invalidJwt) {
            log.warn("post: corrupting JWT");
            String jwts = messageBuilder.getHeader("jwts", String.class);
            messageBuilder.setHeader("jwts", jwts.substring(0, jwts.length() - 2));
        }

        Message<Map<String, Object>> message = messageBuilder.build();
        log.info("post: sending message={}", message);

        emitterProcessor.onNext(message);
    }

    @Bean
    public Supplier<Flux<Message<Map<String, Object>>>> supplier() {
        log.info("supplier");
        return () -> emitterProcessor;
    }
}
