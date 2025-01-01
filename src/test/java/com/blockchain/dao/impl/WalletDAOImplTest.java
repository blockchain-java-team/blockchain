package com.blockchain.dao.impl;

import com.blockchain.dao.WalletDAO;
import com.blockchain.model.Wallet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WalletDAOImplTest {

    private WalletDAO walletDAO;

    @BeforeEach
    void setUp() throws Exception {
        walletDAO = new WalletDAOImpl();
        createTestTable();
    }

    @AfterEach
    void tearDown() throws Exception {
        clearTestTable();
    }

    @Test
    void save() throws Exception {
        // Create a new wallet
        Wallet wallet = new Wallet();
        walletDAO.save(wallet);

        // Verify the wallet is saved
        List<Wallet> wallets = walletDAO.findAll();
        assertEquals(1, wallets.size());
        assertEquals(wallet.getPublicKey(), wallets.getFirst().getPublicKey());

        // Log the content of the Wallet table
        logWalletTableContent();
    }

    @Test
    void findAll() throws Exception {
        // Insert multiple wallets
        Wallet wallet1 = new Wallet();
        Wallet wallet2 = new Wallet();
        walletDAO.save(wallet1);
        walletDAO.save(wallet2);

        // Verify all wallets are retrieved
        List<Wallet> wallets = walletDAO.findAll();
        assertEquals(2, wallets.size());

        // Log the content of the Wallet table
        logWalletTableContent();
    }

    @Test
    void findById() throws Exception {
        // Create a new wallet and save it to the database
        Wallet wallet = new Wallet();
        walletDAO.save(wallet);

        // Now retrieve the wallet by public key
        String publicKey = Base64.getEncoder().encodeToString(wallet.getPublicKey().getEncoded());
        Wallet retrievedWallet = walletDAO.findById(publicKey);

        // Assert that the retrieved wallet is not null
        assertNotNull(retrievedWallet);
        assertEquals(publicKey, Base64.getEncoder().encodeToString(retrievedWallet.getPublicKey().getEncoded()));

        // Log the content of the Wallet table
        logWalletTableContent();
    }

    @Test
    void deleteById() throws Exception {
        // Insert a wallet
        Wallet wallet = new Wallet();
        walletDAO.save(wallet);

        // Delete the wallet by public key
        walletDAO.deleteById(Base64.getEncoder().encodeToString(wallet.getPublicKey().getEncoded()));

        // Verify the wallet is deleted
        Wallet deletedWallet = walletDAO.findById(Base64.getEncoder().encodeToString(wallet.getPublicKey().getEncoded()));
        assertNull(deletedWallet);

        // Log the content of the Wallet table
        logWalletTableContent();
    }

    /**
     * Helper method to create the Wallet table for tests.
     */
    private void createTestTable() throws Exception {
        String query = """
            CREATE TABLE IF NOT EXISTS Wallet (
                PUBLIC_KEY TEXT PRIMARY KEY NOT NULL,
                PRIVATE_KEY TEXT NOT NULL
            )
        """;
        try (Connection conn = DriverManager.getConnection(WalletDAOImpl.getDB_URL());
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(query);
        }
    }

    /**
     * Helper method to clear the Wallet table after each test.
     */
    private void clearTestTable() throws Exception {
        String query = "DELETE FROM Wallet";
        try (Connection conn = DriverManager.getConnection(WalletDAOImpl.getDB_URL());
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(query);
        }
    }

    /**
     * Helper method to log the current contents of the Wallet table.
     */
    private void logWalletTableContent() throws Exception {
        String query = "SELECT * FROM Wallet";
        try (Connection conn = DriverManager.getConnection(WalletDAOImpl.getDB_URL());
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("Wallet Table Content:");
            while (rs.next()) {
                String publicKey = rs.getString("PUBLIC_KEY");
                String privateKey = rs.getString("PRIVATE_KEY");
                System.out.println("Public Key: " + publicKey);
                System.out.println("Private Key: " + privateKey);
            }
        }
    }
}
