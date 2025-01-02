package com.blockchain.threads;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.blockchain.model.Block;
import com.blockchain.service.BlockchainData;

/**
 * we intend to constantly contact other peers and share our
 * blockchain
 */
public class PeerClient extends Thread {

    /**
     * Store the port numbers of the peers we want to connect to
     */
    private Queue<Integer> ports = new ConcurrentLinkedDeque<>();
    Socket socket = null;

    public PeerClient() {
        this.ports.add(5001);
        //this.ports.add(5001);
    }

    @Override
    public void run() {
        //while(true){
            //try (Socket socket = new Socket("127.0.0.1", 5000)) {
                //System.out.println("\n\n\n");
                //System.out.println("you did connect, congratulation!!!");
                //System.out.println("\n\n\n");
                
            //} catch (Exception e) {
                //// TODO: handle exception
            //}
        //}

        while (true) {
            try (Socket socket = new Socket("127.0.0.1", 5001)) {
                System.out.println("Sending blockchain object on port: " + 5001);
                //ports.add(ports.poll()); // move the head to the tail
                socket.setSoTimeout(5000);

                ObjectOutputStream objectOutput = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream objectInput = new ObjectInputStream(socket.getInputStream());

                ObjectMapper mapper = new ObjectMapper();
                // send the blockchain
                LinkedList<Block> blockChain = BlockchainData.getInstance().getCurrentBlockChain();
                System.out.println("-----------------------------------------------------------------------");
                System.out.println("this is the blockchain" + blockChain.size());
                System.out.println("-----------------------------------------------------------------------");
                objectOutput.writeObject(blockChain);
                //objectOutput.writeObject(mapper.writeValueAsString(blockChain));
                System.out.println(mapper.writeValueAsString(blockChain));

                //LinkedList<Block> returnedBlockchain = mapper.readValue(objectInput.readObject().toString(),
                        //new TypeReference<LinkedList<Block>>() {
                        //});


                LinkedList<Block> returnedBlockchain = (LinkedList<Block>) objectInput.readObject() ;

                System.out.println(" RETURNED BC LedgerId = " + returnedBlockchain.getLast().getLedgerId() +
                        " Size= " + returnedBlockchain.getLast().getTransactionLedger().size());
                BlockchainData.getInstance().getBlockchainConsensus(returnedBlockchain);
                Thread.sleep(5000);

            } catch (SocketTimeoutException e) {
                System.out.println("The socket timed out");
                ports.add(ports.poll());
            } catch (IOException e) {
                System.out.println("Client Error: " + e.getMessage() + " -- Error on port: " + ports.peek());
                ports.add(ports.poll());
            } catch (InterruptedException | ClassNotFoundException e) {
                e.printStackTrace();
                ports.add(ports.poll());
            }

        }
    }
}
