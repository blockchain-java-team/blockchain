package com.blockchain;

import com.blockchain.model.Block;
import com.blockchain.model.Transaction;
import com.blockchain.model.Wallet;
import com.blockchain.service.WalletData;
import com.blockchain.service.BlockchainData;
import com.blockchain.state.BlockchainState;
import com.blockchain.threads.MiningThread;
import com.blockchain.threads.UI;

import javafx.application.Application;
import javafx.stage.Stage;
import lombok.Getter;
import java.security.Signature;
import java.time.LocalDateTime;


@Getter
public class EnsaChain extends Application {
    public static void main(String[] args) throws Exception {
        launch(args); // calls the start method
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        new UI().start(primaryStage);
        new MiningThread().start();
    }

    @Override
    public void init() throws Exception {
        if (BlockchainState.wallets.isEmpty()) {
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
    }
}