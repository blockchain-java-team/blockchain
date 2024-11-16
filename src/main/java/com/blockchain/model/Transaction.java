package com.blockchain.model;



import sun.security.provider.DSAPublicKeyImpl;

import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.Signature;
import java.security.SignatureException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

public class Transaction implements Serializable {

    private byte[] from;
    private String fromFX;
    private byte[] to;
    private String toFX;
    private Integer value;
    private String timestamp;
    private byte[] signature;
    private  String signatureFX;
    private Integer ledgerId;


    //Constructor for loading with existing signature
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
    //Constructor for creating a new transaction and signing it.
    public Transaction (
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

    public Boolean isVerified(
            Signature signing
    ) throws InvalidKeyException, SignatureException {
        signing.initVerify(new DSAPublicKeyImpl(this.getFrom()));
        signing.update(this.toString().getBytes());
        return signing.verify(this.signature);
    }

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

}