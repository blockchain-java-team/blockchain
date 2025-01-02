package com.blockchain.controller;

import com.blockchain.model.Transaction;
import com.blockchain.service.BlockchainData;
import com.blockchain.service.WalletData;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.security.GeneralSecurityException;
import java.security.Signature;
import java.util.Base64;

public class AddNewTransactionController {

    @FXML
    private TextField toAddress;

    @FXML
    private TextField value;

    @FXML
    public void createNewTransaction() throws GeneralSecurityException {
        // Validate input fields
        if (toAddress.getText().isEmpty() || value.getText().isEmpty()) {
            showError("Input Error", "To Address and Value cannot be empty.");
            return;
        }

        Base64.Decoder decoder = Base64.getDecoder();
        Signature signing = Signature.getInstance("SHA256withDSA");
        Integer ledgerId = BlockchainData.getInstance().getTransactionLedgerFX().get(0).getLedgerId();

        // Validate public key
        byte[] sendB;
        try {
            sendB = decoder.decode(toAddress.getText());
        } catch (IllegalArgumentException e) {
            showError("To Address Format", "To address is not in the right format");
            return;
        }

        // Validate numeric value
        int amount = 0;
        try {
            amount = Integer.parseInt(value.getText());
        } catch (NumberFormatException e) {
            showError("Validation Error", "Amount should be an integer");
            return;
        }

        if (amount <= 0) {
            showError("Validation Error", "Value must be a positive number.");
            return;
        }

        Transaction transaction = new Transaction(WalletData.getInstance().getWallet(), sendB, amount, ledgerId,
                signing);
        try {
            BlockchainData.getInstance().addTransaction(transaction, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        BlockchainData.getInstance().addTransactionState(transaction);
        closeWindow();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null); // No header
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) toAddress.getScene().getWindow(); // Get current stage
        stage.close(); // Close the window
    }
}