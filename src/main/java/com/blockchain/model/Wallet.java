package com.blockchain.model;

import com.google.gson.Gson;
import lombok.Getter;

import java.security.*;

@Getter
public class Wallet {

    private final KeyPair keyPair;

    public static final int KEY_SIZE = 2048;

    //Constructors for generating new KeyPair
    public Wallet() throws NoSuchAlgorithmException {
        this(KeyPairGenerator.getInstance("DSA"));
    }
    public Wallet(KeyPairGenerator keyPairGen) {
       keyPairGen.initialize(KEY_SIZE);
       this.keyPair = keyPairGen.generateKeyPair();
    }

    //Constructor for importing Keys only
    public Wallet(PublicKey publicKey, PrivateKey privateKey) {
        this.keyPair = new KeyPair(publicKey,privateKey);
    }

    public PublicKey getPublicKey() { return keyPair.getPublic(); }
    public PrivateKey getPrivateKey() { return keyPair.getPrivate(); }

    /**
     * Converts the current Wallet object to a JSON string.
     * @return JSON representation of the Wallet object.
     */
    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}