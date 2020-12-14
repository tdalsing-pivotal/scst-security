package com.vmware.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import static java.lang.Boolean.TRUE;
import static javax.crypto.Cipher.DECRYPT_MODE;
import static javax.crypto.Cipher.ENCRYPT_MODE;
import static lombok.AccessLevel.PRIVATE;

@Service
@EnableConfigurationProperties(MessageSignerProperties.class)
@Slf4j
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class MessageSigner<T> {

    StreamBridge bridge;
    PrivateKey messageSignerPrivateKey;
    PublicKey messageSignerPublicKey;
    Counter messageSignerCounter;
    Counter messageSignerErrorCounter;
    Timer messageSignerVerifyTimer;
    Timer messageSignerSignTimer;
    ObjectMapper mapper = new ObjectMapper();
    static final Decoder decoder = Base64.getDecoder();
    static final Encoder encoder = Base64.getEncoder();

    public MessageSigner(
            StreamBridge bridge,
            PrivateKey messageSignerPrivateKey,
            PublicKey messageSignerPublicKey,
            Counter messageSignerCounter,
            Counter messageSignerErrorCounter,
            Timer messageSignerVerifyTimer,
            Timer messageSignerSignTimer) {
        this.bridge = bridge;
        this.messageSignerPrivateKey = messageSignerPrivateKey;
        this.messageSignerPublicKey = messageSignerPublicKey;
        this.messageSignerCounter = messageSignerCounter;
        this.messageSignerErrorCounter = messageSignerErrorCounter;
        this.messageSignerVerifyTimer = messageSignerVerifyTimer;
        this.messageSignerSignTimer = messageSignerSignTimer;
    }

    @Bean
    public static PublicKey messageSignerPublicKey(MessageSignerProperties properties) throws Exception {
        log.info("messageSignerPublicKey: publicKey='{}'", properties.getPublicKey());
        byte[] encoded = properties.getPublicKey().getBytes();
        byte[] decoded = decoder.decode(encoded);
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
    }

    @Bean
    public static PrivateKey messageSignerPrivateKey(MessageSignerProperties properties) throws Exception {
        log.info("messageSignerPrivateKey: privateKey='{}'", properties.getPrivateKey());
        byte[] encoded = properties.getPrivateKey().getBytes();
        byte[] decoded = decoder.decode(encoded);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
    }

    @Bean
    public static Counter messageSignerCounter(MeterRegistry registry) {
        log.info("messageSignerCounter");
        return registry.counter("message-signer-count");
    }

    @Bean
    public static Counter messageSignerErrorCounter(MeterRegistry registry) {
        log.info("messageSignerErrorCounter");
        return registry.counter("message-signer-error-count");
    }

    @Bean
    public static Timer messageSignerVerifyTimer(MeterRegistry registry) {
        log.info("messageSignerVerifyTimer");
        return registry.timer("message-signer-verify-timer");
    }

    @Bean
    public static Timer messageSignerSignTimer(MeterRegistry registry) {
        log.info("messageSignerSignTimer");
        return registry.timer("message-signer-sign-timer");
    }

    public boolean verifyMessageSignature(Message<T> message, String bindingName) {
        try {
            messageSignerCounter.increment();

            Boolean validSign = messageSignerVerifyTimer.recordCallable(() -> verify(message));
            log.info("verifyMessageSignature: validSign={}", validSign);

            if (!validSign) {
                Message<T> errorMessage = MessageBuilder.fromMessage(message).setHeader("invalidSignature", TRUE).build();
                log.error("verifyMessageSignature: invalid signature, sending to error topic: message={}", errorMessage);
                messageSignerErrorCounter.increment();
                bridge.send(bindingName, errorMessage);
            }

            return validSign;
        } catch (Exception e) {
            log.error("verifyMessageSignature: e={}", e.toString(), e);
            throw new IllegalArgumentException(e.toString(), e);
        }
    }

    private boolean verify(Message<T> message) throws Exception {
        String signature = message.getHeaders().get("signature", String.class);
        log.info("verifyMessageSignature: signature={}", signature);

        byte[] encryptedHash = decoder.decode(signature);

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(DECRYPT_MODE, messageSignerPublicKey);
        byte[] decryptedHash = cipher.doFinal(encryptedHash);

        T payload = message.getPayload();
        byte[] bytes = mapper.writeValueAsBytes(payload);

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(bytes);
        log.info("verifyMessageSignature: decrypted hash = {}", Base64.getEncoder().encodeToString(decryptedHash));
        log.info("verifyMessageSignature:  expected hash = {}", Base64.getEncoder().encodeToString(hash));
        return Arrays.equals(hash, decryptedHash);
    }

    public void signMessage(MessageBuilder<T> messageBuilder, T payload) {
        try {
            String encryptedSign = messageSignerSignTimer.recordCallable(() -> sign(payload));
            log.info("signMessage: encryptedSign={}", encryptedSign);

            messageBuilder.setHeader("signature", encryptedSign);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.toString(), e);
        }
    }

    private String sign(T payload) throws Exception {
        log.info("sign: payload={}", payload);
        byte[] bytes = mapper.writeValueAsBytes(payload);

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(bytes);

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(ENCRYPT_MODE, messageSignerPrivateKey);
        byte[] encryptedHash = cipher.doFinal(hash);

        return encoder.encodeToString(encryptedHash);
    }
}
