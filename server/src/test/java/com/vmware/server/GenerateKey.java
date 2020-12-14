package com.vmware.server;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class GenerateKey {

    @Test
    public void generate() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.genKeyPair();

        byte[] publicKey = kp.getPublic().getEncoded();
        byte[] privateKey = kp.getPrivate().getEncoded();

        String publicString = Base64.getEncoder().encodeToString(publicKey);
        String privateString = Base64.getEncoder().encodeToString(privateKey);

        System.out.println("public key: "+publicString);
        System.out.println("private key: "+privateString);

        Files.write(Paths.get("src/main/resources/publicKey"), publicString.getBytes());
        Files.write(Paths.get("src/main/resources/privateKey"), privateString.getBytes());

        byte[] pubKey = Base64.getDecoder().decode(publicString);
        byte[] priKey = Base64.getDecoder().decode(privateString);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        PublicKey decodedPubKey = keyFactory.generatePublic(new X509EncodedKeySpec(pubKey));
        PrivateKey decodedPriKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(priKey));
    }
}
