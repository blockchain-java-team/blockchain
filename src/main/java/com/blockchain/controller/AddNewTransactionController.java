package com.blockchain.controller;

import com.blockchain.model.Transaction;
import com.blockchain.service.BlockchainData;
import com.blockchain.service.WalletData;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import lombok.Getter;
import lombok.Setter;

import java.security.GeneralSecurityException;
import java.security.Signature;
import java.util.Base64;

public class AddNewTransactionController {

    @FXML
    @Getter
    @Setter
    private TextField toAddress;
    @FXML
    @Getter
    @Setter
    private TextField value;

    @FXML
    public void createNewTransaction() throws GeneralSecurityException {
    	System.out.println("i'm in yeeaaaah");
        Base64.Decoder decoder = Base64.getDecoder();
        System.out.println("i'm in yeeaaaahsub");
        Signature signing = Signature.getInstance("SHA256withDSA");
        System.out.println("i'm in yeeaaaahsub1");
//        Retrieves the ledgerId from the first transaction in the blockchain ledger (TransactionLedgerFX)
        Integer ledgerId = BlockchainData.getInstance().getTransactionLedgerFX().get(0).getLedgerId();
        System.out.println("i'm in yeeaaaahsub2");
//        Decodes the recipient's address (entered as a Base64 string in the toAddress field) into a byte array
        byte[] sendB = decoder.decode(toAddress.getText());
//        WalletData.getInstance().getWallet(): Retrieves the sender's wallet (likely the private/public keypair).
        System.out.println("i'm in yeeaaaah1");
        Transaction transaction = new Transaction(WalletData.getInstance()
                .getWallet(),sendB ,Integer.parseInt(value.getText()), ledgerId, signing);
        System.out.println("i'm in yeeaaaah2");
        try {
			BlockchainData.getInstance().addTransaction(transaction,false);
			System.out.println("i'm in yeeaaaah285");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("i'm in yeeaaaah286");
		}
        System.out.println("i'm in yeeaaaah3");
        BlockchainData.getInstance().addTransactionState(transaction);
        System.out.println("i'm in yeeaaaah4");
    }
}