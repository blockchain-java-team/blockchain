package com.blockchain.state;

import com.blockchain.dao.BlockDAO;
import com.blockchain.dao.TransactionDAO;
import com.blockchain.dao.WalletDAO;
import com.blockchain.dao.impl.BlockDAOImpl;
import com.blockchain.dao.impl.TransactionDAOImpl;
import com.blockchain.dao.impl.WalletDAOImpl;
import com.blockchain.model.Block;
import com.blockchain.model.Transaction;
import com.blockchain.model.Wallet;

import java.util.List;

public class BlockchainState {
    private static final BlockDAO blockDAO = new BlockDAOImpl();
    private static final WalletDAO walletDAO = new WalletDAOImpl();
    private static final TransactionDAO transactionDAO = new TransactionDAOImpl();

    public static List<Block> getBlocks() throws Exception {
        return blockDAO.findAll();
    }

    public static void addBlock(Block block) throws Exception {
        blockDAO.save(block);
    }

    public static List<Wallet> getWallets() throws Exception {
        return walletDAO.findAll();
    }

    public static void addWallet(Wallet wallet) throws Exception {
        walletDAO.save(wallet);
    }

    public static List<Transaction> getTransactions() throws Exception {
        return transactionDAO.findAll();
    }

    public static void addTransaction(Transaction transaction) throws Exception {
        transactionDAO.save(transaction);
    }
}
