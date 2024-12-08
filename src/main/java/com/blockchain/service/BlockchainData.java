package com.blockchain.service;

import com.blockchain.helpers.Utils;
import com.blockchain.model.Block;
import com.blockchain.model.Transaction;
import com.blockchain.model.Wallet;
import com.blockchain.state.BlockchainState;
import lombok.Getter;
import lombok.Setter;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Blockchain's Reward Transaction create new coins (from nothing) and aware them to the miner that mines that block.
 * We are america btw.
 */
public class BlockchainData {
    @Getter
    private static final int TIMEOUT_INTERVAL = 15; // todo: return it to 65
    @Getter
    private static final int MINING_INTERVAL = 10; // todo: should be 60
    @Getter
    private static BlockchainData instance;

    static {
        try {
            instance = new BlockchainData();
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
        }
    }

    Comparator<Transaction> transactionComparator = Comparator.comparing(Transaction::getTimestamp);
    @Getter
    @Setter
    private int miningPoints;
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
    @Getter
    private LinkedList<Block> currentBlockChain = new LinkedList<>();

    /**
     * latest block that we are trying to add to the blockchain.
     */
    private Block latestBlock;

    public BlockchainData() throws NoSuchAlgorithmException {
        this.newBlockTransactions = new ArrayList<>();
    }

    public String getWalletBalance() {
        return getBalance(currentBlockChain, newBlockTransactions,
                WalletData.getInstance().getWallet().getPublicKey()).toString();
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

    public void mineBlock() throws Exception {
        finalizeBlock(WalletData.getInstance().getWallet());
        addBlock(latestBlock);
    }

    /**
     * preparing/finalizing our latestBlock.
     * <br>
     * Reinitialize mining points,
     * we add the reward
     * transaction of the block we just finalized to the database since until now
     * we have kept it only in the newBlockTransactions list, which we copied in
     * our lastestBlock.
     * <br>
     * We reward the miner of block A in block B (A -> B)
     *
     * @param minersWallet
     * @throws GeneralSecurityException
     * @throws SQLException
     */
    private void finalizeBlock(Wallet minersWallet) throws Exception {
        latestBlock = new Block(BlockchainData.getInstance().currentBlockChain);
        latestBlock.setTransactionLedger(new ArrayList<>(newBlockTransactions));
        latestBlock.setTimeStamp(LocalDateTime.now().toString());
        latestBlock.setMinedBy(minersWallet.getPublicKey().getEncoded());
        latestBlock.setMiningPoints(miningPoints);
        signing.initSign(minersWallet.getPrivateKey());
        signing.update(latestBlock.toString().getBytes());
        latestBlock.setCurrHash(signing.sign());

        // we include the latestBlock into our CurrentBlockChain since we have finalized it completely.
        currentBlockChain.add(latestBlock);

        miningPoints = 0;

        //Reward transaction
        latestBlock.getTransactionLedger().sort(transactionComparator);

        addTransaction(latestBlock.getTransactionLedger().getFirst(), true);

        Transaction transaction = new Transaction(new Wallet(), minersWallet.getPublicKey().getEncoded(),
                100, latestBlock.getLedgerId() + 1, signing);
        newBlockTransactions.clear();
        newBlockTransactions.add(transaction);
    }

    /**
     * adds the block to the Blockchain state
     *
     * @param block
     */
    private void addBlock(Block block) {
        BlockchainState.blocks.add(block);
    }

    /**
     * used whenever we want to load the whole blockchain from
     * our database and set up the state of the app accordingly
     */
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
     *
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
