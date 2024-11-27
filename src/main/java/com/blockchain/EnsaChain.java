package com.blockchain;

import com.blockchain.model.Block;
import com.blockchain.model.Transaction;
import com.blockchain.model.Wallet;
import com.blockchain.service.WalletData;
import com.blockchain.service.BlockchainData;
import com.blockchain.state.BlockchainState;
import com.blockchain.threads.MiningThread;
import lombok.Getter;

import java.security.Signature;
import java.time.LocalDateTime;

@Getter
public class EnsaChain {
    public static void main(String[] args) throws Exception {


        if (BlockchainState.wallets.isEmpty()) {
            Wallet newWallet = new Wallet();
            BlockchainState.wallets.add(newWallet);
        }

        WalletData.getInstance().loadWallet();


        Signature transSignature = Signature.getInstance("SHA256withDSA");

        Transaction initBlockRewardTransaction = null;
        if (BlockchainState.blocks.isEmpty()) {
            Block firstBlock = new Block();
            firstBlock.setMinedBy(WalletData.getInstance().getWallet().getPublicKey().getEncoded());
            firstBlock.setTimeStamp(LocalDateTime.now().toString());
            //helper class.
            Signature signing = Signature.getInstance("SHA256withDSA");
            signing.initSign(WalletData.getInstance().getWallet().getPrivateKey());
            signing.update(firstBlock.toString().getBytes());
            firstBlock.setCurrHash(signing.sign());

            BlockchainState.blocks.add(firstBlock);

            initBlockRewardTransaction = new Transaction(WalletData.getInstance().getWallet(), WalletData.getInstance().getWallet().getPublicKey().getEncoded(), 100, 1, transSignature);

            // This TX is for the creator of the first block in the blockchain
            BlockchainData.getInstance().addTransaction(initBlockRewardTransaction, true);
            BlockchainData.getInstance().addTransactionState(initBlockRewardTransaction);
        }

        BlockchainData.getInstance().loadBlockChain();

        new MiningThread().start();

        Wallet walletA = new Wallet();

        // should work
        Transaction t = new Transaction(WalletData.getInstance().getWallet(), walletA.getPublicKey().getEncoded(), 50, 1, transSignature);
        BlockchainData.getInstance().addTransaction(t, false);
        BlockchainData.getInstance().addTransactionState(t);

        // should not work
        Thread.sleep(16000);
        Transaction t2 = new Transaction(WalletData.getInstance().getWallet(), walletA.getPublicKey().getEncoded(), 100050, 1, transSignature);
        BlockchainData.getInstance().addTransaction(t2, false);
        BlockchainData.getInstance().addTransactionState(t2);

    }
}