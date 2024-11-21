package com.blockchain.model;

import com.blockchain.EnsaChain;
import lombok.Getter;
import lombok.Setter;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;


@Getter
@Setter

public class Block {
    /**
     * The field prevHash will contain the signature or, in other words,
     * the encrypted data from the previous block.
     **/
    private byte[] prevHash;
    /**
     * The currHash will contain the signature or, in other words,
     * the encrypted data from this block,which will be encrypted
     * with the private key of the miner that will get to mine this block.
     * <br>
     * The current hash/signature of the block is just the encrypted
     * version of the data contained in the block.
     **/
    private byte[] currHash;
    /**
     * The timeStamp obviously will contain a timestamp of when
     * this block was mined/finalized.
     */
    private String timeStamp;
    /**
     * The field minedBy will contain the public key,
     * which also doubles as the public address of the miner
     * that managed to mine this block.
     * <br>
     * In the process of blockchain verification, this public address/public
     * key will be used to verify that the currHash/signature of this block is
     * the same as the hash of the data presented by this block and secondary
     * that this block was indeed mined by this particular miner.
     * <br>
     * The miner’s public key is used for other peers to verify the
     * block by comparing the signature’s hash against the hash of
     * the block’s data.
     * <br>
     * The miner’s private key is used to encrypt the block’s data,
     * which creates the signature.
     */
    private byte[] minedBy;
    /**
     * Block number, help retrieve the correct block from database
     */
    private Integer ledgerId = 1;
    /**
     * used to form the network consensus in regard to choosing this block’s miner.
     */
    private Integer miningPoints = 0;
    /**
     * used to form the network consensus in regard to choosing this block’s miner.
     */
    private Double luck = 0.0;
    /**
     * {@link java.util.ArrayList} of all transactions contained in this block
     */
    private ArrayList<Transaction> transactionLedger = new ArrayList<>();

    /**
     * This constructor is used when we retrieve it from the db
     *
     * @param prevHash
     * @param currHash
     * @param timeStamp
     * @param minedBy
     * @param ledgerId
     * @param miningPoints
     * @param luck
     * @param transactionLedger
     */
    public Block(byte[] prevHash, byte[] currHash, String timeStamp, byte[] minedBy, Integer ledgerId,
                 Integer miningPoints, Double luck, ArrayList<Transaction> transactionLedger) {
        this.prevHash = prevHash;
        this.currHash = currHash;
        this.timeStamp = timeStamp;
        this.minedBy = minedBy;
        this.ledgerId = ledgerId;
        this.miningPoints = miningPoints;
        this.luck = luck;
        this.transactionLedger = transactionLedger;
    }

    /**
     * used while the application is running and is used to create a completely new block (in other words, the head of the blockchain) for us to work on.
     *
     * @param currentBlockChain
     */
    public Block(LinkedList<Block> currentBlockChain) {
        Block lastBlock = currentBlockChain.getLast();
        prevHash = lastBlock.getCurrHash();
        ledgerId = lastBlock.getLedgerId() + 1;
        luck = Math.random() * 1000000;
    }

    /**
     * used once to create our first block.
     * <br>
     * It sets the first block’s prevHash to an array of zeros.
     */
    public Block() {
        prevHash = new byte[]{0}; // initiate the array with zeros
    }


    /**
     * verifies a digital signature.
     * verify the data in this class against the signature stored in the currHash.
     *
     * @param signing
     * @return whether the data contained in this class is verified against its currHash.
     * @throws InvalidKeyException
     * @throws SignatureException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public Boolean isVerified(Signature signing)
            throws InvalidKeyException, SignatureException, NoSuchAlgorithmException, InvalidKeySpecException {
        // Get publicKey from this.minedBy (which is the public key in byte array format)
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(this.minedBy);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");  // Use RSA instead of DSA
        PublicKey publicKey = keyFactory.generatePublic(keySpec);

        // Initialize the Signature with the PublicKey for verification
        signing.initVerify(publicKey);

        // Generate a hash of the block's data (excluding the currHash, which is the signature)
        byte[] blockDataHash = this.toString().getBytes();  // Use relevant block data to hash

        // Update the Signature with the data to verify (usually the block data or hash)
        signing.update(blockDataHash);

        // Verify the signature with the current hash
        return signing.verify(this.currHash);
    }

    /**
     * two Blocks are equals if and only if they have the same prevHash
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Block block)) return false;
        return Arrays.equals(getPrevHash(), block.getPrevHash());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getPrevHash());
    }

    /**
     * Note how all the essential fields that make certain the
     * block is unique are included in the toString() method.
     **/
    @Override
    public String toString() {
        return "Block{" +
                "luck=" + luck +
                ", miningPoints=" + miningPoints +
                ", ledgerId=" + ledgerId +
                ", minedBy=" + Arrays.toString(minedBy) +
                ", timeStamp='" + timeStamp + '\'' +
                ", prevHash=" + Arrays.toString(prevHash) +
                '}';
    }
}