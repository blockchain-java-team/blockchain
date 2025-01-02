package com.blockchain.controller;

import com.blockchain.model.Transaction;
import com.blockchain.service.BlockchainData;
import com.blockchain.service.WalletData;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import com.blockchain.dao.*;
import com.blockchain.dao.impl.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

public class MainWindowController {

    @FXML
    public TableView<Transaction> tableview = new TableView<>(); // this is read-only UI table
    @FXML
    private TableColumn<Transaction, String> from;
    @FXML
    private TableColumn<Transaction, String> to;
    @FXML
    private TableColumn<Transaction, Integer> value;
    @FXML
    private TableColumn<Transaction, String> createdOn;
    @FXML
    private TableColumn<Transaction, String> signature;
    @FXML
    private BorderPane borderPane;
    @FXML
    private TextField eCoins;
    @FXML
    private TextArea publicKey;

    public void initialize() {
 
        from.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFromFX()));
        to.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getToFX()));
        value.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getValue()));
        signature.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSignatureFX()));
        createdOn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTimestamp()));

        List<Transaction> transactions = new ArrayList<>();
        TransactionDAO tdi = new TransactionDAOImpl();
        try {
            transactions = tdi.findAll();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ObservableList<Transaction> transactionList = FXCollections.observableArrayList(transactions);
        tableview.setItems(transactionList);

        if (!transactionList.isEmpty()) {
            tableview.getSelectionModel().select(0);
        }
    }

    @FXML
    public void toNewTransactionController() {
        Dialog<ButtonType> newTransactionController = new Dialog<>();
        newTransactionController.initOwner(borderPane.getScene().getWindow());
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("../view/AddNewTransactionWindow.fxml"));
        try {
            newTransactionController.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            System.out.println("Cant load dialog");
            e.printStackTrace();
            return;
        }
        newTransactionController.getDialogPane().getButtonTypes().add(ButtonType.FINISH);
        newTransactionController.getDialogPane().lookupButton(ButtonType.FINISH).setVisible(false);
        Optional<ButtonType> result = newTransactionController.showAndWait();
        if (result.isPresent()) {
            tableview.setItems(BlockchainData.getInstance().getTransactionLedgerFX());
            eCoins.setText(BlockchainData.getInstance().getWalletBalance());
        }
    }

    @FXML
    public void refresh() {
        tableview.setItems(BlockchainData.getInstance().getTransactionLedgerFX());
        tableview.getSelectionModel().select(0);
        eCoins.setText(BlockchainData.getInstance().getWalletBalance());
    }
}