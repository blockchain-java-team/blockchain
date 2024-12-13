package com.blockchain.dao.impl;

import com.blockchain.dao.BlockDAO;
import com.blockchain.model.Block;
import com.blockchain.model.Transaction;
import com.blockchain.service.BlockchainData;
import com.blockchain.util.DatabaseConnection;
import lombok.Getter;

import java.security.GeneralSecurityException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class BlockDAOImpl implements BlockDAO {

    @Getter
    private static final String DB_URL = "jdbc:sqlite:db/blockchain.db";

    @Override
    public void save(Block block) throws Exception {
        String query = "INSERT INTO BLOCKS (ID, PREVIOUS_HASH, CURRENT_HASH, LEDGER_ID, CREATED_ON, CREATED_BY, MININGS_POINTS, LUCK) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, block.getLedgerId());
            stmt.setBytes(2, block.getPrevHash());
            stmt.setBytes(3, block.getCurrHash());
            stmt.setInt(4, block.getLedgerId());
            stmt.setString(5, block.getTimeStamp());
            stmt.setBytes(6, block.getMinedBy());
            stmt.setInt(7, block.getMiningPoints());
            stmt.setDouble(8, block.getLuck());

            stmt.executeUpdate();
        }
    }

    @Override
    public List<Block> findAll() throws Exception {
        List<Block> blocks = new ArrayList<>();
        String query = "SELECT * FROM BLOCKS";
        try (Connection conn = DatabaseConnection.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                blocks.add(new Block(
                        rs.getBytes("PREVIOUS_HASH"),
                        rs.getBytes("CURRENT_HASH"),
                        rs.getString("CREATED_ON"),
                        rs.getBytes("CREATED_BY"),
                        rs.getInt("LEDGER_ID"),
                        rs.getInt("MININGS_POINTS"),
                        rs.getDouble("LUCK"),
                        new ArrayList<>() // Placeholder for transaction ledger
                ));
            }
        }
        return blocks;
    }

    @Override
    public Block findById(int id) throws Exception {
        String query = "SELECT * FROM BLOCKS WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Block(
                            rs.getBytes("PREVIOUS_HASH"),
                            rs.getBytes("CURRENT_HASH"),
                            rs.getString("CREATED_ON"),
                            rs.getBytes("CREATED_BY"),
                            rs.getInt("LEDGER_ID"),
                            rs.getInt("MININGS_POINTS"),
                            rs.getDouble("LUCK"),
                            new ArrayList<>() // Placeholder for transaction ledger
                    );
                }
            }
        }
        return null;
    }

    @Override
    public void deleteById(int id) throws Exception {
        String query = "DELETE FROM BLOCKS WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public void replaceBlockchainInDatabase(List<Block> receivedBC){
        try {
            Connection connection = DriverManager.getConnection(DB_URL);
            Statement clearDBStatement = connection.createStatement();
            clearDBStatement.executeUpdate("DELETE FROM BLOCKCHAIN WHERE 1");
            clearDBStatement.executeUpdate("DELETE FROM TRANSACTIONS WHERE 1");
            clearDBStatement.close();
            connection.close();
            for (Block block : receivedBC) {
                save(block);
                boolean rewardTransaction = true;
                Comparator<Transaction> transactionComparator = Comparator.comparing(Transaction::getTimestamp);
                block.getTransactionLedger().sort(transactionComparator);
                for (Transaction transaction : block.getTransactionLedger()) {
                    BlockchainData.getInstance().addTransaction(transaction, rewardTransaction);
                    rewardTransaction = false;
                }
            }
        } catch (SQLException | GeneralSecurityException e) {
            System.out.println("Problem with DB: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
}
