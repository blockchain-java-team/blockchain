package com.blockchain.service;

import com.blockchain.dao.impl.BlockDAOImpl;
import com.blockchain.dao.impl.TransactionDAOImpl;
import com.blockchain.helpers.Utils;
import com.blockchain.model.Block;
import com.blockchain.model.Transaction;
import com.blockchain.model.Wallet;
import com.blockchain.state.BlockchainState;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.Setter;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Blockchain's Reward Transaction create new coins (from nothing) and aware
 * them to the miner that mines that block. We are america btw.
 */
public class BlockchainData {
    @Getter
    private static final int TIMEOUT_INTERVAL = 65;
    @Getter
    private static final int MINING_INTERVAL = 60;
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
     * List of Transactions before block finalization. Which means before mining the
     * Block. represent the current ledger of our blockchain
     */
    private List<Transaction> newBlockTransactions;
    private ObservableList<Transaction> newBlockTransactionsFX;

    /**
     * current blockchain.
     */
    @Getter
    @Setter
    private LinkedList<Block> currentBlockChain = new LinkedList<>();

    /**
     * latest block that we are trying to add to the blockchain.
     */
    private Block latestBlock;

    public BlockchainData() throws NoSuchAlgorithmException {
        this.newBlockTransactions = new ArrayList<>();
        this.newBlockTransactionsFX = FXCollections.observableArrayList();
    }

    public String getWalletBalance() {
        return getBalance(currentBlockChain, newBlockTransactions, WalletData.getInstance().getWallet().getPublicKey())
                .toString();
    }

    /**
     * Calculate the Wallet Balance using UTXO <br>
     * To prevent double spending we also need to subtract any funds we are already
     * trying to send that exist in the current transaction ledger.
     *
     * @param blockChain    current blockchain
     * @param currentLedger List of the current Block's Transactions, Not mined
     * @param walletAddress {@link PublicKey} of the current wallet
     * @return your balance
     */
    private Integer getBalance(LinkedList<Block> blockChain, List<Transaction> currentLedger, PublicKey walletAddress) {
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
     * @param blockReward is a reward transaction for the miner (true), regular
     *                    transaction(false)
     * @throws Exception
     */
    public void addTransaction(Transaction transaction, boolean blockReward) throws Exception {
        Integer current_balance = getBalance(currentBlockChain, newBlockTransactions,
                Utils.byteArrayToPublicKey(transaction.getFrom()));
        boolean isRegularTransaction = !blockReward;

        if (isRegularTransaction && current_balance < transaction.getValue()) {
            // it's a regular transaction and
            throw new GeneralSecurityException("Not enough funds by sender to record transaction");
        }

        BlockchainState.addTransaction(transaction);
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
     * preparing/finalizing our latestBlock. <br>
     * Reinitialize mining points, we add the reward transaction of the block we
     * just finalized to the database since until now we have kept it only in the
     * newBlockTransactions list, which we copied in our lastestBlock. <br>
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

        // we include the latestBlock into our CurrentBlockChain since we have finalized
        // it completely.
        currentBlockChain.add(latestBlock);

        miningPoints = 0;

        // Reward transaction
        latestBlock.getTransactionLedger().sort(transactionComparator);

        addTransaction(latestBlock.getTransactionLedger().getFirst(), true);

        Transaction transaction = new Transaction(new Wallet(), minersWallet.getPublicKey().getEncoded(), 100,
                latestBlock.getLedgerId() + 1, signing);
        newBlockTransactions.clear();
        newBlockTransactions.add(transaction);
    }

    /**
     * adds the block to the Blockchain state
     *
     * @param block
     */
    private void addBlock(Block block) throws Exception {
        BlockchainState.addBlock(block);
    }

    /**
     * used whenever we want to load the whole blockchain from our database and set
     * up the state of the app accordingly
     */
    public void loadBlockChain() throws Exception {

        this.currentBlockChain.addAll(BlockchainState.getBlocks());

        // this block is mined
        latestBlock = currentBlockChain.getLast();

        // reward transaction
        Transaction transaction = new Transaction(new Wallet(),
                WalletData.getInstance().getWallet().getPublicKey().getEncoded(), 100, latestBlock.getLedgerId() + 1,
                signing);

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
            List<Transaction> transactions = block.getTransactionLedger();
            for (Transaction transaction : transactions) {
                if (Boolean.FALSE.equals(transaction.isVerified(signing))) {
                    throw new GeneralSecurityException("Transaction validation failed");
                }
            }
        }
    }

    /**
     * This method prepares and returns a sorted, up-to-date list of transactions
     * that the UI can observe and display. FXCollections.observableArrayList: Wraps
     * the newBlockTransactionsFX into an ObservableList, which notifies JavaFX UI
     * components of any changes
     */
    public ObservableList<Transaction> getTransactionLedgerFX() {
        newBlockTransactionsFX.clear();
        newBlockTransactions.sort(transactionComparator);
        newBlockTransactionsFX.addAll(newBlockTransactions);
        return FXCollections.observableArrayList(newBlockTransactionsFX);
    }

