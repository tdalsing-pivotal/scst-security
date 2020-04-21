package com.vmware.error.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

import static lombok.AccessLevel.PRIVATE;

@SpringBootApplication
@Slf4j
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ErrorMonitorApp {

    Counter jwtErrorCounter;
    Counter signatureErrorCounter;

    public ErrorMonitorApp(Counter jwtErrorCounter, Counter signatureErrorCounter) {
        this.jwtErrorCounter = jwtErrorCounter;
        this.signatureErrorCounter = signatureErrorCounter;
    }

    public static void main(String[] args) {
        SpringApplication.run(ErrorMonitorApp.class, args);
    }

    @Bean
    public static Counter jwtErrorCounter(MeterRegistry registry) {
        log.info("jwtErrorCounter");
        return registry.counter("scst-security-error-monitor-jwt-count");
    }

    @Bean
    public static Counter signatureErrorCounter(MeterRegistry registry) {
        log.info("signatureErrorCounter");
        return registry.counter("scst-security-error-monitor-signature-count");
    }

    @Bean
    public Consumer<Message<?>> jwtErrorConsumer() {
        log.info("jwtErrorConsumer");
        return message -> {
            jwtErrorCounter.increment();
            log.error("jwtErrorConsumer: message={}", message);
        };
    }

    @Bean
    public Consumer<Message<?>> signatureErrorConsumer() {
        log.info("signatureErrorConsumer");
        return message -> {
            signatureErrorCounter.increment();
            log.error("signatureErrorConsumer: message={}", message);
        };
    }
}
