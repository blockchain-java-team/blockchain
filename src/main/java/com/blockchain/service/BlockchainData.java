package com.blockchain.service;

import com.blockchain.helpers.Utils;
import com.blockchain.model.Block;
import com.blockchain.model.Transaction;
import com.blockchain.model.Wallet;
import com.blockchain.state.BlockchainState;
import lombok.Getter;

import java.security.*;
import java.util.*;

/**
 * Blockchain's Reward Transaction create new coins (from nothing) and aware them to the miner that mines that block.
 * We are america btw.
 */
public class BlockchainData {
    @Getter
    private static BlockchainData instance;

    static {
        try {
            instance = new BlockchainData();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    Comparator<Transaction> transactionComparator = Comparator.comparing(Transaction::getTimestamp);
    // helper
    private Signature signing = Signature.getInstance("SHA256withDSA");
    /**
     * List of Transactions before block finalization.
     * Which means before mining the Block.
     * represent the current ledger of our blockchain
     */
    private List<Transaction> newBlockTransactions;

    /**
     * current blockchain. The blockchain in this attribute
     */
    private LinkedList<Block> currentBlockChain = new LinkedList<>();

    /**
     * latest block that we are trying to add to the blockchain.
     */
    private Block latestBlock;

    public BlockchainData() throws NoSuchAlgorithmException {
        this.newBlockTransactions = new ArrayList<>();
    }

    /**
     * Calculate the Wallet Balance using UTXO
     * <br>
     * To prevent double spending we also need to subtract any funds we
     * are already trying to send that exist in the current transaction ledger.
     *
     * @param blockChain    current blockchain
     * @param currentLedger List of the current Block's Transactions, Not mined
     * @param walletAddress {@link PublicKey} of the current wallet
     * @return your balance
     */
    private Integer getBalance(
            LinkedList<Block> blockChain,
            List<Transaction> currentLedger, PublicKey walletAddress) {
        Integer balance = 0;
        for (Block block : blockChain) {
            for (Transaction transaction : block.getTransactionLedger()) {
                if (Arrays.equals(transaction.getFrom(), walletAddress.getEncoded())) {
                    balance -= transaction.getValue();
                }
                if (Arrays.equals(transaction.getTo(), walletAddress.getEncoded())) {
                    balance += transaction.getValue();
                }
            }
        }
        for (Transaction transaction : currentLedger) {
            if (Arrays.equals(transaction.getFrom(), walletAddress.getEncoded())) {
                balance -= transaction.getValue();
            }
        }
        return balance;
    }

    /**
     * add a new transaction to our ledger.
     *
     * @param transaction the transaction you want to add
     * @param blockReward is a reward transaction for the miner (true), regular transaction(false)
     * @throws Exception
     */
    public void addTransaction(Transaction transaction, boolean blockReward) throws Exception {
        Integer current_balance = getBalance(currentBlockChain, newBlockTransactions, Utils.byteArrayToPublicKey(transaction.getFrom()));
        boolean isRegularTransaction = !blockReward;

        if (isRegularTransaction && current_balance < transaction.getValue()) {
            // it's a regular transaction and
            throw new GeneralSecurityException("Not enough funds by sender to record transaction");
        }

        BlockchainState.transactions.add(transaction);
    }

    /**
     * adds a given transaction into our current transaction ledger and sorts it.
     *
     * @param transaction
     */
    public void addTransactionState(Transaction transaction) {
        newBlockTransactions.add(transaction);
        newBlockTransactions.sort(transactionComparator);
    }

    public void loadBlockChain() throws GeneralSecurityException {

        this.currentBlockChain.addAll(BlockchainState.blocks);

        // this block is mined
        latestBlock = currentBlockChain.getLast();

        // reward transaction
        Transaction transaction = new Transaction(
                new Wallet(),
                WalletData.getInstance().getWallet().getPublicKey().getEncoded(),
                100, latestBlock.getLedgerId() + 1, signing
        );

        newBlockTransactions.clear();
        newBlockTransactions.add(transaction);
        verifyBlockChain(currentBlockChain);
    }

    /**
     * checks the validity of each transaction and each block.
     * @param currentBlockChain
     * @throws GeneralSecurityException
     */
    private void verifyBlockChain(LinkedList<Block> currentBlockChain) throws GeneralSecurityException {

        for (Block block : currentBlockChain) {
            if (Boolean.FALSE.equals(block.isVerified(signing))) {
                throw new GeneralSecurityException("Block validation failed");
            }
            ArrayList<Transaction> transactions = block.getTransactionLedger();
            for (Transaction transaction : transactions) {
                if (Boolean.FALSE.equals(transaction.isVerified(signing))) {
                    throw new GeneralSecurityException("Transaction validation failed");
                }
            }
        }
    }
}
