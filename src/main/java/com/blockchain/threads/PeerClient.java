package com.blockchain.threads;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.blockchain.model.Block;
import com.blockchain.service.BlockchainData;

public class PeerClient extends Thread {
    
    /* 
     * Store the port numbers of the peers we want to connect to
     */
    private Queue<Integer> ports = new ConcurrentLinkedDeque<>();
    public PeerClient(){
        this.ports.add(6001);
        this.ports.add(6002);
    }

    @Override
    public void run() {
        while (true) {
            try (Socket socket = new Socket("127.0.0.1", ports.peek())) {
                System.out.println("Sending blockchain object on port: " + ports.peek());
                ports.add(ports.poll());  // move the head to the tail
                socket.setSoTimeout(5000);

                ObjectOutputStream objectOutput = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream objectInput = new ObjectInputStream(socket.getInputStream());

                LinkedList<Block> blockChain = BlockchainData.getInstance().getCurrentBlockChain();
                objectOutput.writeObject(blockChain);

                LinkedList<Block> returnedBlockchain = (LinkedList<Block>) objectInput.readObject();
                System.out.println(" RETURNED BC LedgerId = " + returnedBlockchain.getLast().getLedgerId()  +
                        " Size= " + returnedBlockchain.getLast().getTransactionLedger().size());
                BlockchainData.getInstance().getBlockchainConsensus(returnedBlockchain);
                Thread.sleep(2000);

            } catch (SocketTimeoutException e) {
                System.out.println("The socket timed out");
                ports.add(ports.poll());
            } catch (IOException e) {
                System.out.println("Client Error: " + e.getMessage() + " -- Error on port: "+ ports.peek());
                ports.add(ports.poll());
            } catch (InterruptedException | ClassNotFoundException e) {
                e.printStackTrace();
                ports.add(ports.poll());
            }
        }
    }
}
