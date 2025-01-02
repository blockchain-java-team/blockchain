# Blockchain

This project is a simple blockchain application built using Java. It allows users to create and manage transactions while demonstrating core blockchain concepts such as distributed nodes, transaction handling, and peer-to-peer communication. The application is designed to run with multiple peers, each operating as an independent node in the blockchain network.

---

## **Project Structure**

The project is organized into the following packages for clarity and modularity:

### 1. **`doa`**
Manages the application's database operations and ensures seamless data persistence.

### 2. **`models`**
Contains the basic models of the blockchain, including classes representing blocks, transactions, and the chain itself.

### 3. **`threads`**
Holds the thread classes responsible for handling multi-threaded operations within the application.

### 4. **`service`**
Includes service classes with methods necessary for running the core functionality of the application.

### 5. **`view`**
Contains the FXML files defining the graphical user interface for the application.

### 6. **`controllers`**
Holds the controller classes for managing interactions between the views and the underlying logic.

### 7. **`state`**
Contains classes that interact with the `doa` package to manage and update the application's state.

### 8. **`util`**
Responsible for managing the database connection and utility methods used across the application.

### 9. **`helpers`**
Contains helper classes with utility methods, such as those for converting data formats.

---

## **How to Run the Application with Two or More Peers**

To set up and run the application in a multi-peer environment:

1. **Configure Peer Ports:**
   - Each user must add the other peers' ports to the `PeerClient`'s `port` variable. Update the code as follows:
   ```java
    //this block of code is a part of com.blockchain.threads.PeerClient class
    private Queue<Integer> ports = new ConcurrentLinkedDeque<>();
    Socket socket = null;

    // add other's nodes ports here
    public PeerClient() {
        this.ports.add(5001); //Example: Peer Listening on port 5001
    }
   ```
2. **Define Your Port:**
    - Each user must define their own port in the `EnsaChain` class when passing the port to the `PeerServer` thread.
    ```java
    //this block of code is a part of com.blockchain.EnsaChain class
    public void start(Stage primaryStage) throws Exception {
        new UI().start(primaryStage);
        new MiningThread().start();
        //add your port here (new PeerServer(YOURPORT))
        new PeerServer(5000).start(); 
        new PeerClient().start();
    }
    ```
3. **Run The Application:**
   - Start the application for each peer, ensuring that each instance is correctly configured as described above.

# Dependencies
The project uses the following dependencies, managed via Maven:

- JavaFX Controls (org.openjfx:javafx-controls:23)
- JavaFX FXML (org.openjfx:javafx-fxml:23)
- Gson (com.google.code.gson:gson:2.11.0)
- SQLite JDBC (org.xerial:sqlite-jdbc:3.47.1.0)
- JUnit Jupiter Engine (org.junit.jupiter:junit-jupiter-engine:5.9.1)
- Lombok (org.projectlombok:lombok:1.18.34)
- Jackson Databind (com.fasterxml.jackson.core:jackson-databind:2.18.2)

These libraries are included in the `pom.xml` file for dependency management.