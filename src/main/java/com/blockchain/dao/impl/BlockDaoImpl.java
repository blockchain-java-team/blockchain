package com.blockchain.dao.impl;

import com.blockchain.dao.BlockDAO;
import com.blockchain.model.Block;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BlockDAOImpl implements BlockDAO {
    private final String url = "jdbc:sqlite:db/blockchain.db";

    @Override
    public void save(Block block) {
        String query = "INSERT INTO Block (id, previousHash, timestamp, data) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, block.getId());
            stmt.setString(2, block.getPreviousHash());
            stmt.setLong(3, block.getTimestamp());
            stmt.setString(4, block.getData());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Block> findAll() {
        List<Block> blocks = new ArrayList<>();
        String query = "SELECT * FROM Block";
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                blocks.add(new Block(
                        rs.getString("id"),
                        rs.getString("previousHash"),
                        rs.getLong("timestamp"),
                        rs.getString("data")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return blocks;
    }

    @Override
    public Block findById(String id) {
        String query = "SELECT * FROM Block WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Block(
                            rs.getString("id"),
                            rs.getString("previousHash"),
                            rs.getLong("timestamp"),
                            rs.getString("data")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void delete(String id) {
        String query = "DELETE FROM Block WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}