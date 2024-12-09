package com.blockchain.threads;

import java.io.IOException;
import java.net.ServerSocket;

public class PeerServer {

    private ServerSocket serverSocket;
    public PeerServer(Integer socketPort) throws IOException {
        this.serverSocket = new ServerSocket(socketPort);
    }
    
}
