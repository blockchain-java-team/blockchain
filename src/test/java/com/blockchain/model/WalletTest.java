package com.blockchain.model;

import org.junit.jupiter.api.Test;

import java.security.*;

import static org.junit.jupiter.api.Assertions.*;

class WalletTest {
    @Test
    void testWallet() {
        try {
            Wallet w = new Wallet();

            // just for visualization
            System.out.println("private key: \n " + w.getPrivateKey().toString());
            System.out.println("public key : \n" + w.getPublicKey().toString());
            System.out.println("key pair: \n" + w.getKeyPair().getPrivate());

            assertEquals(w.getPrivateKey(), w.getKeyPair().getPrivate());
            assertEquals(w.getPublicKey(), w.getKeyPair().getPublic());
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    void testWallet_constructorWithPublicPrivateKey(){
        KeyPairGenerator keyPairGen;
        try {
            keyPairGen = KeyPairGenerator.getInstance("DSA");
            keyPairGen.initialize(Wallet.KEY_SIZE);
            KeyPair pair = keyPairGen.generateKeyPair();
            PrivateKey privateKey = pair.getPrivate();
            PublicKey publicKey = pair.getPublic();

            Wallet w = new Wallet(publicKey, privateKey);
            assertEquals(w.getPrivateKey(), privateKey);
            assertEquals(w.getPublicKey(), publicKey);
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
        }


    }
}