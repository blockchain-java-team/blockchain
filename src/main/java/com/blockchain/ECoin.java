package com.blockchain
import com.blockchain.model.Block
import com.blockchain.model.Transcation
import com.blockchain.model.Wallet
import com.blockchain.ServiceData.WalletData;
import com.blockchain.threads.MiningThread;
import com.blockchain.threads.PeerClient;
import com.blockchain.threads.PeerServer;
import com.blockchain.threads.UI;
import javafx.application.Application;
import javafx.stage.Stage;
import java.security.*;
import java.sql.*;
import java.time.LocalDateTime;

public class ECoin extends Application {
    public static void main(String[] args) {
        // Lance l'application JavaFX
        launch(args); // La méthode launch() appelle automatiquement la méthode start() après l'initialisation
    }

    // La méthode start() est appelée après le lancement de l'application. Elle sert à configurer l'interface graphique et à démarrer les threads.
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Création et démarrage du thread UI pour afficher l'interface graphique
        new UI().start(primaryStage);  // L'interface utilisateur sera affichée dans la fenêtre principale (primaryStage)

        // Création et démarrage du thread PeerClient pour gérer la logique du client (se connecter à d'autres pairs)
        new PeerClient().start();  // Le client se connectera à d'autres pairs du réseau pour envoyer des requêtes

        // Création et démarrage du thread PeerServer pour gérer les connexions réseau côté serveur
        new PeerServer(6000).start();  // Le serveur écoute les connexions entrantes sur le port 6000

        // Création et démarrage du thread MiningThread pour effectuer le minage de la blockchain
        new MiningThread().start();  // Ce thread effectue des opérations de vérification et de consensus pour le minage
    }

    @Override
    public void init() {
        try {
            // Établir une connexion à la base de données wallet.db
            Connection walletConnection = DriverManager.getConnection(
                    "jdbc:sqlite:c:\\Users\\hamza\\IdeaProjects\\blockchain\\db\\WALLET.db"
            );

            // Créer un objet Statement pour exécuter des commandes SQL
            Statement walletStatement = walletConnection.createStatement();

            // Créer la table WALLET si elle n'existe pas déjà
            walletStatement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS WALLET (" +
                            "PRIVATE_KEY BLOB NOT NULL UNIQUE, " +  // Colonne pour la clé privée
                            "PUBLIC_KEY BLOB NOT NULL UNIQUE, " +  // Colonne pour la clé publique
                            "PRIMARY KEY (PRIVATE_KEY, PUBLIC_KEY)" +  // Clé primaire sur la paire clé privée/clé publique
                            ")"
            );

            // Vérifier si la table WALLET contient déjà des données
            ResultSet resultSet = walletStatement.executeQuery("SELECT * FROM WALLET");

            if (!resultSet.next()) {
                // Si aucune clé n'existe, générer une nouvelle paire de clés
                Wallet newWallet = new Wallet(); // Création d'un portefeuille avec une nouvelle paire de clés
                byte[] pubBlob = newWallet.getPublicKey().getEncoded(); // Encodage de la clé publique
                byte[] prvBlob = newWallet.getPrivateKey().getEncoded(); // Encodage de la clé privée

                // Préparer une commande d'insertion pour ajouter les clés dans la base de données
                PreparedStatement pstmt = walletConnection.prepareStatement(
                        "INSERT INTO WALLET(PRIVATE_KEY, PUBLIC_KEY) VALUES (?, ?)"
                );

                // Associer les valeurs des clés à la requête
                pstmt.setBytes(1, prvBlob); // Associer la clé privée
                pstmt.setBytes(2, pubBlob); // Associer la clé publique

                // Exécuter l'insertion dans la table
                pstmt.executeUpdate();

                // Fermer le ResultSet, Statement et la connexion à la base de données
                resultSet.close();
                walletStatement.close();
                walletConnection.close();

                // Charger le portefeuille dans l'application
                WalletData.getInstance().loadWallet();
            }
        }
    }


}
