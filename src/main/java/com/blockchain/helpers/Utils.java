package com.blockchain.helpers;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Utils {

    /**
     * Converts a byte array to a PublicKey.
     * @param publicKeyBytes The byte array representing the public key.
     * @return The corresponding PublicKey object.
     * @throws Exception If there is an error generating the PublicKey.
     */
    public static PublicKey byteArrayToPublicKey(byte[] publicKeyBytes) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("DSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        return keyFactory.generatePublic(keySpec);
    }


    /**
     * Converts a byte array to a PrivateKey.
     * @param privateKeyBytes The byte array representing the private key.
     * @return The corresponding PrivateKey object.
     * @throws Exception If there is an error generating the PrivateKey.
     */
    public static PrivateKey byteArrayToPrivateKey(byte[] privateKeyBytes) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("DSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        return keyFactory.generatePrivate(keySpec);
    }
}
