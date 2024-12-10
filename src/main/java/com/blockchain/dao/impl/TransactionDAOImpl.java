package com.blockchain.dao.impl;

import com.blockchain.dao.TransactionDAO;
import com.blockchain.model.Transaction;
import lombok.Getter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAOImpl implements TransactionDAO {
    @Getter
    private static final String DB_URL = "jdbc:sqlite:db/blockchain.db";

    @Override
    public void save(Transaction transaction) throws Exception {
        String query = "INSERT INTO TRANSACTIONS (FROM, TO, VALUE, SIGNATURE, LEDGER_ID, CREATED_ON) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
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
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                transactions.add(new Transaction(
                        rs.getBytes("FROM"),
                        rs.getBytes("TO"),
                        rs.getInt("VALUE"),
                        rs.getBytes("SIGNATURE"),
                        rs.getInt("LEDGER_ID"),
                        rs.getString("CREATED_ON")
                ));
            }
        }
        return transactions;
    }

    @Override
    public Transaction findByLedgerId(int ledgerId) throws Exception {
        String query = "SELECT * FROM TRANSACTIONS WHERE LEDGER_ID = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, ledgerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Transaction(
                            rs.getBytes("FROM"),
                            rs.getBytes("TO"),
                            rs.getInt("VALUE"),
                            rs.getBytes("SIGNATURE"),
                            rs.getInt("LEDGER_ID"),
                            rs.getString("CREATED_ON")
                    );
                }
            }
        }
        return null;
    }

    @Override
    public void deleteByLedgerId(int ledgerId) throws Exception {
        String query = "DELETE FROM TRANSACTIONS WHERE LEDGER_ID = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, ledgerId);
            stmt.executeUpdate();
        }
    }
}