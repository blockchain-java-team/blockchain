package com.blockchain.model;

import org.junit.jupiter.api.Test;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    @Test
    void isVerified_verified() {
        try {
            var walletFrom = new Wallet();
            var walletTo = new Wallet();
            int value = 100;
            int ledgerId = 1;
            Signature sig = Signature.getInstance("SHA256withRSA");

            var transaction = new Transaction(
                    walletFrom,
                    walletTo.getPublicKey().getEncoded(),
                    value,
                    ledgerId,
                    sig
            );

            try {
                assertEquals(true, transaction.isVerified(sig));
            } catch (InvalidKeySpecException e) {
                System.out.println(e.getMessage());
            }


        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test

    void isVerified_notVerifiedForToAddressChanged() {
        try {
            var walletFrom = new Wallet();
            var walletTo = new Wallet();
            int value = 100;
            int ledgerId = 1;
            Signature sig = Signature.getInstance("SHA256withRSA");

            var transaction = new Transaction(
                    walletFrom,
                    walletTo.getPublicKey().getEncoded(),
                    value,
                    ledgerId,
                    sig
            );

            var walletToHacker = new Wallet();
            transaction.setTo(walletToHacker.getPublicKey().getEncoded());


            try {
                assertEquals(false, transaction.isVerified(sig));
            } catch (InvalidKeySpecException e) {
                System.out.println(e.getMessage());
            }


        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    void isVerified_notVerifiedForValueChanged() {
        try {
            var walletFrom = new Wallet();
            var walletTo = new Wallet();
            int value = 100;
            int ledgerId = 1;
            Signature sig = Signature.getInstance("SHA256withRSA");

            var transaction = new Transaction(
                    walletFrom,
                    walletTo.getPublicKey().getEncoded(),
                    value,
                    ledgerId,
                    sig
            );

            transaction.setValue(200);


            try {
                assertEquals(false, transaction.isVerified(sig));
            } catch (InvalidKeySpecException e) {
                System.out.println(e.getMessage());
            }


        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            System.out.println(e.getMessage());
        }
    }
}