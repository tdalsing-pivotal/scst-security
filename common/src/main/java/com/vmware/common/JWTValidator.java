package com.vmware.common;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

import java.util.function.Supplier;

import static java.lang.Boolean.TRUE;
import static lombok.AccessLevel.PRIVATE;

@Service
@Slf4j
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class JWTValidator<T> {

    JwtDecoder jwtDecoder;
    Counter jwtValidatorCounter;
    Counter jwtValidatorErrorCounter;
    EmitterProcessor<Message<T>> emitterProcessor = EmitterProcessor.create();

    public JWTValidator(JwtDecoder jwtDecoder, Counter jwtValidatorCounter, Counter jwtValidatorErrorCounter) {
        this.jwtDecoder = jwtDecoder;
        this.jwtValidatorCounter = jwtValidatorCounter;
        this.jwtValidatorErrorCounter = jwtValidatorErrorCounter;
    }

    @Bean
    public static Counter jwtValidatorCounter(MeterRegistry registry) {
        log.info("jwtValidatorCounter");
        return registry.counter("jwt-validator-count");
    }

    @Bean
    public static Counter jwtValidatorErrorCounter(MeterRegistry registry) {
        log.info("jwtValidatorErrorCounter");
        return registry.counter("jwt-validator-error-count");
    }

    @Bean
    public Supplier<Flux<Message<T>>> jwtErrorSupplier() {
        log.info("jwtErrorSupplier");
        return () -> emitterProcessor;
    }

    public void populateJwtHeader(MessageBuilder<T> messageBuilder) {
        log.info("populateJwtHeader: messageBuilder={}", messageBuilder);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken)authentication;
        Jwt jwt = jwtAuthenticationToken.getToken();
        String jwts = jwt.getTokenValue();
        messageBuilder.setHeader("jwts", jwts);
    }

    public boolean validate(Message<T> message) {
        log.info("validate: message={}", message);
        jwtValidatorCounter.increment();
        String jwts = message.getHeaders().get("jwts", String.class);

        try {
            jwtDecoder.decode(jwts);
            log.info("validate: JWT is valid: message={}", message);
            return true;
        } catch (JwtException e) {
            log.error("validate: JWT is invalid: message={}, e={}", message, e.toString(), e);
            jwtValidatorErrorCounter.increment();
            emitterProcessor.onNext(MessageBuilder.fromMessage(message).setHeader("jwtInvalid", TRUE).setHeader("jwtException", e.toString()).build());
            return false;
        }
    }
}