    /**
     * This method determines the consensus blockchain between the current
     * blockchain and a received blockchain. It verifies the validity of the
     * received blockchain and compares it with the current blockchain to decide
     * which one to keep based on various criteria such as creation time, mining
     * points, and transaction ledgers. <br>
     * we don’t trust any peer, so we always verify anything we get from them.
     * 
     *
     * @param receivedBC The received blockchain to be compared with the current
     *                   blockchain.
     * @return The consensus blockchain, which could be the current blockchain or
     *         the received blockchain.
     */
    public LinkedList<Block> getBlockchainConsensus(LinkedList<Block> receivedBC) {
        try {
            // Verify the validity of the received blockchain.
            verifyBlockChain(receivedBC);
            // Check if we have received an identical blockchain.
            if (!Arrays.equals(receivedBC.getLast().getCurrHash(), getCurrentBlockChain().getLast().getCurrHash())) {
                // when both blockchains have different miners of the last block
                // it means that it’s time for us to perform some
                // consensus checks to determine which miner gets to mine the last block.
                if (checkIfOutdated(receivedBC) != null) {
                    return getCurrentBlockChain();
                } else {
                    if (checkWhichIsCreatedFirst(receivedBC) != null) {
                        return getCurrentBlockChain();
                    } else {
                        // now received blockchain is valid, and both blockchains
                        // are up-to-date and identical until the last block.

                        // to check which blockchain’s last block we are going to use,
                        if (compareMiningPointsAndLuck(receivedBC) != null) {
                            return getCurrentBlockChain();
                        }
                    }
                }
                // if only the transaction ledgers are different then combine them.
            } else if (!receivedBC.getLast().getTransactionLedger()
                    .equals(getCurrentBlockChain().getLast().getTransactionLedger())) {
                updateTransactionLedgers(receivedBC);
                System.out.println("Transaction ledgers updated");
                return receivedBC;
            } else {
                System.out.println("blockchains are identical");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return receivedBC;
    }

    /**
     * this method shares any transaction that might be missing between the local
     * and the received blockchain from their last blocks between them. it ensures
     * that both blockchains have the same set of transactions in their last blocks
     * by adding any missing transactions from one blockchain to the other.
     * 
     * @param receivedBC The received blockchain to compare and update transactions
     *                   with.
     * @throws Exception If an error occurs while adding transactions or sorting the
     *                   ledgers.
     */
    private void updateTransactionLedgers(LinkedList<Block> receivedBC) throws Exception {
        // loop over the transactions of the received blockchain’s last block ledger.
        for (Transaction transaction : receivedBC.getLast().getTransactionLedger()) {
            if (!getCurrentBlockChain().getLast().getTransactionLedger().contains(transaction)) {
                getCurrentBlockChain().getLast().getTransactionLedger().add(transaction);
                System.out.println("current ledger id = " + getCurrentBlockChain().getLast().getLedgerId()
                        + " transaction id = " + transaction.getLedgerId());
                addTransaction(transaction, false);
            }
        }
        getCurrentBlockChain().getLast().getTransactionLedger().sort(transactionComparator);
        // loop through our local blockchain’s last block transactions
        for (Transaction transaction : getCurrentBlockChain().getLast().getTransactionLedger()) {
            if (!receivedBC.getLast().getTransactionLedger().contains(transaction)) {
                receivedBC.getLast().getTransactionLedger().add(transaction);
            }
        }
        receivedBC.getLast().getTransactionLedger().sort(transactionComparator);
    }

    /**
     * Check each blockchain’s recorded mining points. In case of a tie, we will
     * determine the outcome by their recorded luck value, which is just a large
     * random number.
     * 
     * @param receivedBC The received blockchain to compare with the current
     *                   blockchain.
     * @return The updated current blockchain if it wins, or null if the received
     *         blockchain wins.
     * @throws Exception If an error occurs while adding transactions or sorting the
     */
    private LinkedList<Block> compareMiningPointsAndLuck(LinkedList<Block> receivedBC) throws Exception {
        // check if both blockchains have the same prevHashes to confirm they are both
        // contending to mine the last block
        // if they are the same compare the mining points and luck in case of equal
        // mining points
        // of last block to see who wins
        if (receivedBC.equals(getCurrentBlockChain())) {
            // If received block has more mining points points or luck in case of tie
            // transfer all transactions to the winning block and add them in DB.
            if (receivedBC.getLast().getMiningPoints() > getCurrentBlockChain().getLast().getMiningPoints()
                    || receivedBC.getLast().getMiningPoints().equals(getCurrentBlockChain().getLast().getMiningPoints())
                            && receivedBC.getLast().getLuck() > getCurrentBlockChain().getLast().getLuck()) {
                // remove the reward transaction from our losing block and
                // transfer the transactions to the winning block
                getCurrentBlockChain().getLast().getTransactionLedger().remove(0);
                for (Transaction transaction : getCurrentBlockChain().getLast().getTransactionLedger()) {
                    if (!receivedBC.getLast().getTransactionLedger().contains(transaction)) {
                        receivedBC.getLast().getTransactionLedger().add(transaction);
                    }
                }
                receivedBC.getLast().getTransactionLedger().sort(transactionComparator);
                // we are returning the mining points since our local block lost.
                setMiningPoints(BlockchainData.getInstance().getMiningPoints()
                        + getCurrentBlockChain().getLast().getMiningPoints());
                new BlockDAOImpl().replaceBlockchainInDatabase(receivedBC);
                setCurrentBlockChain(new LinkedList<>());
                loadBlockChain();
                System.out.println("Received blockchain won!");
            } else {
                // remove the reward transaction from their losing block and transfer
                // the transactions to our winning block
                receivedBC.getLast().getTransactionLedger().remove(0);
                for (Transaction transaction : receivedBC.getLast().getTransactionLedger()) {
                    if (!getCurrentBlockChain().getLast().getTransactionLedger().contains(transaction)) {
                        getCurrentBlockChain().getLast().getTransactionLedger().add(transaction);
                        addTransaction(transaction, false);
                    }
                }
                getCurrentBlockChain().getLast().getTransactionLedger().sort(transactionComparator);
                return getCurrentBlockChain();
            }
        }
        return null;
    }

    /**
     * determine if both blockchains have the same initial block. If they don’t, we
     * will use the one that got created first.
     * 
     * @param receivedBC The received blockchain to compare with the current
     * @return The updated current blockchain if it wins, or null if the received
     * @throws Exception If an error occurs while adding transactions or sorting the
     */
    private Object checkWhichIsCreatedFirst(LinkedList<Block> receivedBC) throws Exception {
        // Compare timestamps to see which one is created first.
        long initRcvBlockTime = LocalDateTime.parse(receivedBC.getFirst().getTimeStamp()).toEpochSecond(ZoneOffset.UTC);
        long initLocalBlockTIme = LocalDateTime.parse(getCurrentBlockChain().getFirst().getTimeStamp())
                .toEpochSecond(ZoneOffset.UTC);
        if (initRcvBlockTime < initLocalBlockTIme) {
            // we reset the mining points since we weren't contributing until now.
            setMiningPoints(0);
            new BlockDAOImpl().replaceBlockchainInDatabase(receivedBC);
            setCurrentBlockChain(new LinkedList<>());
            loadBlockChain();
            System.out.println("PeerClient blockchain won!, PeerServer's BC was old");
        } else if (initLocalBlockTIme < initRcvBlockTime) {
            return getCurrentBlockChain();
        }
        return null;
    }

    /**
     * determines if any method is outdated and discards it. If the received one is
     * outdated, then we keep using ours. If ours is outdated, we discard ours and
     * use the received one from this point forward. If both are outdated, then we
     * do nothing regarding consensus, and we wait to receive an up-­to-­date
     * blockchain. If both are up-to-date, then we move on with the consensus
     * checks.
     * 
     * @param receivedBC The received blockchain to be compared with the current
     * @return The updated current blockchain if it wins, or null if the received
     * @throws Exception If an error occurs while adding transactions or sorting the
     */
    private LinkedList<Block> checkIfOutdated(LinkedList<Block> receivedBC) throws Exception {
        // Check how old the blockchains are.
        long lastMinedLocalBlock = LocalDateTime.parse(getCurrentBlockChain().getLast().getTimeStamp())
                .toEpochSecond(ZoneOffset.UTC);
        long lastMinedRcvdBlock = LocalDateTime.parse(receivedBC.getLast().getTimeStamp())
                .toEpochSecond(ZoneOffset.UTC);
        // if both are old just do nothing
        if ((lastMinedLocalBlock + TIMEOUT_INTERVAL) < LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
                && (lastMinedRcvdBlock + TIMEOUT_INTERVAL) < LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)) {
            System.out.println("both are old check other peers");
            // If your blockchain is old but the received one is new use the received one
        } else if ((lastMinedLocalBlock + TIMEOUT_INTERVAL) < LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
                && (lastMinedRcvdBlock + TIMEOUT_INTERVAL) >= LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)) {
            // we reset the mining points since we weren't contributing until now.
            setMiningPoints(0);
            new BlockDAOImpl().replaceBlockchainInDatabase(receivedBC);
            setCurrentBlockChain(new LinkedList<>());
            loadBlockChain();
            System.out.println("received blockchain won!, local BC was old");
            // If received one is old but local is new send ours to them
        } else if ((lastMinedLocalBlock + TIMEOUT_INTERVAL) > LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
                && (lastMinedRcvdBlock + TIMEOUT_INTERVAL) < LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)) {

            return getCurrentBlockChain();
        }
        return null;
    }
}
