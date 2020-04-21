package com.vmware.server;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class GenerateJwt {

//    @Test
    public void generate() throws Exception {
        String publicKeyString = Files.readString(Paths.get("src/main/resources/publicKey"));
        String privateKeyString = Files.readString(Paths.get("src/main/resources/privateKey"));

        byte[] pubKey = Base64.getDecoder().decode(publicKeyString);
        byte[] priKey = Base64.getDecoder().decode(privateKeyString);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        PublicKey decodedPubKey = keyFactory.generatePublic(new X509EncodedKeySpec(pubKey));
        PrivateKey decodedPriKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(priKey));

        RSASSASigner signer = new RSASSASigner(decodedPriKey);

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).build();
        JWTClaimsSet claims = new JWTClaimsSet.Builder().subject("SCSt").build();
        SignedJWT signedJWT = new SignedJWT(header, claims);
        signedJWT.sign(signer);
        String jwts = signedJWT.serialize();

        System.out.println(jwts);

        Files.writeString(Paths.get("src/main/resources/jwt"), jwts);
    }
}
