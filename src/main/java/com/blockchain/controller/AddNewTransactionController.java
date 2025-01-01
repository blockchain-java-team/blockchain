package com.blockchain.controller;

import com.blockchain.dao.TransactionDAO;
import com.blockchain.dao.impl.TransactionDAOImpl;
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
        Base64.Decoder decoder = Base64.getDecoder();
        Signature signing = Signature.getInstance("SHA256withDSA");
//        Retrieves the ledgerId from the first transaction in the blockchain ledger (TransactionLedgerFX)
        Integer ledgerId = BlockchainData.getInstance().getTransactionLedgerFX().get(0).getLedgerId();
//        Decodes the recipient's address (entered as a Base64 string in the toAddress field) into a byte array
        byte[] sendB = decoder.decode(toAddress.getText());
        Transaction transaction = new Transaction(WalletData.getInstance()
                .getWallet(),sendB ,Integer.parseInt(value.getText()), ledgerId, signing);
        try {
			BlockchainData.getInstance().addTransaction(transaction,false);	        
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        BlockchainData.getInstance().addTransactionState(transaction);
    }
}