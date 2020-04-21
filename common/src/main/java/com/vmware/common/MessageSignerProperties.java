package com.vmware.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static lombok.AccessLevel.PRIVATE;

@ConfigurationProperties("message.signer")
@Data
@NoArgsConstructor
@FieldDefaults(level = PRIVATE)
public class MessageSignerProperties {

    String privateKey;
    String publicKey;
}
