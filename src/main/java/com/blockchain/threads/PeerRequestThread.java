package com.blockchain.threads;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;

import com.blockchain.model.Block;
import com.blockchain.service.BlockchainData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PeerRequestThread extends Thread {

    private Socket socket;

    public PeerRequestThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream objectOutput = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInput = new ObjectInputStream(socket.getInputStream());

            LinkedList<Block> recievedBC = (LinkedList<Block>) objectInput.readObject();
            System.out.println("LedgerId = " + recievedBC.getLast().getLedgerId()  +
                    " Size= " + recievedBC.getLast().getTransactionLedger().size());
           objectOutput.writeObject(BlockchainData.getInstance().getBlockchainConsensus(recievedBC));

            //ObjectMapper mapper = new ObjectMapper();
            //ObjectOutputStream objectOutput = new ObjectOutputStream(socket.getOutputStream());
            //ObjectInputStream objectInput = new ObjectInputStream(socket.getInputStream());

            ////LinkedList<Block> recievedBC = (LinkedList<Block>) objectInput.readObject();
            //LinkedList<Block> recievedBC = (LinkedList<Block>) mapper.readValue((String) objectInput.readObject(), new TypeReference<LinkedList<Block>>(){});
            //System.out.println("LedgerId = " + recievedBC.getLast().getLedgerId()  +
                    //" Size= " + recievedBC.getLast().getTransactionLedger().size());
            ////objectOutput.writeObject(BlockchainData.getInstance().getBlockchainConsensus(recievedBC));


            //objectOutput.writeObject(mapper.writeValueAsString(BlockchainData.getInstance().getBlockchainConsensus(recievedBC)));
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    
}
