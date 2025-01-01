package com.blockchain.dao.impl;

import com.blockchain.dao.TransactionDAO;
import com.blockchain.model.Transaction;
import com.blockchain.util.DatabaseConnection;
import lombok.Getter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAOImpl implements TransactionDAO {
    @Getter
    private static final String DB_URL = "jdbc:sqlite:db/blockchain.db";

    @Override
    public void save(Transaction transaction) throws Exception {
        String query = "INSERT INTO TRANSACTIONS (SENDER, \"TO\", VALUE, SIGNATURE, LEDGER_ID, CREATED_ON) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(DB_URL);
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setBytes(1, transaction.getFrom());
            stmt.setBytes(2, transaction.getTo());
            stmt.setInt(3, transaction.getValue());
            stmt.setBytes(4, transaction.getSignature());
            stmt.setInt(5, transaction.getLedgerId());
            stmt.setString(6, transaction.getTimestamp());

            stmt.executeUpdate();
        }
    }

    @Override
    public List<Transaction> findAll() throws Exception {
        List<Transaction> transactions = new ArrayList<>();
        String query = "SELECT * FROM TRANSACTIONS";
        try (Connection conn = DatabaseConnection.getConnection(DB_URL);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                transactions.add(new Transaction(rs.getBytes("SENDER"), rs.getBytes("TO"), rs.getInt("VALUE"),
                        rs.getBytes("SIGNATURE"), rs.getInt("LEDGER_ID"), rs.getString("CREATED_ON")));
            }
        }
        return transactions;
    }

    @Override
    public List<Transaction> findByLedgerId(int ledgerId) throws Exception {
        String query = "SELECT * FROM TRANSACTIONS WHERE LEDGER_ID = ?";
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection(DB_URL);
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, ledgerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(new Transaction(rs.getBytes("SENDER"), rs.getBytes("TO"), rs.getInt("VALUE"),
                            rs.getBytes("SIGNATURE"), rs.getInt("LEDGER_ID"), rs.getString("CREATED_ON")));
                }
            }
        }
        return transactions;
    }

    @Override
    public void deleteByLedgerId(int ledgerId) throws Exception {
        String query = "DELETE FROM TRANSACTIONS WHERE LEDGER_ID = ?";
        try (Connection conn = DatabaseConnection.getConnection(DB_URL);
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, ledgerId);
            stmt.executeUpdate();
        }
    }
}
