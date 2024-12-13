package com.blockchain.dao.impl;

import com.blockchain.dao.WalletDAO;
import com.blockchain.model.Wallet;
import com.blockchain.util.DatabaseConnection;
import lombok.Getter;

import java.security.*;
import java.security.spec.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class WalletDAOImpl implements WalletDAO {

    @Getter
    private static final String DB_URL = "jdbc:sqlite:db/WALLET.db";


    /**
     * Save a Wallet object into the database.
     */
    @Override
    public void save(Wallet wallet) throws Exception {
        String query = "INSERT INTO Wallet (PUBLIC_KEY, PRIVATE_KEY) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            Base64.Encoder encoder = Base64.getEncoder();
            stmt.setString(1, encoder.encodeToString(wallet.getPublicKey().getEncoded()));
            stmt.setString(2, encoder.encodeToString(wallet.getPrivateKey().getEncoded()));

            stmt.executeUpdate();
        }
    }

    /**
     * Retrieve all Wallet objects from the database.
     */
    @Override
    public List<Wallet> findAll() throws Exception {
        List<Wallet> wallets = new ArrayList<>();
        String query = "SELECT * FROM Wallet";
        try (Connection conn = DatabaseConnection.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                wallets.add(createWalletFromResultSet(rs));
            }
        }
        return wallets;
    }

    /**
     * Find a Wallet by its public key.
     */
    @Override
    public Wallet findById(String publicKey) throws Exception {
        String query = "SELECT * FROM Wallet WHERE PUBLIC_KEY = ?";
        try (Connection conn = DatabaseConnection.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, publicKey);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return createWalletFromResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * Delete a Wallet by its public key.
     */
    @Override
    public void deleteById(String publicKey) throws Exception {
        String query = "DELETE FROM Wallet WHERE PUBLIC_KEY = ?";
        try (Connection conn = DatabaseConnection.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, publicKey);
            stmt.executeUpdate();
        }
    }

    /**
     * Helper method to construct a Wallet object from a ResultSet.
     */
    private Wallet createWalletFromResultSet(ResultSet rs) throws Exception {
        Base64.Decoder decoder = Base64.getDecoder();
        KeyFactory keyFactory = KeyFactory.getInstance("DSA");

        // Deserialize public key
        byte[] publicKeyBytes = decoder.decode(rs.getString("PUBLIC_KEY"));
        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

        // Deserialize private key
        byte[] privateKeyBytes = decoder.decode(rs.getString("PRIVATE_KEY"));
        PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));


        // Return Wallet object
        return new Wallet(publicKey, privateKey);
    }
}