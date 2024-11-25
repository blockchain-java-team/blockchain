package com.blockchain.model;

import org.junit.jupiter.api.Test;

import java.security.*;
import static org.junit.jupiter.api.Assertions.*;

class BlockTest {

    @Test
    void isVerified_verified() throws Exception {
        // Generate a key pair for testing
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        // Create a sample block
        Block block = new Block();
        block.setLedgerId(1);
        block.setTimeStamp("2024-11-20T12:00:00");
        block.setMinedBy(publicKey.getEncoded());
        block.setLuck(12345.67);
        block.setMiningPoints(100);

        // Generate a digital signature for the block
        Signature signing = Signature.getInstance("SHA256withRSA");
        signing.initSign(privateKey);
        signing.update(block.toString().getBytes());
        byte[] signature = signing.sign();
        block.setCurrHash(signature);

        // Verify the block
        Signature verification = Signature.getInstance("SHA256withRSA");
        assertTrue(block.isVerified(verification), "Block should be verified successfully.");
    }

    @Test
    void isVerified_notVerified_modifiedBlock() throws Exception {
        // Generate a key pair for testing
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        // Create a sample block
        Block block = new Block();
        block.setLedgerId(1);
        block.setTimeStamp("2024-11-20T12:00:00");
        block.setMinedBy(publicKey.getEncoded());
        block.setLuck(12345.67);
        block.setMiningPoints(100);

        // Generate a digital signature for the block
        Signature signing = Signature.getInstance("SHA256withRSA");
        signing.initSign(privateKey);
        signing.update(block.toString().getBytes());
        byte[] signature = signing.sign();
        block.setCurrHash(signature);

        // Modify the block's data
        block.setMiningPoints(200); // Tampering with data

        // Verify the block
        Signature verification = Signature.getInstance("SHA256withRSA");
        assertFalse(block.isVerified(verification), "Modified block should not be verified.");
    }
}
