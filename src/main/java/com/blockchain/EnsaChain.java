//package com.blockchain;
//
//import com.blockchain.model.Block;
//import com.blockchain.model.Transaction;
//import com.blockchain.model.Wallet;
//import com.blockchain.service.WalletData;
//import com.blockchain.service.BlockchainData;
//import com.blockchain.state.BlockchainState;
//import com.blockchain.threads.MiningThread;
//import lombok.Getter;
//
//import java.security.Signature;
//import java.time.LocalDateTime;
//
//@Getter
//public class EnsaChain {
//    public static void main(String[] args) throws Exception {
//
//
//        if (BlockchainState.getWallets().isEmpty()) {
//            Wallet newWallet = new Wallet();
//            BlockchainState.addWallet(newWallet);
//        }
//
//        WalletData.getInstance().loadWallet();
//
//
//        Signature transSignature = Signature.getInstance("SHA256withDSA");
//
//        Transaction initBlockRewardTransaction = null;
//        if (BlockchainState.getBlocks().isEmpty()) {
//            Block firstBlock = new Block();
//            firstBlock.setMinedBy(WalletData.getInstance().getWallet().getPublicKey().getEncoded());
//            firstBlock.setTimeStamp(LocalDateTime.now().toString());
//            //helper class.
//            Signature signing = Signature.getInstance("SHA256withDSA");
//            signing.initSign(WalletData.getInstance().getWallet().getPrivateKey());
//            signing.update(firstBlock.toString().getBytes());
//            firstBlock.setCurrHash(signing.sign());
//
//            BlockchainState.addBlock(firstBlock);
//
//            initBlockRewardTransaction = new Transaction(WalletData.getInstance().getWallet(), WalletData.getInstance().getWallet().getPublicKey().getEncoded(), 100, 1, transSignature);
//
//            // This TX is for the creator of the first block in the blockchain
//            BlockchainData.getInstance().addTransaction(initBlockRewardTransaction, true);
//            BlockchainData.getInstance().addTransactionState(initBlockRewardTransaction);
//        }
//
//        BlockchainData.getInstance().loadBlockChain();
//
//        new MiningThread().start();
//
//        Wallet walletA = new Wallet();
//
//        // should work
//        Transaction t = new Transaction(WalletData.getInstance().getWallet(), walletA.getPublicKey().getEncoded(), 50, 1, transSignature);
//        BlockchainData.getInstance().addTransaction(t, false);
//        BlockchainData.getInstance().addTransactionState(t);
//
//        // should not work
//        Thread.sleep(16000);
//        Transaction t2 = new Transaction(WalletData.getInstance().getWallet(), walletA.getPublicKey().getEncoded(), 100050, 1, transSignature);
//        BlockchainData.getInstance().addTransaction(t2, false);
//        BlockchainData.getInstance().addTransactionState(t2);
//
//    }
//}


package com.blockchain;

import com.blockchain.model.Block;
import com.blockchain.model.Transaction;
import com.blockchain.model.Wallet;
import com.blockchain.service.WalletData;
import com.blockchain.service.BlockchainData;
import com.blockchain.state.BlockchainState;
import com.blockchain.threads.MiningThread;

import java.security.Signature;
import java.time.LocalDateTime;

public class EnsaChain {
    public static void main(String[] args) throws Exception {

        if (BlockchainState.getWallets().isEmpty()) {
            Wallet newWallet = new Wallet();
            BlockchainState.addWallet(newWallet);
        }

        WalletData.getInstance().loadWallet();

        Signature transSignature = Signature.getInstance("SHA256withDSA");

        Transaction initBlockRewardTransaction = null;
        if (BlockchainState.getBlocks().isEmpty()) {
            Block firstBlock = new Block();
            firstBlock.setMinedBy(WalletData.getInstance().getWallet().getPublicKey().getEncoded());
            firstBlock.setTimeStamp(LocalDateTime.now().toString());
            Signature signing = Signature.getInstance("SHA256withDSA");
            signing.initSign(WalletData.getInstance().getWallet().getPrivateKey());
            signing.update(firstBlock.toString().getBytes());
            firstBlock.setCurrHash(signing.sign());

            BlockchainState.addBlock(firstBlock);

            initBlockRewardTransaction = new Transaction(
                    WalletData.getInstance().getWallet(),
                    WalletData.getInstance().getWallet().getPublicKey().getEncoded(),
                    100, 1, transSignature
            );

            BlockchainData.getInstance().addTransaction(initBlockRewardTransaction, true);
            BlockchainData.getInstance().addTransactionState(initBlockRewardTransaction);
        }

        BlockchainData.getInstance().loadBlockChain();

        new MiningThread().start();

        Wallet walletA = new Wallet();

        // Check balance before proceeding with transaction
        int balance = Integer.parseInt(BlockchainData.getInstance().getWalletBalance());
        int transactionAmount = 50;

        // Adjust this value and condition as needed
        if (balance >= transactionAmount) {
            Transaction t = new Transaction(
                    WalletData.getInstance().getWallet(),
                    walletA.getPublicKey().getEncoded(),
                    transactionAmount, 1, transSignature
            );
            BlockchainData.getInstance().addTransaction(t, false);
            BlockchainData.getInstance().addTransactionState(t);
        } else {
            System.out.println("Insufficient funds for transaction 1.");
        }

        // should not work
        Thread.sleep(16000);
        int transactionAmount2 = 100050;
        if (balance >= transactionAmount2) {
            Transaction t2 = new Transaction(
                    WalletData.getInstance().getWallet(),
                    walletA.getPublicKey().getEncoded(),
                    transactionAmount2, 1, transSignature
            );
            BlockchainData.getInstance().addTransaction(t2, false);
            BlockchainData.getInstance().addTransactionState(t2);
        } else {
            System.out.println("Insufficient funds for transaction 2.");
        }
    }
}