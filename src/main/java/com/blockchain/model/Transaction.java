package com.blockchain.model;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;

@Getter
public class Transaction {

    /**
     * public keys/addresses of the account that sends
     */
    @Setter
    private byte[] from;
    private String fromFX;

    /**
     * public keys/addresses of the account that receives
     */
    @Setter
    private byte[] to;
    private String toFX;

    /**
     * coins/amount sent
     */
    @Setter
    private Integer value;

    /**
     * time at which the transaction has occurred
     */
    private String timestamp;

    /**
     * contain the encrypted information of all the fields, and it will be used to verify the
     * validity of the transaction (it will be used the same way the field currHash
     * was used in the {@link Block#getCurrHash()} class)
     */
    private byte[] signature;
    private String signatureFX;

    /**
     * {@link Block} number where the transaction is stored, help retrieve the correct block from database
     */
    @Setter
    private Integer ledgerId;

    /**
     * used when we retrieve a transaction from the database,
     * Constructor for loading with existing signature
     */
    public Transaction(
            byte[] from,
            byte[] to,
            Integer value,
            byte[] signature,
            Integer ledgerId,
            String timeStamp
    ) {
        Base64.Encoder encoder = Base64.getEncoder();
        this.from = from;
        this.fromFX = encoder.encodeToString(from);
        this.to = to;
        this.toFX = encoder.encodeToString(to);
        this.value = value;
        this.signature = signature;
        this.signatureFX = encoder.encodeToString(signature);
        this.ledgerId = ledgerId;
        this.timestamp = timeStamp;
    }


    /**
     * Constructor for creating a new transaction and signing it.
     * <br>
     * used when we want to create a new transaction within our application
     *
     * @param fromWallet the fromWallet parameter contains the public and
     *                   private keys of the sender/maker of the transaction.
     */
    public Transaction(
            Wallet fromWallet,
            byte[] toAddress,
            Integer value,
            Integer ledgerId,
            Signature signing
    ) throws InvalidKeyException, SignatureException {
        Base64.Encoder encoder = Base64.getEncoder();
        this.from = fromWallet.getPublicKey().getEncoded();
        this.fromFX = encoder.encodeToString(fromWallet.getPublicKey().getEncoded());
        this.to = toAddress;
        this.toFX = encoder.encodeToString(toAddress);
        this.value = value;
        this.ledgerId = ledgerId;
        this.timestamp = LocalDateTime.now().toString();
        signing.initSign(fromWallet.getPrivateKey());
        String sr = this.toString();
        signing.update(sr.getBytes());
        this.signature = signing.sign();
        this.signatureFX = encoder.encodeToString(this.signature);
    }

    /**
     * Verification of Transaction,
     * should be used by the other peers to verify that each transaction is valid.
     * <br>
     * We verify that the {@link Transaction#from sender} is the one that created the transaction
     * and not someone else.
     * @return true if Transaction is valid, false else
     */
    public Boolean isVerified(Signature signing)
            throws InvalidKeyException, SignatureException, NoSuchAlgorithmException, InvalidKeySpecException {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(getFrom());
        KeyFactory keyFactory = KeyFactory.getInstance("DSA");
        // public key of sender
        PublicKey publicKey = keyFactory.generatePublic(keySpec);

        signing.initVerify(publicKey);
        signing.update(this.toString().getBytes());
        return signing.verify(this.getSignature());
    }

    /**
     * Note how all the essential fields that make certain
     * the transaction is unique are included in the toString() method.
     * <br>
     * Used to sign in {@link #Transaction(Wallet, byte[], Integer, Integer, Signature)}
     * and verify the transaction in {@link #isVerified(Signature)}
     */
    @Override
    public String toString() {
        return "Transaction{" +
                "from=" + Arrays.toString(from) +
                ", to=" + Arrays.toString(to) +
                ", value=" + value +
                ", timeStamp= " + timestamp +
                ", ledgerId=" + ledgerId +
                '}';
    }


    /**
     * two Transactions are equal if and only if they have the same signature
     * <br>
     * signature is used because we're certain it's unique.
     * {@link #timestamp} proves the uniqueness of the Transaction
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction that)) return false;
        return Arrays.equals(getSignature(), that.getSignature());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getSignature());
    }

    /**
     * Converts the current Transaction object to a JSON string.
     * @return JSON representation of the Transaction object.
     */
    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

}